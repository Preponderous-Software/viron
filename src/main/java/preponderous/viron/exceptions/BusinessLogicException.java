package preponderous.viron.exceptions;

/**
 * Exception thrown when business logic constraints are violated.
 *
 * <p>This exception is used for cases where the request is technically valid but violates business
 * rules, such as attempting to place an entity in a location that already contains another entity.
 */
public class BusinessLogicException extends VironException {

  /**
   * Constructs a new BusinessLogicException with the specified detail message.
   *
   * @param message the detail message describing the business rule violation
   */
  public BusinessLogicException(String message) {
    super(message);
  }

  /**
   * Constructs a new BusinessLogicException with the specified detail message and cause.
   *
   * @param message the detail message describing the business rule violation
   * @param cause the cause of this exception
   */
  public BusinessLogicException(String message, Throwable cause) {
    super(message, cause);
  }
}
