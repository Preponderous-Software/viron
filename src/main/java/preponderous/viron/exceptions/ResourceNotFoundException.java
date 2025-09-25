package preponderous.viron.exceptions;

/**
 * Exception thrown when a requested resource cannot be found.
 *
 * <p>This exception is typically thrown when attempting to retrieve an entity by ID that doesn't
 * exist in the system.
 */
public class ResourceNotFoundException extends VironException {

  /**
   * Constructs a new ResourceNotFoundException with the specified detail message.
   *
   * @param message the detail message explaining which resource was not found
   */
  public ResourceNotFoundException(String message) {
    super(message);
  }

  /**
   * Constructs a new ResourceNotFoundException with the specified detail message and cause.
   *
   * @param message the detail message explaining which resource was not found
   * @param cause the cause of this exception
   */
  public ResourceNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
