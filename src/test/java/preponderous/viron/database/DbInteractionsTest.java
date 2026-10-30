package preponderous.viron.database;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DuplicateKeyException;
import preponderous.viron.config.DataSourceConfig;
import preponderous.viron.config.DbConfig;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Exercises {@link DbInteractions} against an in-memory H2 database to verify
 * parameter binding (#139), resource handling (#140), the row-mapping
 * query API (#144), and connection pooling (#194). The application's real SQL is
 * Postgres-specific; this test uses its own neutral schema and only validates the
 * generic query/update mechanism.
 *
 * <p>The {@link DataSource} is built by the production {@link DataSourceConfig} so the
 * wiring under test is the wiring that ships.
 */
public class DbInteractionsTest {

    /** Row shape used by this test's neutral {@code person} schema. */
    private record Person(int id, String name) {
    }

    private static final RowMapper<Person> PERSON_MAPPER =
            rs -> new Person(rs.getInt("id"), rs.getString("name"));

    private DataSource dataSource;
    private DbInteractions dbInteractions;

    @BeforeEach
    void setUp() {
        dataSource = new DataSourceConfig().dataSource(h2Config());
        dbInteractions = new DbInteractions(dataSource);

        dbInteractions.update("DROP TABLE IF EXISTS person");
        dbInteractions.update("CREATE TABLE person (id INT PRIMARY KEY, name VARCHAR(255))");
    }

    @AfterEach
    void tearDown() {
        ((HikariDataSource) dataSource).close();
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

    // #200: a duplicate key is a conflict, and updateReportingDuplicateKey is the only way a
    // caller can tell it apart from any other failed write.
    @Test
    void updateReportingDuplicateKey_onDuplicateKey_throws() {
        assertThat(dbInteractions.update("INSERT INTO person (id, name) VALUES (?, ?)", 8, "Heidi")).isTrue();

        assertThatThrownBy(() ->
                dbInteractions.updateReportingDuplicateKey("INSERT INTO person (id, name) VALUES (?, ?)", 8, "Ivan"))
                .isInstanceOf(DuplicateKeyException.class);

        // The row that was already there is untouched.
        assertThat(dbInteractions.queryOne("SELECT id, name FROM person WHERE id = ?", PERSON_MAPPER, 8))
                .contains(new Person(8, "Heidi"));
    }

    @Test
    void updateReportingDuplicateKey_onAnyOtherFailure_returnsFalseAndDoesNotThrow() {
        assertThat(dbInteractions.updateReportingDuplicateKey("UPDATE does_not_exist SET name = ?", "x")).isFalse();
    }

    // #252: Postgres reports a write that gave up on a row lock as 55P03 and a broken deadlock as
    // 40P01. Neither is evidence the row is absent, so both are reported rather than flattened to
    // false — through update() as well, which only flattens a duplicate key.
    @Test
    void update_onPostgresLockFailure_throwsCannotAcquireLock() throws SQLException {
        for (String sqlState : List.of("55P03", "40P01")) {
            DbInteractions failing = new DbInteractions(dataSourceFailingUpdatesWith(sqlState));

            assertThatThrownBy(() -> failing.update("DELETE FROM person WHERE id = ?", 1))
                    .as("SQL state %s", sqlState)
                    .isInstanceOf(CannotAcquireLockException.class);
            assertThatThrownBy(() -> failing.updateReportingDuplicateKey("DELETE FROM person WHERE id = ?", 1))
                    .as("SQL state %s", sqlState)
                    .isInstanceOf(CannotAcquireLockException.class);
        }
    }

    @Test
    void update_onAFailureThatIsNotALockFailure_stillReturnsFalse() throws SQLException {
        DbInteractions failing = new DbInteractions(dataSourceFailingUpdatesWith("23503"));

        assertThat(failing.update("DELETE FROM person WHERE id = ?", 1)).isFalse();
    }

    private static DataSource dataSourceFailingUpdatesWith(String sqlState) throws SQLException {
        PreparedStatement statement = mock(PreparedStatement.class);
        when(statement.executeUpdate()).thenThrow(new SQLException("failed", sqlState));
        Connection connection = mock(Connection.class);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        DataSource failingDataSource = mock(DataSource.class);
        when(failingDataSource.getConnection()).thenReturn(connection);
        return failingDataSource;
    }

    @Test
    void updateReportingDuplicateKey_onSuccess_returnsTrue() {
        assertThat(dbInteractions.updateReportingDuplicateKey("INSERT INTO person (id, name) VALUES (?, ?)", 9, "Judy"))
                .isTrue();
        assertThat(dbInteractions.queryOne("SELECT id, name FROM person WHERE id = ?", PERSON_MAPPER, 9)).isPresent();
    }

    // The contract every pre-existing caller was written against: update() still flattens a
    // duplicate key to false rather than throwing at code that cannot handle it.
    @Test
    void update_onDuplicateKey_stillReturnsFalse() {
        assertThat(dbInteractions.update("INSERT INTO person (id, name) VALUES (?, ?)", 10, "Karl")).isTrue();

        assertThat(dbInteractions.update("INSERT INTO person (id, name) VALUES (?, ?)", 10, "Liam")).isFalse();
    }

    // A duplicate key leaves nothing checked out: the throwing path returns its connection too.
    @Test
    void updateReportingDuplicateKey_onDuplicateKey_returnsItsConnectionToThePool() {
        HikariDataSource singleConnectionPool = (HikariDataSource) new DataSourceConfig().dataSource(h2Config());
        singleConnectionPool.setMaximumPoolSize(1);
        singleConnectionPool.setConnectionTimeout(1000);

        DbInteractions pooled = new DbInteractions(singleConnectionPool);
        try {
            assertThat(pooled.update("INSERT INTO person (id, name) VALUES (?, ?)", 11, "Mona")).isTrue();

            for (int i = 0; i < 5; i++) {
                assertThatThrownBy(() ->
                        pooled.updateReportingDuplicateKey("INSERT INTO person (id, name) VALUES (?, ?)", 11, "Nina"))
                        .isInstanceOf(DuplicateKeyException.class);
            }

            assertThat(pooled.queryOne("SELECT id, name FROM person WHERE id = ?", PERSON_MAPPER, 11)).isPresent();
        } finally {
            singleConnectionPool.close();
        }
    }

    // #194: every path must hand its connection back to the pool. A pool of one with a short
    // acquisition timeout turns any leak — including one on an error path — into a failure on
    // the very next call.
    @Test
    void everyPath_returnsItsConnectionToThePool() {
        HikariDataSource singleConnectionPool = (HikariDataSource) new DataSourceConfig().dataSource(h2Config());
        singleConnectionPool.setMaximumPoolSize(1);
        singleConnectionPool.setConnectionTimeout(1000);

        DbInteractions pooled = new DbInteractions(singleConnectionPool);
        try {
            for (int i = 0; i < 20; i++) {
                int id = 100 + i;
                assertThat(pooled.update("INSERT INTO person (id, name) VALUES (?, ?)", id, "Pooled" + id)).isTrue();
                assertThat(pooled.queryOne("SELECT id, name FROM person WHERE id = ?", PERSON_MAPPER, id)).isPresent();
                assertThat(pooled.query("SELECT id, name FROM person", PERSON_MAPPER)).isNotEmpty();

                assertThat(pooled.update("UPDATE does_not_exist SET name = ?", "x")).isFalse();
                assertThat(pooled.query("SELECT * FROM does_not_exist", PERSON_MAPPER)).isEmpty();
                assertThat(pooled.queryOne("SELECT * FROM does_not_exist", PERSON_MAPPER)).isEmpty();
            }
        } finally {
            singleConnectionPool.close();
        }
    }

    // #194: concurrent callers each get their own connection. The previous design held one
    // shared Connection on an application-scoped component, which JDBC does not require to be
    // thread-safe.
    @Test
    void concurrentCallers_eachSeeTheirOwnWrite() throws Exception {
        int threads = 8;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Optional<Person>>> results = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            int id = 200 + i;
            results.add(executor.submit(() -> {
                start.await();
                dbInteractions.update("INSERT INTO person (id, name) VALUES (?, ?)", id, "Concurrent" + id);
                return dbInteractions.queryOne("SELECT id, name FROM person WHERE id = ?", PERSON_MAPPER, id);
            }));
        }
        start.countDown();

        try {
            for (int i = 0; i < threads; i++) {
                assertThat(results.get(i).get(30, TimeUnit.SECONDS))
                        .contains(new Person(200 + i, "Concurrent" + (200 + i)));
            }
        } finally {
            executor.shutdownNow();
        }
    }

    // #203: a locking SELECT reports whether it matched a row, and reports a failure rather than
    // flattening it into "no such row" — a caller that locks a row in order to decide something
    // on the strength of holding it cannot tell the two apart otherwise.
    @Test
    void lock_reportsWhetherARowWasMatched() {
        dbInteractions.update("INSERT INTO person (id, name) VALUES (?, ?)", 300, "Locked");

        assertThat(dbInteractions.lock("SELECT id FROM person WHERE id = ? FOR UPDATE", 300)).isTrue();
        assertThat(dbInteractions.lock("SELECT id FROM person WHERE id = ? FOR UPDATE", 301)).isFalse();
    }

    @Test
    void lock_reportsAFailedStatementInsteadOfReturningFalse() {
        assertThatThrownBy(() -> dbInteractions.lock("SELECT id FROM no_such_table WHERE id = ? FOR UPDATE", 1))
                .isInstanceOf(CannotAcquireLockException.class);
    }

    private static DbConfig h2Config() {
        DbConfig config = new DbConfig();
        config.setDbUrl("jdbc:h2:mem:viron_dbinteractions;DB_CLOSE_DELAY=-1");
        config.setDbUsername("sa");
        config.setDbPassword("");
        return config;
    }
}
