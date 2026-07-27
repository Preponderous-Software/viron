// Copyright (c) 2024 Preponderous Software
// MIT License

package preponderous.viron.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
     * Execute a parameterized SELECT and map every returned row.
     *
     * <p>The query is run through a {@link PreparedStatement} so caller-supplied
     * values are bound as parameters rather than concatenated into SQL. The
     * statement and result set are owned here and closed before returning, so no
     * JDBC resource ever reaches the caller.
     *
     * @param query  SQL with {@code ?} placeholders for each parameter
     * @param mapper maps each row to a result object
     * @param params values to bind to the placeholders, in order
     * @param <T>    mapped result type
     * @return the mapped rows in result-set order, or an empty list if the query failed
     */
    public <T> List<T> query(String query, RowMapper<T> mapper, Object... params) {
        List<T> results = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            bindParameters(statement, params);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    results.add(mapper.map(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error executing query: {}", e.getMessage());
            // Discard any partially mapped rows rather than reporting a truncated result.
            return new ArrayList<>();
        }
        return results;
    }

    /**
     * Execute a parameterized SELECT and map its first row, if any.
     *
     * <p>Behaves like {@link #query(String, RowMapper, Object...)} but stops after the
     * first row; any further rows the query happens to return are ignored.
     *
     * @param query  SQL with {@code ?} placeholders for each parameter
     * @param mapper maps the first row to a result object
     * @param params values to bind to the placeholders, in order
     * @param <T>    mapped result type
     * @return the mapped first row, or an empty {@link Optional} if there were no rows or the query failed
     */
    public <T> Optional<T> queryOne(String query, RowMapper<T> mapper, Object... params) {
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            bindParameters(statement, params);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.ofNullable(mapper.map(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error executing query: {}", e.getMessage());
        }
        return Optional.empty();
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
