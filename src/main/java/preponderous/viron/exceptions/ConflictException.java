package preponderous.viron.exceptions;

/**
 * Thrown when a request cannot be completed because it conflicts with the current
 * state — e.g. moving an entity onto an already-occupied location (a collision).
 * Mapped to HTTP 409 Conflict.
 */
public class ConflictException extends ServiceException {
    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
