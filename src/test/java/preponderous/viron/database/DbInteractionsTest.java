package preponderous.viron.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import preponderous.viron.config.DbConfig;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exercises {@link DbInteractions} against an in-memory H2 database to verify
 * parameter binding (#139) and resource handling (#140). The application's real
 * SQL is Postgres-specific; this test uses its own neutral schema and only
 * validates the generic query/update mechanism.
 */
public class DbInteractionsTest {

    private DbInteractions dbInteractions;

    @BeforeEach
    void setUp() {
        DbConfig config = new DbConfig();
        config.setDbUrl("jdbc:h2:mem:viron_dbinteractions;DB_CLOSE_DELAY=-1");
        config.setDbUsername("sa");
        config.setDbPassword("");
        dbInteractions = new DbInteractions(config);

        dbInteractions.update("DROP TABLE IF EXISTS person");
        dbInteractions.update("CREATE TABLE person (id INT PRIMARY KEY, name VARCHAR(255))");
    }

    @Test
    void update_bindsParameters_andQueryReturnsMatchingRow() throws SQLException {
        boolean inserted = dbInteractions.update("INSERT INTO person (id, name) VALUES (?, ?)", 1, "Alice");
        assertThat(inserted).isTrue();

        ResultSet rs = dbInteractions.query("SELECT id, name FROM person WHERE id = ?", 1);

        assertThat(rs).isNotNull();
        assertThat(rs.next()).isTrue();
        assertThat(rs.getInt("id")).isEqualTo(1);
        assertThat(rs.getString("name")).isEqualTo("Alice");
        assertThat(rs.next()).isFalse();
    }

    // #139: a value containing a quote / SQL payload must be stored verbatim, never executed.
    @Test
    void parameterizedValueWithSqlPayload_isStoredLiterally_andDoesNotInject() throws SQLException {
        String tricky = "O'Brien'); DROP TABLE person; --";

        assertThat(dbInteractions.update("INSERT INTO person (id, name) VALUES (?, ?)", 2, tricky)).isTrue();

        // The table still exists (payload did not execute) and the value round-trips intact.
        ResultSet rs = dbInteractions.query("SELECT name FROM person WHERE id = ?", 2);
        assertThat(rs).isNotNull();
        assertThat(rs.next()).isTrue();
        assertThat(rs.getString("name")).isEqualTo(tricky);
    }

    // #140: query() returns a disconnected result that stays readable after the method has
    // returned and closed its PreparedStatement and live ResultSet internally.
    @Test
    void query_returnsDisconnectedResultSet_readableAfterReturn() throws SQLException {
        dbInteractions.update("INSERT INTO person (id, name) VALUES (?, ?)", 3, "Carol");
        dbInteractions.update("INSERT INTO person (id, name) VALUES (?, ?)", 4, "Dave");

        ResultSet rs = dbInteractions.query("SELECT id, name FROM person ORDER BY id");

        assertThat(rs).isNotNull();
        int count = 0;
        while (rs.next()) {
            count++;
        }
        assertThat(count).isEqualTo(2);
    }

    @Test
    void query_onInvalidSql_returnsNullAndDoesNotThrow() {
        assertThat(dbInteractions.query("SELECT * FROM does_not_exist")).isNull();
    }

    @Test
    void update_onInvalidSql_returnsFalseAndDoesNotThrow() {
        assertThat(dbInteractions.update("UPDATE does_not_exist SET name = ?", "x")).isFalse();
    }

    @Test
    void update_affectingNoRows_returnsFalse() {
        assertThat(dbInteractions.update("UPDATE person SET name = ? WHERE id = ?", "Nobody", 999)).isFalse();
    }
}
