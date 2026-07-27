package preponderous.viron.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import preponderous.viron.config.DbConfig;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exercises {@link DbInteractions} against an in-memory H2 database to verify
 * parameter binding (#139), resource handling (#140), and the row-mapping
 * query API (#144). The application's real SQL is Postgres-specific; this test
 * uses its own neutral schema and only validates the generic query/update
 * mechanism.
 */
public class DbInteractionsTest {

    /** Row shape used by this test's neutral {@code person} schema. */
    private record Person(int id, String name) {
    }

    private static final RowMapper<Person> PERSON_MAPPER =
            rs -> new Person(rs.getInt("id"), rs.getString("name"));

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
    void update_bindsParameters_andQueryOneReturnsMatchingRow() {
        boolean inserted = dbInteractions.update("INSERT INTO person (id, name) VALUES (?, ?)", 1, "Alice");
        assertThat(inserted).isTrue();

        Optional<Person> person =
                dbInteractions.queryOne("SELECT id, name FROM person WHERE id = ?", PERSON_MAPPER, 1);

        assertThat(person).contains(new Person(1, "Alice"));
    }

    // #139: a value containing a quote / SQL payload must be stored verbatim, never executed.
    @Test
    void parameterizedValueWithSqlPayload_isStoredLiterally_andDoesNotInject() {
        String tricky = "O'Brien'); DROP TABLE person; --";

        assertThat(dbInteractions.update("INSERT INTO person (id, name) VALUES (?, ?)", 2, tricky)).isTrue();

        // The table still exists (payload did not execute) and the value round-trips intact.
        Optional<Person> person =
                dbInteractions.queryOne("SELECT id, name FROM person WHERE id = ?", PERSON_MAPPER, 2);
        assertThat(person).isPresent();
        assertThat(person.get().name()).isEqualTo(tricky);
    }

    // #140/#144: query() owns the cursor — callers only ever see mapped rows.
    @Test
    void query_mapsEveryRow_inResultSetOrder() {
        dbInteractions.update("INSERT INTO person (id, name) VALUES (?, ?)", 3, "Carol");
        dbInteractions.update("INSERT INTO person (id, name) VALUES (?, ?)", 4, "Dave");

        List<Person> people = dbInteractions.query("SELECT id, name FROM person ORDER BY id", PERSON_MAPPER);

        assertThat(people).containsExactly(new Person(3, "Carol"), new Person(4, "Dave"));
    }

    @Test
    void query_withNoMatchingRows_returnsEmptyList() {
        assertThat(dbInteractions.query("SELECT id, name FROM person WHERE id = ?", PERSON_MAPPER, 999)).isEmpty();
    }

    @Test
    void queryOne_withNoMatchingRows_returnsEmptyOptional() {
        assertThat(dbInteractions.queryOne("SELECT id, name FROM person WHERE id = ?", PERSON_MAPPER, 999)).isEmpty();
    }

    @Test
    void queryOne_withMultipleMatchingRows_returnsFirst() {
        dbInteractions.update("INSERT INTO person (id, name) VALUES (?, ?)", 5, "Erin");
        dbInteractions.update("INSERT INTO person (id, name) VALUES (?, ?)", 6, "Frank");

        assertThat(dbInteractions.queryOne("SELECT id, name FROM person ORDER BY id", PERSON_MAPPER))
                .contains(new Person(5, "Erin"));
    }

    @Test
    void query_onInvalidSql_returnsEmptyListAndDoesNotThrow() {
        assertThat(dbInteractions.query("SELECT * FROM does_not_exist", PERSON_MAPPER)).isEmpty();
    }

    @Test
    void queryOne_onInvalidSql_returnsEmptyOptionalAndDoesNotThrow() {
        assertThat(dbInteractions.queryOne("SELECT * FROM does_not_exist", PERSON_MAPPER)).isEmpty();
    }

    // A mapper that fails mid-iteration must not surface partially mapped rows.
    @Test
    void query_whenMapperThrows_returnsEmptyListAndDoesNotThrow() {
        dbInteractions.update("INSERT INTO person (id, name) VALUES (?, ?)", 7, "Grace");

        RowMapper<Person> failing = rs -> {
            throw new SQLException("boom");
        };

        assertThat(dbInteractions.query("SELECT id, name FROM person", failing)).isEmpty();
        assertThat(dbInteractions.queryOne("SELECT id, name FROM person", failing)).isEmpty();
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
