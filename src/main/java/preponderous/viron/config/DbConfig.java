// Copyright (c) 2024 Preponderous Software
// MIT License

package preponderous.viron.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;

/**
 * The {@code database.*} properties: where the database is, how to log in, and the two settings
 * that bound how the pool built from it behaves under contention (#212).
 *
 * <p>The defaults on the last two are what a {@code DbConfig} built by hand (as the tests do)
 * gets; {@code application.properties} sets both explicitly so the deployed values are read off
 * one file rather than inferred from a Java field.
 */
@Setter
@Getter
@ConfigurationProperties("database")
@PropertySource("classpath:application.properties")
public class DbConfig {
    /** HikariCP's own default, restated so a change to it is a deliberate one. */
    public static final int DEFAULT_MAX_POOL_SIZE = 10;
    /** Well under the pool's 30-second connection timeout, so it is this limit that fires first. */
    public static final int DEFAULT_LOCK_TIMEOUT_MS = 5000;

    private String dbUrl;
    private String dbUsername;
    private String dbPassword;
    /** Upper bound on pooled connections; every waiting request holds one for as long as it waits. */
    private int maxPoolSize = DEFAULT_MAX_POOL_SIZE;
    /**
     * Longest a statement waits for a row lock before failing, in milliseconds. {@code 0} leaves
     * the database's own default in place, which for Postgres means waiting indefinitely.
     */
    private int lockTimeoutMs = DEFAULT_LOCK_TIMEOUT_MS;
}
