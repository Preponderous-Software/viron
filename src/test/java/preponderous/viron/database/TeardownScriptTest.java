// Copyright (c) 2024 Preponderous Software
// MIT License

package preponderous.viron.database;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import preponderous.viron.config.DataSourceConfig;
import preponderous.viron.config.DbConfig;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs the real {@code db-scripts/setup/create_sequences.sql} and
 * {@code db-scripts/teardown/delete_tables.sql} files, in order, against H2 in PostgreSQL mode.
 *
 * <p>The sequences are standalone (not {@code OWNED BY} a column), so dropping the tables does not
 * remove them. A reset database should restart every id at 1, which needs the teardown to drop the
 * sequences itself.
 */
class TeardownScriptTest {

    private static final Path CREATE_SEQUENCES = Path.of("db-scripts", "setup", "create_sequences.sql");
    private static final Path TEARDOWN = Path.of("db-scripts", "teardown", "delete_tables.sql");
    private static final String[] SEQUENCES = {
            "entity_id_seq", "environment_id_seq", "grid_id_seq", "location_id_seq"
    };

    private DataSource dataSource;
    private DbInteractions dbInteractions;

    @BeforeEach
    void setUp() {
        dataSource = new DataSourceConfig().dataSource(h2Config());
        dbInteractions = new DbInteractions(dataSource);
        dbInteractions.update("DROP SCHEMA IF EXISTS viron CASCADE");
    }

    @AfterEach
    void tearDown() {
        ((HikariDataSource) dataSource).close();
    }

    @Test
    void teardownThenSetup_restartsEverySequenceAtOne() throws IOException {
        runScript(CREATE_SEQUENCES);
        for (String sequence : SEQUENCES) {
            nextval(sequence);
            nextval(sequence);
            nextval(sequence);
        }

        runScript(TEARDOWN);
        runScript(CREATE_SEQUENCES);

        for (String sequence : SEQUENCES) {
            assertThat(nextval(sequence)).as(sequence).isEqualTo(1);
        }
    }

    @Test
    void teardown_runsAgainstAnEmptySchema() throws IOException {
        dbInteractions.update("CREATE SCHEMA viron");

        runScript(TEARDOWN);

        runScript(CREATE_SEQUENCES);
        assertThat(nextval("entity_id_seq")).isEqualTo(1);
    }

    private int nextval(String sequence) {
        return dbInteractions.queryOne("SELECT nextval('viron." + sequence + "')", rs -> rs.getInt(1))
                .orElseThrow();
    }

    private void runScript(Path script) throws IOException {
        String withoutComments = Files.readAllLines(script).stream()
                .filter(line -> !line.trim().startsWith("--"))
                .collect(Collectors.joining("\n"));
        Arrays.stream(withoutComments.split(";"))
                .map(String::trim)
                .filter(statement -> !statement.isEmpty())
                .forEach(dbInteractions::update);
    }

    private static DbConfig h2Config() {
        DbConfig config = new DbConfig();
        config.setDbUrl("jdbc:h2:mem:viron_teardown;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
        config.setDbUsername("sa");
        config.setDbPassword("");
        return config;
    }
}
