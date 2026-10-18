// Copyright (c) 2024 Preponderous Software
// MIT License

package preponderous.viron.repositories;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import preponderous.viron.config.DataSourceConfig;
import preponderous.viron.config.DbConfig;
import preponderous.viron.database.DbInteractions;
import preponderous.viron.models.Placement;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Issues {@link LocationRepositoryImpl#findAllPlacements()} and
 * {@link LocationRepositoryImpl#findPlacementsByEnvironmentId(int)} against a real schema (#234).
 *
 * <p>{@link LocationRepositoryImplTest} matches the SQL string verbatim against a mocked
 * {@link DbInteractions}, so it asserts that the query is sent, not what it selects. The tables
 * here mirror {@code db-scripts/setup/create_tables.sql}, and every id and coordinate is distinct
 * so that a column read into the wrong field is observable. The placements are inserted out of
 * entity order, so the ordering the endpoint promises is the query's doing rather than the
 * insertion order's.
 */
class LocationRepositoryPlacementsTest {

    private static final int MEADOW_ID = 7;
    private static final int FOREST_ID = 8;
    private static final int EMPTY_ENVIRONMENT_ID = 9;

    private DataSource dataSource;
    private DbInteractions dbInteractions;
    private LocationRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        dataSource = new DataSourceConfig().dataSource(h2Config());
        dbInteractions = new DbInteractions(dataSource);
        repository = new LocationRepositoryImpl(dbInteractions);

        dbInteractions.update("DROP SCHEMA IF EXISTS viron CASCADE");
        dbInteractions.update("CREATE SCHEMA viron");
        dbInteractions.update(
                "CREATE TABLE viron.entity (entity_id INT PRIMARY KEY, name VARCHAR(255), creation_date VARCHAR(255))");
        dbInteractions.update("CREATE TABLE viron.location (location_id INT PRIMARY KEY, x INT, y INT)");
        dbInteractions.update("CREATE TABLE viron.grid (grid_id INT PRIMARY KEY, name VARCHAR(255))");
        dbInteractions.update(
                "CREATE TABLE viron.environment (environment_id INT PRIMARY KEY, name VARCHAR(255), creation_date VARCHAR(255))");
        dbInteractions.update("CREATE TABLE viron.entity_location ("
                + "entity_id INT NOT NULL, location_id INT NOT NULL, PRIMARY KEY (entity_id), "
                + "FOREIGN KEY (entity_id) REFERENCES viron.entity(entity_id), "
                + "FOREIGN KEY (location_id) REFERENCES viron.location(location_id))");
        dbInteractions.update("CREATE TABLE viron.location_grid ("
                + "grid_id INT NOT NULL, location_id INT NOT NULL, PRIMARY KEY (grid_id, location_id), "
                + "FOREIGN KEY (grid_id) REFERENCES viron.grid(grid_id), "
                + "FOREIGN KEY (location_id) REFERENCES viron.location(location_id))");
        dbInteractions.update("CREATE TABLE viron.grid_environment ("
                + "environment_id INT NOT NULL, grid_id INT NOT NULL, PRIMARY KEY (environment_id, grid_id), "
                + "FOREIGN KEY (environment_id) REFERENCES viron.environment(environment_id), "
                + "FOREIGN KEY (grid_id) REFERENCES viron.grid(grid_id))");

        // Meadow holds two grids, Forest one, and the third environment has a grid with nobody on it.
        environment(MEADOW_ID, "Meadow");
        environment(FOREST_ID, "Forest");
        environment(EMPTY_ENVIRONMENT_ID, "Desert");
        grid(10, MEADOW_ID);
        grid(11, MEADOW_ID);
        grid(20, FOREST_ID);
        grid(30, EMPTY_ENVIRONMENT_ID);
        location(100, 10, 1, 2);
        location(101, 10, 3, 4);
        location(110, 11, 5, 6);
        location(200, 20, 13, 14);
        location(300, 30, 15, 16);

        for (int entityId = 1; entityId <= 5; entityId++) {
            dbInteractions.update("INSERT INTO viron.entity (entity_id, name, creation_date) VALUES (?, ?, ?)",
                    entityId, "Entity " + entityId, "2026-01-01");
        }
        // Entities 2 and 3 share location 100; entity 5 stays unplaced.
        place(4, 200);
        place(1, 110);
        place(3, 100);
        place(2, 100);
    }

    @AfterEach
    void tearDown() {
        ((HikariDataSource) dataSource).close();
    }

    @Test
    void findAllPlacements_returnsEveryPlacedEntityAcrossEnvironmentsInEntityOrder() {
        assertThat(repository.findAllPlacements()).containsExactly(
                new Placement(1, MEADOW_ID, 11, 110, 5, 6),
                new Placement(2, MEADOW_ID, 10, 100, 1, 2),
                new Placement(3, MEADOW_ID, 10, 100, 1, 2),
                new Placement(4, FOREST_ID, 20, 200, 13, 14));
    }

    @Test
    void findPlacementsByEnvironmentId_returnsOnlyThatEnvironmentsPlacements() {
        assertThat(repository.findPlacementsByEnvironmentId(MEADOW_ID)).containsExactly(
                new Placement(1, MEADOW_ID, 11, 110, 5, 6),
                new Placement(2, MEADOW_ID, 10, 100, 1, 2),
                new Placement(3, MEADOW_ID, 10, 100, 1, 2));
        assertThat(repository.findPlacementsByEnvironmentId(FOREST_ID)).containsExactly(
                new Placement(4, FOREST_ID, 20, 200, 13, 14));
    }

    @Test
    void findPlacementsByEnvironmentId_returnsEmptyForAnEnvironmentWithNoPlacedEntities() {
        assertThat(repository.findPlacementsByEnvironmentId(EMPTY_ENVIRONMENT_ID)).isEmpty();
    }

    @Test
    void findPlacementsByEnvironmentId_returnsEmptyForAnUnknownEnvironment() {
        assertThat(repository.findPlacementsByEnvironmentId(99)).isEmpty();
    }

    @Test
    void findAllPlacements_returnsEmptyWhenNothingIsPlaced() {
        dbInteractions.update("DELETE FROM viron.entity_location");

        assertThat(repository.findAllPlacements()).isEmpty();
    }

    private void environment(int environmentId, String name) {
        dbInteractions.update("INSERT INTO viron.environment (environment_id, name, creation_date) VALUES (?, ?, ?)",
                environmentId, name, "2026-01-01");
    }

    private void grid(int gridId, int environmentId) {
        dbInteractions.update("INSERT INTO viron.grid (grid_id, name) VALUES (?, ?)", gridId, "Grid " + gridId);
        dbInteractions.update("INSERT INTO viron.grid_environment (environment_id, grid_id) VALUES (?, ?)",
                environmentId, gridId);
    }

    private void location(int locationId, int gridId, int x, int y) {
        dbInteractions.update("INSERT INTO viron.location (location_id, x, y) VALUES (?, ?, ?)", locationId, x, y);
        dbInteractions.update("INSERT INTO viron.location_grid (grid_id, location_id) VALUES (?, ?)",
                gridId, locationId);
    }

    private void place(int entityId, int locationId) {
        dbInteractions.update("INSERT INTO viron.entity_location (entity_id, location_id) VALUES (?, ?)",
                entityId, locationId);
    }

    private static DbConfig h2Config() {
        DbConfig config = new DbConfig();
        config.setDbUrl("jdbc:h2:mem:viron_location_placements;DB_CLOSE_DELAY=-1");
        config.setDbUsername("sa");
        config.setDbPassword("");
        return config;
    }
}
