// Copyright (c) 2024 Preponderous Software
// MIT License

package preponderous.viron.config;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.transaction.support.TransactionTemplate;
import preponderous.viron.database.DbInteractions;
import preponderous.viron.repositories.LocationRepositoryImpl;

import javax.sql.DataSource;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Proves the lock wait {@link DataSourceConfig} bounds (#212) is actually bounded: a request
 * whose row lock is held by another transaction fails within the configured time instead of
 * waiting on it indefinitely.
 *
 * <p>The bound is set on the pool's connections, not on any statement, so the real
 * {@link LocationRepositoryImpl#lockLocation(int)} is driven through the production wiring
 * against an in-memory H2 database, as {@code ConcurrentMoveTest} does. One transaction takes
 * the lock and holds it until told to let go; a second reaches for the same row and is expected
 * to give up well before the first releases it.
 *
 * <p>The time asserted on is generous on purpose. What is being shown is that the configured
 * bound applies at all — H2's own default is 2 seconds, and Postgres's is forever — so a wait
 * that ends in a fraction of that default is the evidence, not the exact figure.
 *
 * <p>Only the holder runs in a transaction; the waiter locks on an auto-commit connection. H2
 * reports the expired wait as a {@link java.sql.SQLTimeoutException}, which HikariCP treats as a
 * broken connection and evicts on the spot, so a transaction around the waiter would fail its
 * rollback on the closed proxy and report that failure instead of the timeout. The Postgres
 * driver reports the same condition as a plain {@code PSQLException} (SQL state {@code 55P03}),
 * which Hikari leaves alone, so the production path through {@code @Transactional} is not
 * subject to that eviction.
 */
class LockWaitBoundTest {

    private static final int LOCATION_ID = 1;
    private static final int LOCK_TIMEOUT_MS = 300;
    private static final long MUST_FAIL_WITHIN_MS = 1500;
    private static final int TIMEOUT_SECONDS = 30;

    private DataSource dataSource;
    private DbInteractions dbInteractions;
    private LocationRepositoryImpl locationRepository;
    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() {
        dataSource = new DataSourceConfig().dataSource(h2Config());
        dbInteractions = new DbInteractions(dataSource);
        locationRepository = new LocationRepositoryImpl(dbInteractions);
        transactionTemplate = new TransactionTemplate(new DataSourceConfig().transactionManager(dataSource));

        dbInteractions.update("DROP TABLE IF EXISTS viron.location");
        dbInteractions.update("CREATE SCHEMA IF NOT EXISTS viron");
        dbInteractions.update("CREATE TABLE viron.location (location_id INT PRIMARY KEY, x INT, y INT)");
        dbInteractions.update("INSERT INTO viron.location (location_id, x, y) VALUES (?, ?, ?)", LOCATION_ID, 0, 0);
    }

    @AfterEach
    void tearDown() {
        ((HikariDataSource) dataSource).close();
    }

    @Test
    void aLockHeldByAnotherTransaction_failsTheWaiterWithinTheConfiguredBound() throws Exception {
        CountDownLatch lockHeld = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ExecutorService holder = Executors.newSingleThreadExecutor();
        try {
            Future<?> holding = holder.submit(() -> transactionTemplate.executeWithoutResult(status -> {
                assertThat(locationRepository.lockLocation(LOCATION_ID)).isTrue();
                lockHeld.countDown();
                await(release);
            }));
            assertThat(lockHeld.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isTrue();

            long started = System.nanoTime();
            assertThatThrownBy(() -> locationRepository.lockLocation(LOCATION_ID))
                    .isInstanceOf(CannotAcquireLockException.class);
            long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);

            assertThat(elapsedMs)
                    .as("a lock wait bounded at %d ms must not run to %d ms", LOCK_TIMEOUT_MS, elapsedMs)
                    .isLessThan(MUST_FAIL_WITHIN_MS);

            release.countDown();
            holding.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } finally {
            release.countDown();
            holder.shutdownNow();
        }
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out waiting to be released");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while holding the lock", e);
        }
    }

    private static DbConfig h2Config() {
        DbConfig config = new DbConfig();
        config.setDbUrl("jdbc:h2:mem:viron_lock_wait_bound;DB_CLOSE_DELAY=-1");
        config.setDbUsername("sa");
        config.setDbPassword("");
        config.setLockTimeoutMs(LOCK_TIMEOUT_MS);
        return config;
    }
}
