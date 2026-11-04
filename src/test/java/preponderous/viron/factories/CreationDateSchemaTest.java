// Copyright (c) 2024 Preponderous Software
// MIT License

package preponderous.viron.factories;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import preponderous.viron.config.DataSourceConfig;
import preponderous.viron.config.DbConfig;
import preponderous.viron.database.DbInteractions;
import preponderous.viron.models.Entity;
import preponderous.viron.models.Environment;
import preponderous.viron.repositories.EntityRepositoryImpl;
import preponderous.viron.repositories.EnvironmentRepositoryImpl;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Creates rows against {@code creation_date TIMESTAMP} columns, as declared in
 * {@code db-scripts/setup/create_tables.sql}.
 *
 * <p>The factory unit tests mock {@link DbInteractions}, and the schema tests elsewhere declare
 * {@code creation_date} as {@code VARCHAR}, so neither can see what the column makes of the value
 * bound to it, nor compare the date a create answers with the one a later read answers.
 */
class CreationDateSchemaTest {

    private static final String DATE_TIME = "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}";

    private DataSource dataSource;
    private DbInteractions dbInteractions;

    @BeforeEach
    void setUp() {
        dataSource = new DataSourceConfig().dataSource(h2Config());
        dbInteractions = new DbInteractions(dataSource);

        dbInteractions.update("DROP SCHEMA IF EXISTS viron CASCADE");
        dbInteractions.update("CREATE SCHEMA viron");
        dbInteractions.update("CREATE SEQUENCE viron.entity_id_seq");
        dbInteractions.update("CREATE SEQUENCE viron.environment_id_seq");
        dbInteractions.update("CREATE SEQUENCE viron.grid_id_seq");
        dbInteractions.update("CREATE SEQUENCE viron.location_id_seq");
        dbInteractions.update("CREATE TABLE viron.entity ("
                + "entity_id INT PRIMARY KEY, name VARCHAR(255) NOT NULL, creation_date TIMESTAMP NOT NULL)");
        dbInteractions.update("CREATE TABLE viron.environment ("
                + "environment_id INT PRIMARY KEY, name VARCHAR(255) NOT NULL, creation_date TIMESTAMP NOT NULL)");
        dbInteractions.update("CREATE TABLE viron.location (location_id INT PRIMARY KEY, x INT NOT NULL, y INT NOT NULL)");
        dbInteractions.update("CREATE TABLE viron.grid (grid_id INT PRIMARY KEY, \"rows\" INT, \"columns\" INT, name VARCHAR(255))");
        dbInteractions.update("CREATE TABLE viron.location_grid (grid_id INT NOT NULL, location_id INT NOT NULL)");
        dbInteractions.update("CREATE TABLE viron.grid_environment (environment_id INT NOT NULL, grid_id INT NOT NULL)");
    }

    @AfterEach
    void tearDown() {
        ((HikariDataSource) dataSource).close();
    }

    @Test
    void createEntity_answersTheDateALaterReadAnswers() {
        Entity created = new EntityFactory(dbInteractions).createEntity("Alice");

        Entity read = new EntityRepositoryImpl(dbInteractions).findById(created.getEntityId()).orElseThrow();

        assertThat(created.getCreationDate()).matches(DATE_TIME).isEqualTo(read.getCreationDate());
    }

    @Test
    void createEnvironment_answersTheDateALaterReadAnswers() {
        Environment created = new EnvironmentFactory(dbInteractions).createEnvironment("Meadow", 1, 1, 1);

        Environment read = new EnvironmentRepositoryImpl(dbInteractions).findById(created.getEnvironmentId()).orElseThrow();

        assertThat(created.getCreationDate()).matches(DATE_TIME).isEqualTo(read.getCreationDate());
    }

    private static DbConfig h2Config() {
        DbConfig config = new DbConfig();
        config.setDbUrl("jdbc:h2:mem:viron_creation_date;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
        config.setDbUsername("sa");
        config.setDbPassword("");
        return config;
    }
}
