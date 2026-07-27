package preponderous.viron.database;

import org.mockito.stubbing.Answer;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Mockito answers for stubbing {@link DbInteractions#query(String, RowMapper, Object...)} and
 * {@link DbInteractions#queryOne(String, RowMapper, Object...)}.
 *
 * <p>Each answer drives the {@link RowMapper} the caller passed over a mocked {@link ResultSet},
 * standing in for the iteration the real {@code DbInteractions} performs. That keeps a
 * repository's row-mapping covered by its own tests even though the cursor itself is no longer
 * visible to the repository.
 *
 * <p>Failure handling (a null cursor, a {@link java.sql.SQLException} mid-iteration) now lives in
 * {@code DbInteractions} and is covered by {@code DbInteractionsTest} against H2; repositories
 * only see the empty {@link List}/{@link Optional} that results, so stub those directly.
 */
public final class ResultSetAnswers {

    private ResultSetAnswers() {
    }

    /** Maps every row of {@code rs}, as {@code DbInteractions.query} would. */
    public static <T> Answer<List<T>> mapsAllRows(ResultSet rs) {
        return invocation -> {
            RowMapper<T> mapper = invocation.getArgument(1);
            List<T> results = new ArrayList<>();
            while (rs.next()) {
                results.add(mapper.map(rs));
            }
            return results;
        };
    }

    /** Maps the first row of {@code rs}, if any, as {@code DbInteractions.queryOne} would. */
    public static <T> Answer<Optional<T>> mapsFirstRow(ResultSet rs) {
        return invocation -> {
            RowMapper<T> mapper = invocation.getArgument(1);
            return rs.next() ? Optional.ofNullable(mapper.map(rs)) : Optional.empty();
        };
    }
}
