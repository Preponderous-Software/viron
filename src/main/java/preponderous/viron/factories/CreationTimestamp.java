package preponderous.viron.factories;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * The moment a row is created, in the two forms a factory needs: a value to bind to a
 * {@code TIMESTAMP} column, and the text a read of that column answers with. Both come from one
 * instant, truncated to the second, so a create and a later read of the same row report the same
 * date.
 */
final class CreationTimestamp {
    private static final DateTimeFormatter AS_READ_FROM_THE_DATABASE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final LocalDateTime moment;

    private CreationTimestamp(LocalDateTime moment) {
        this.moment = moment;
    }

    static CreationTimestamp now() {
        return new CreationTimestamp(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
    }

    Timestamp toColumnValue() {
        return Timestamp.valueOf(moment);
    }

    @Override
    public String toString() {
        return AS_READ_FROM_THE_DATABASE.format(moment);
    }
}
