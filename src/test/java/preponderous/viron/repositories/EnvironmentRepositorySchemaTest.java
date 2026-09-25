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
import preponderous.viron.models.Environment;

import javax.sql.DataSource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Issues {@link EnvironmentRepositoryImpl#findByEntityId(int)} against a real schema (#221).
 *
 * <p>{@link EnvironmentRepositoryImplTest} matches the SQL string verbatim against a mocked
 * {@link DbInteractions}, so it asserts that the query is sent, not what it selects. The query it
 * once pinned read {@code environment_id} from {@code viron.entity}, which has no such column; the
 * name resolved to the outer {@code viron.environment} row instead, so any existing entity, placed
 * or not, was answered with whichever environment came first. The tables here mirror
 * {@code db-scripts/setup/create_tables.sql}, so an entity's environment is reachable only through
 * {@code entity_location}, {@code location_grid} and {@code grid_environment}, as it is in
 * production, and the entity's own environment is deliberately not the first one.
 */
class EnvironmentRepositorySchemaTest {

    private static final int OTHER_ENVIRONMENT_ID = 7;
    private static final int ENVIRONMENT_ID = 8;
    private static final int GRID_ID = 3;
    private static final int LOCATION_ID = 5;
    private static final int PLACED_ENTITY_ID = 1;
    private static final int UNPLACED_ENTITY_ID = 2;

    private DataSource dataSource;
    private DbInteractions dbInteractions;

    @BeforeEach
    void setUp() {
        dataSource = new DataSourceConfig().dataSource(h2Config());
        dbInteractions = new DbInteractions(dataSource);

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

        dbInteractions.update("INSERT INTO viron.environment (environment_id, name, creation_date) VALUES (?, ?, ?)",
                OTHER_ENVIRONMENT_ID, "Elsewhere", "2026-01-01");
        dbInteractions.update("INSERT INTO viron.environment (environment_id, name, creation_date) VALUES (?, ?, ?)",
                ENVIRONMENT_ID, "Meadow", "2026-01-02");
        dbInteractions.update("INSERT INTO viron.grid (grid_id, name) VALUES (?, ?)", GRID_ID, "Grid");
        dbInteractions.update("INSERT INTO viron.grid_environment (environment_id, grid_id) VALUES (?, ?)",
                ENVIRONMENT_ID, GRID_ID);
        dbInteractions.update("INSERT INTO viron.location (location_id, x, y) VALUES (?, ?, ?)", LOCATION_ID, 0, 0);
        dbInteractions.update("INSERT INTO viron.location_grid (grid_id, location_id) VALUES (?, ?)",
                GRID_ID, LOCATION_ID);
        dbInteractions.update("INSERT INTO viron.entity (entity_id, name, creation_date) VALUES (?, ?, ?)",
                PLACED_ENTITY_ID, "Alice", "2026-01-03");
        dbInteractions.update("INSERT INTO viron.entity (entity_id, name, creation_date) VALUES (?, ?, ?)",
                UNPLACED_ENTITY_ID, "Bob", "2026-01-04");
        dbInteractions.update("INSERT INTO viron.entity_location (entity_id, location_id) VALUES (?, ?)",
                PLACED_ENTITY_ID, LOCATION_ID);
    }

    @AfterEach
    void tearDown() {
        ((HikariDataSource) dataSource).close();
    }

    @Test
    void findByEntityId_returnsTheEnvironmentOfAPlacedEntity() {
        Optional<Environment> environment = new EnvironmentRepositoryImpl(dbInteractions).findByEntityId(PLACED_ENTITY_ID);

        assertThat(environment).isPresent();
        assertThat(environment.get().getEnvironmentId()).isEqualTo(ENVIRONMENT_ID);
        assertThat(environment.get().getName()).isEqualTo("Meadow");
    }

    @Test
    void findByEntityId_returnsEmptyForAnUnplacedEntity() {
        assertThat(new EnvironmentRepositoryImpl(dbInteractions).findByEntityId(UNPLACED_ENTITY_ID)).isEmpty();
    }

    private static DbConfig h2Config() {
        DbConfig config = new DbConfig();
        config.setDbUrl("jdbc:h2:mem:viron_environment_schema;DB_CLOSE_DELAY=-1");
        config.setDbUsername("sa");
        config.setDbPassword("");
        return config;
    }
}
