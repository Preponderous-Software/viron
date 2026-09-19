// Copyright (c) 2024 Preponderous Software
// MIT License

package preponderous.viron.config;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the pooled {@link DataSource} introduced for #194: that it is built from
 * {@link DbConfig}, that it does not contact the database until first use, and that it is the
 * only {@link DataSource} in the context (so Spring Boot's auto-configured one has backed off).
 */
@SpringBootTest
class DataSourceConfigTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void dataSourceIsAPoolBuiltFromDbConfig() {
        DbConfig config = new DbConfig();
        config.setDbUrl("jdbc:h2:mem:viron_datasourceconfig");
        config.setDbUsername("sa");
        config.setDbPassword("secret");

        HikariDataSource dataSource = (HikariDataSource) new DataSourceConfig().dataSource(config);
        try {
            assertThat(dataSource.getJdbcUrl()).isEqualTo("jdbc:h2:mem:viron_datasourceconfig");
            assertThat(dataSource.getUsername()).isEqualTo("sa");
            assertThat(dataSource.getPassword()).isEqualTo("secret");
            assertThat(dataSource.getPoolName()).isEqualTo("viron-pool");
        } finally {
            dataSource.close();
        }
    }

    // #212: the pool's size and the lock wait it is drawn against are both taken from DbConfig,
    // and the wait is applied as a session setting on every connection the pool opens.
    @Test
    void poolSizeAndLockTimeoutAreTakenFromDbConfig() {
        DbConfig config = new DbConfig();
        config.setDbUrl("jdbc:h2:mem:viron_datasourceconfig_bounds");
        config.setDbUsername("sa");
        config.setDbPassword("");
        config.setMaxPoolSize(4);
        config.setLockTimeoutMs(750);

        HikariDataSource dataSource = (HikariDataSource) new DataSourceConfig().dataSource(config);
        try {
            assertThat(dataSource.getMaximumPoolSize()).isEqualTo(4);
            assertThat(dataSource.getConnectionInitSql()).isEqualTo("SET lock_timeout = 750");
        } finally {
            dataSource.close();
        }
    }

    // A DbConfig left at its defaults still bounds the wait and still states the pool size.
    @Test
    void defaultsBoundTheLockWaitAndStateThePoolSize() {
        DbConfig config = new DbConfig();
        config.setDbUrl("jdbc:h2:mem:viron_datasourceconfig_defaults");
        config.setDbUsername("sa");
        config.setDbPassword("");

        HikariDataSource dataSource = (HikariDataSource) new DataSourceConfig().dataSource(config);
        try {
            assertThat(dataSource.getMaximumPoolSize()).isEqualTo(DbConfig.DEFAULT_MAX_POOL_SIZE);
            assertThat(dataSource.getConnectionInitSql())
                    .isEqualTo("SET lock_timeout = " + DbConfig.DEFAULT_LOCK_TIMEOUT_MS);
            assertThat(DbConfig.DEFAULT_LOCK_TIMEOUT_MS)
                    .as("the lock wait must expire before the pool's connection timeout does")
                    .isLessThan((int) dataSource.getConnectionTimeout());
        } finally {
            dataSource.close();
        }
    }

    // Zero opts out: no session setting is sent, so the database's own default applies.
    @Test
    void zeroLockTimeoutLeavesTheDatabaseDefaultInPlace() {
        DbConfig config = new DbConfig();
        config.setDbUrl("jdbc:h2:mem:viron_datasourceconfig_unbounded");
        config.setDbUsername("sa");
        config.setDbPassword("");
        config.setLockTimeoutMs(0);

        HikariDataSource dataSource = (HikariDataSource) new DataSourceConfig().dataSource(config);
        try {
            assertThat(dataSource.getConnectionInitSql()).isNull();
        } finally {
            dataSource.close();
        }
    }

    // The deployed values are bound from application.properties (the test copy sets values that
    // differ from the Java defaults for exactly this reason) and reach the pool.
    @Test
    void contextBindsThePoolBoundsFromProperties() {
        DbConfig bound = applicationContext.getBean(DbConfig.class);
        assertThat(bound.getMaxPoolSize()).isEqualTo(4);
        assertThat(bound.getLockTimeoutMs()).isEqualTo(2500);

        HikariDataSource dataSource = (HikariDataSource) applicationContext.getBean(DataSource.class);
        assertThat(dataSource.getMaximumPoolSize()).isEqualTo(4);
        assertThat(dataSource.getConnectionInitSql()).isEqualTo("SET lock_timeout = 2500");
    }

    // Pool start-up is deferred so the context still starts when the database is unreachable.
    @Test
    void poolIsNotStartedUntilFirstConnectionIsRequested() {
        DbConfig config = new DbConfig();
        config.setDbUrl("jdbc:h2:mem:viron_datasourceconfig_lazy");
        config.setDbUsername("sa");
        config.setDbPassword("");

        HikariDataSource dataSource = (HikariDataSource) new DataSourceConfig().dataSource(config);
        try {
            assertThat(dataSource.getHikariPoolMXBean()).isNull();
        } finally {
            dataSource.close();
        }
    }

    @Test
    void contextExposesExactlyOnePooledDataSource() {
        assertThat(applicationContext.getBeanNamesForType(DataSource.class)).hasSize(1);
        assertThat(applicationContext.getBean(DataSource.class)).isInstanceOf(HikariDataSource.class);
    }
}
