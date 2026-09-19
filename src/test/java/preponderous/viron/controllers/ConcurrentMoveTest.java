// Copyright (c) 2024 Preponderous Software
// MIT License

package preponderous.viron.controllers;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import preponderous.viron.config.DataSourceConfig;
import preponderous.viron.config.DbConfig;
import preponderous.viron.database.DbInteractions;
import preponderous.viron.exceptions.ConflictException;
import preponderous.viron.mappers.LocationMapperImpl;
import preponderous.viron.repositories.EntityRepositoryImpl;
import preponderous.viron.repositories.LocationRepositoryImpl;

import javax.sql.DataSource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exercises the concurrent path through
 * {@link LocationController#moveEntityToLocation(int, int)} (#203): two entities standing either
 * side of the same empty location both move into it at once, and the collision the endpoint's 409
 * exists to prevent has to be prevented rather than merely reported.
 *
 * <p>Nothing in the schema settles this on its own, unlike the placement race in
 * {@link ConcurrentPlacementTest}: a location may hold several entities as far as the tables are
 * concerned. What settles it is the lock the move takes on the target row, which only outlives
 * the statement that took it inside a transaction — so the controller is driven through a
 * {@link TransactionTemplate} here, standing in for the proxy that applies its
 * {@code @Transactional} at runtime. That the annotation is genuinely applied is asserted
 * separately, in {@code preponderous.viron.config.TransactionBoundaryWiringTest}.
 *
 * <p>The interleaving is forced rather than hoped for: {@link LockRacer} holds both requests at a
 * barrier immediately before they reach for the lock, so they contend for it instead of arriving
 * one after the other.
 */
class ConcurrentMoveTest {

    private static final int LEFT_ENTITY_ID = 1;
    private static final int RIGHT_ENTITY_ID = 2;
    private static final int LEFT_LOCATION_ID = 1;
    private static final int TARGET_LOCATION_ID = 2;
    private static final int RIGHT_LOCATION_ID = 3;
    private static final int GRID_ID = 1;
    private static final int TIMEOUT_SECONDS = 30;

    private DataSource dataSource;
    private DbInteractions dbInteractions;

    @BeforeEach
    void setUp() {
        dataSource = new DataSourceConfig().dataSource(h2Config());
        dbInteractions = new DbInteractions(dataSource);

        dbInteractions.update("DROP TABLE IF EXISTS viron.entity_location");
        dbInteractions.update("DROP TABLE IF EXISTS viron.location_grid");
        dbInteractions.update("DROP TABLE IF EXISTS viron.entity");
        dbInteractions.update("DROP TABLE IF EXISTS viron.location");
        dbInteractions.update("CREATE SCHEMA IF NOT EXISTS viron");
        dbInteractions.update(
                "CREATE TABLE viron.entity (entity_id INT PRIMARY KEY, name VARCHAR(255), creation_date VARCHAR(255))");
        dbInteractions.update("CREATE TABLE viron.location (location_id INT PRIMARY KEY, x INT, y INT)");
        dbInteractions.update("CREATE TABLE viron.location_grid ("
                + "location_id INT NOT NULL, grid_id INT NOT NULL, PRIMARY KEY (location_id), "
                + "FOREIGN KEY (location_id) REFERENCES viron.location(location_id))");
        dbInteractions.update("CREATE TABLE viron.entity_location ("
                + "entity_id INT NOT NULL, location_id INT NOT NULL, PRIMARY KEY (entity_id), "
                + "FOREIGN KEY (entity_id) REFERENCES viron.entity(entity_id), "
                + "FOREIGN KEY (location_id) REFERENCES viron.location(location_id))");

        // Three locations in a row, so the middle one is adjacent to both of the others.
        for (int locationId = LEFT_LOCATION_ID; locationId <= RIGHT_LOCATION_ID; locationId++) {
            dbInteractions.update("INSERT INTO viron.location (location_id, x, y) VALUES (?, ?, ?)",
                    locationId, locationId, 0);
            dbInteractions.update("INSERT INTO viron.location_grid (location_id, grid_id) VALUES (?, ?)",
                    locationId, GRID_ID);
        }
        placeEntity(LEFT_ENTITY_ID, "Alice", LEFT_LOCATION_ID);
        placeEntity(RIGHT_ENTITY_ID, "Bob", RIGHT_LOCATION_ID);
    }

    @AfterEach
    void tearDown() {
        ((HikariDataSource) dataSource).close();
    }

    /**
     * Both entities may move into the empty location between them, but only one of them may end
     * up there. The other is told the target is occupied and is left where it started, which is
     * the same pair of outcomes the two requests would have got had they arrived in sequence.
     */
    @Test
    void concurrentMovesIntoTheSameEmptyLocation_leaveOneEntityThere_andReportTheLoserAsAConflict()
            throws Exception {
        List<Throwable> outcomes = moveConcurrently();

        assertThat(entityIdsAt(TARGET_LOCATION_ID)).hasSize(1);

        int winner = entityIdsAt(TARGET_LOCATION_ID).get(0);
        int loser = winner == LEFT_ENTITY_ID ? RIGHT_ENTITY_ID : LEFT_ENTITY_ID;
        int loserStartingLocation = loser == LEFT_ENTITY_ID ? LEFT_LOCATION_ID : RIGHT_LOCATION_ID;
        assertThat(entityIdsAt(loserStartingLocation)).containsExactly(loser);

        List<Throwable> failures = outcomes.stream().filter(outcome -> outcome != null).toList();
        assertThat(failures).hasSize(1);
        assertThat(failures.get(0))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Target location " + TARGET_LOCATION_ID + " is already occupied");
    }

    /**
     * Runs both moves at once, each in its own transaction, and returns the exception each ended
     * with or {@code null} where it succeeded.
     */
    private List<Throwable> moveConcurrently() throws Exception {
        CyclicBarrier beforeLock = new CyclicBarrier(2);
        LocationController controller = new LocationController(
                new LockRacer(dbInteractions, beforeLock),
                new EntityRepositoryImpl(dbInteractions),
                new LocationMapperImpl());
        TransactionTemplate transactionTemplate =
                new TransactionTemplate(new DataSourceTransactionManager(dataSource));

        ExecutorService executor = Executors.newFixedThreadPool(2);
        List<Future<Throwable>> futures = new ArrayList<>();
        for (int entityId : new int[] {LEFT_ENTITY_ID, RIGHT_ENTITY_ID}) {
            futures.add(executor.submit(() -> {
                try {
                    transactionTemplate.executeWithoutResult(
                            status -> controller.moveEntityToLocation(entityId, TARGET_LOCATION_ID));
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

    private void placeEntity(int entityId, String name, int locationId) {
        dbInteractions.update("INSERT INTO viron.entity (entity_id, name, creation_date) VALUES (?, ?, ?)",
                entityId, name, "2026-01-01");
        dbInteractions.update("INSERT INTO viron.entity_location (entity_id, location_id) VALUES (?, ?)",
                entityId, locationId);
    }

    private List<Integer> entityIdsAt(int locationId) {
        return dbInteractions.query("SELECT entity_id FROM viron.entity_location WHERE location_id = ?",
                rs -> rs.getInt("entity_id"), locationId);
    }

    /**
     * The real repository, with both requests held at a barrier immediately before the lock is
     * taken. Releasing them together is what makes the contention certain instead of a matter of
     * timing: without the barrier the first request tends to have committed long before the
     * second reads the target's occupancy, and the race never occurs.
     */
    private static class LockRacer extends LocationRepositoryImpl {
        private final CyclicBarrier beforeLock;

        LockRacer(DbInteractions dbInteractions, CyclicBarrier beforeLock) {
            super(dbInteractions);
            this.beforeLock = beforeLock;
        }

        @Override
        public boolean lockLocation(int locationId) {
            try {
                beforeLock.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new IllegalStateException("Timed out waiting for the other move to reach the lock", e);
            }
            return super.lockLocation(locationId);
        }
    }

    private static DbConfig h2Config() {
        DbConfig config = new DbConfig();
        config.setDbUrl("jdbc:h2:mem:viron_concurrent_move;DB_CLOSE_DELAY=-1");
        config.setDbUsername("sa");
        config.setDbPassword("");
        // A lock timeout well above the time either transaction needs, so the request that waits
        // for the other one waits rather than failing. Set the way production sets it (#212), so
        // the wait here goes through the same bound.
        config.setLockTimeoutMs(10000);
        return config;
    }
}
