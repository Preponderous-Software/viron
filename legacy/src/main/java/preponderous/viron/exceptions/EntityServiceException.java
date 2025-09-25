package preponderous.viron.exceptions;

public class EntityServiceException extends ServiceException {
    public EntityServiceException(String message) {
        super(message);
    }

    public EntityServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
