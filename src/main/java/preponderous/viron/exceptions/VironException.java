package preponderous.viron.exceptions;

/**
 * Base exception class for all Viron-specific exceptions.
 *
 * <p>This class serves as the root of the Viron exception hierarchy, providing a common base for
 * all custom exceptions in the application.
 */
public abstract class VironException extends RuntimeException {

  /**
   * Constructs a new VironException with the specified detail message.
   *
   * @param message the detail message explaining the reason for the exception
   */
  protected VironException(String message) {
    super(message);
  }

  /**
   * Constructs a new VironException with the specified detail message and cause.
   *
   * @param message the detail message explaining the reason for the exception
   * @param cause the cause of this exception
   */
  protected VironException(String message, Throwable cause) {
    super(message, cause);
  }
}
