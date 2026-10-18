// Copyright (c) 2024 Preponderous Software
// MIT License

package preponderous.viron.controllers;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import preponderous.viron.config.DataSourceConfig;
import preponderous.viron.config.DbConfig;
import preponderous.viron.database.DbInteractions;
import preponderous.viron.exceptions.ConflictException;
import preponderous.viron.mappers.LocationMapperImpl;
import preponderous.viron.mappers.PlacementMapperImpl;
import preponderous.viron.models.Location;
import preponderous.viron.repositories.EntityRepositoryImpl;
import preponderous.viron.repositories.LocationRepositoryImpl;

import javax.sql.DataSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.IntUnaryOperator;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exercises the concurrent path through
 * {@link LocationController#addEntityToLocation(int, int)} (#200) rather than only its
 * sequential guard: several requests race to place the same unplaced entity, and the invariant
 * that an entity occupies at most one location has to survive.
 *
 * <p>The controller is assembled over real repositories and an in-memory H2 database, because
 * what settles the race is the primary key on {@code entity_location.entity_id} — nothing a
 * mocked repository could reproduce. The schema mirrors the Postgres one in
 * {@code db-scripts/setup/create_tables.sql}, including that key.
 *
 * <p>The interleaving is forced rather than hoped for. Simply releasing threads together does
 * not reproduce it: the winner commits so quickly that every other request's guard read already
 * sees the placement and is refused by the sequential check. {@link GuardReadRacer} therefore
 * holds every request at a barrier immediately after its placement read, so all of them observe
 * an unplaced entity and only then go on to insert.
 */
class ConcurrentPlacementTest {

    private static final int ENTITY_ID = 1;
    private static final int REQUESTS = 8;
    private static final int TIMEOUT_SECONDS = 30;

    private DataSource dataSource;
    private DbInteractions dbInteractions;

    @BeforeEach
    void setUp() {
        dataSource = new DataSourceConfig().dataSource(h2Config());
        dbInteractions = new DbInteractions(dataSource);

        dbInteractions.update("DROP TABLE IF EXISTS viron.entity_location");
        dbInteractions.update("DROP TABLE IF EXISTS viron.entity");
        dbInteractions.update("DROP TABLE IF EXISTS viron.location");
        dbInteractions.update("CREATE SCHEMA IF NOT EXISTS viron");
        dbInteractions.update(
                "CREATE TABLE viron.entity (entity_id INT PRIMARY KEY, name VARCHAR(255), creation_date VARCHAR(255))");
        dbInteractions.update("CREATE TABLE viron.location (location_id INT PRIMARY KEY, x INT, y INT)");
        dbInteractions.update("CREATE TABLE viron.entity_location ("
                + "entity_id INT NOT NULL, location_id INT NOT NULL, PRIMARY KEY (entity_id), "
                + "FOREIGN KEY (entity_id) REFERENCES viron.entity(entity_id), "
                + "FOREIGN KEY (location_id) REFERENCES viron.location(location_id))");

        dbInteractions.update("INSERT INTO viron.entity (entity_id, name, creation_date) VALUES (?, ?, ?)",
                ENTITY_ID, "Alice", "2026-01-01");
        for (int locationId = 1; locationId <= REQUESTS; locationId++) {
            dbInteractions.update("INSERT INTO viron.location (location_id, x, y) VALUES (?, ?, ?)",
                    locationId, locationId, 0);
        }
    }

    @AfterEach
    void tearDown() {
        ((HikariDataSource) dataSource).close();
    }

    /**
     * Every request asks for a different location, so exactly one placement may survive and every
     * other request has to be told, as a conflict, where the entity actually ended up.
     */
    @Test
    void concurrentPlacementsAtDifferentLocations_leaveExactlyOnePlacement_andReportTheLosersAsConflicts()
            throws Exception {
        List<Throwable> outcomes = placeConcurrently(request -> request);

        List<Integer> placements = placedLocationIds();
        assertThat(placements).hasSize(1);

        int winningLocationId = placements.get(0);
        List<Throwable> failures = outcomes.stream().filter(outcome -> outcome != null).toList();
        assertThat(failures).hasSize(REQUESTS - 1);
        assertThat(failures).allSatisfy(failure -> assertThat(failure)
                .isInstanceOf(ConflictException.class)
                .hasMessage("Entity " + ENTITY_ID + " is already placed at location " + winningLocationId));
    }

    /**
     * Every request asks for the same location, so every one of them got the outcome it wanted:
     * the endpoint is idempotent whether the requests arrive together or in sequence.
     */
    @Test
    void concurrentPlacementsAtTheSameLocation_allSucceed_andLeaveExactlyOnePlacement() throws Exception {
        List<Throwable> outcomes = placeConcurrently(request -> 1);

        assertThat(placedLocationIds()).containsExactly(1);
        assertThat(outcomes).containsOnlyNulls();
    }

    /**
     * Runs {@link #REQUESTS} placements of the same entity at once, each against the location
     * {@code targetLocation} derives from its request number. Returns the exception each request
     * ended with, or {@code null} where it succeeded, in request order.
     */
    private List<Throwable> placeConcurrently(IntUnaryOperator targetLocation) throws Exception {
        CyclicBarrier afterGuardRead = new CyclicBarrier(REQUESTS);
        LocationController controller = new LocationController(
                new GuardReadRacer(dbInteractions, afterGuardRead),
                new EntityRepositoryImpl(dbInteractions),
                new LocationMapperImpl(),
                new PlacementMapperImpl());

        ExecutorService executor = Executors.newFixedThreadPool(REQUESTS);
        List<Future<Throwable>> futures = new ArrayList<>();
        for (int request = 1; request <= REQUESTS; request++) {
            int locationId = targetLocation.applyAsInt(request);
            futures.add(executor.submit(() -> {
                try {
                    controller.addEntityToLocation(ENTITY_ID, locationId);
                    return null;
                } catch (Throwable t) {
                    return t;
                }
            }));
        }

        try {
            List<Throwable> outcomes = new ArrayList<>();
            for (Future<Throwable> future : futures) {
                outcomes.add(future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS));
            }
            return outcomes;
        } finally {
            executor.shutdownNow();
        }
    }

    private List<Integer> placedLocationIds() {
        return dbInteractions.query("SELECT location_id FROM viron.entity_location WHERE entity_id = ?",
                rs -> rs.getInt("location_id"), ENTITY_ID);
    }

    /**
     * The real repository, with every request held at a barrier the first time it reads a
     * placement. That is the window the controller's guard cannot cover on its own, and holding
     * it open makes the race a certainty instead of a matter of timing.
     *
     * <p>Only the first read per thread waits: a request that loses the race reads the placement
     * a second time to find out where the entity ended up, and by then the other requests have
     * long since left the barrier.
     */
    private static class GuardReadRacer extends LocationRepositoryImpl {
        private final CyclicBarrier afterGuardRead;
        private final ThreadLocal<Boolean> hasWaited = ThreadLocal.withInitial(() -> false);

        GuardReadRacer(DbInteractions dbInteractions, CyclicBarrier afterGuardRead) {
            super(dbInteractions);
            this.afterGuardRead = afterGuardRead;
        }

        @Override
        public Optional<Location> findByEntityId(int entityId) {
            Optional<Location> placement = super.findByEntityId(entityId);
            if (!hasWaited.get()) {
                hasWaited.set(true);
                try {
                    afterGuardRead.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                } catch (Exception e) {
                    throw new IllegalStateException("Timed out waiting for the other placements to read", e);
                }
            }
            return placement;
        }
    }

    private static DbConfig h2Config() {
        DbConfig config = new DbConfig();
        config.setDbUrl("jdbc:h2:mem:viron_concurrent_placement;DB_CLOSE_DELAY=-1");
        config.setDbUsername("sa");
        config.setDbPassword("");
        return config;
    }
}
