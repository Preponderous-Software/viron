// Copyright (c) 2024 Preponderous Software
// MIT License

package preponderous.viron.database;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Maps the row a {@link ResultSet} is currently positioned on to a domain object.
 *
 * <p>Implementations must read only from the current row and must not advance the
 * cursor — {@link DbInteractions} owns the iteration and closes the underlying JDBC
 * resources.
 *
 * @param <T> type produced for each row
 */
@FunctionalInterface
public interface RowMapper<T> {

    /**
     * Map the current row of {@code rs}.
     *
     * @param rs result set positioned on the row to map
     * @return the mapped object
     * @throws SQLException if a column cannot be read
     */
    T map(ResultSet rs) throws SQLException;
}
