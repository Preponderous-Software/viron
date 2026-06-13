// Copyright (c) 2024 Preponderous Software
// MIT License

package preponderous.viron.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import preponderous.viron.config.DbConfig;

/**
 * Postgres database interactions.
 */
@Component
@Slf4j
public class DbInteractions {
    private Connection connection;
    private final DbConfig dbConfig;

    @Autowired
    public DbInteractions(DbConfig dbConfig) {
        this.dbConfig = dbConfig;
        this.connection = connect();
    }

    /**
     * Execute a parameterized SELECT and return a disconnected result.
     *
     * <p>The query is run through a {@link PreparedStatement} so caller-supplied
     * values are bound as parameters rather than concatenated into SQL. The
     * statement and live result set are closed before returning; the rows are
     * copied into a {@link CachedRowSet} so callers can read them without leaking
     * JDBC resources.
     *
     * @param query  SQL with {@code ?} placeholders for each parameter
     * @param params values to bind to the placeholders, in order
     * @return a disconnected {@link ResultSet} of the rows, or {@code null} on error
     */
    public ResultSet query(String query, Object... params) {
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            bindParameters(statement, params);
            try (ResultSet rs = statement.executeQuery()) {
                CachedRowSet cachedRowSet = RowSetProvider.newFactory().createCachedRowSet();
                cachedRowSet.populate(rs);
                return cachedRowSet;
            }
        } catch (SQLException e) {
            log.error("Error executing query: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Execute a parameterized INSERT/UPDATE/DELETE.
     *
     * <p>Run through a {@link PreparedStatement} (parameters bound, not concatenated)
     * inside a try-with-resources so the statement is always closed.
     *
     * @param query  SQL with {@code ?} placeholders for each parameter
     * @param params values to bind to the placeholders, in order
     * @return {@code true} if at least one row was affected, {@code false} otherwise (including on error)
     */
    public boolean update(String query, Object... params) {
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            bindParameters(statement, params);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("Error executing update: {}", e.getMessage());
        }
        return false;
    }

    private void bindParameters(PreparedStatement statement, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            log.error("Error closing connection: {}", e.getMessage());
        }
    }

    /**
    * Connect to the database.
    * @return Connection
    */
    public Connection connect() {
        try {
            connection = DriverManager.getConnection(dbConfig.getDbUrl(), dbConfig.getDbUsername(), dbConfig.getDbPassword());
        } catch (SQLException e) {
            log.error("Error connecting to the database: {}", e.getMessage());
        }
        return connection;
    }
    
}
