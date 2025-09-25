package preponderous.viron.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import preponderous.viron.dto.ErrorResponseDto;

/**
 * Global exception handler for the Viron application.
 *
 * <p>This class provides centralized exception handling across the entire application, ensuring
 * consistent error response formats and appropriate HTTP status codes.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  /**
   * Handles ResourceNotFoundException and returns a 404 Not Found response.
   *
   * @param ex the ResourceNotFoundException
   * @param request the HTTP request that caused the exception
   * @return ResponseEntity with error details and 404 status
   */
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponseDto> handleResourceNotFound(
      ResourceNotFoundException ex, HttpServletRequest request) {
    log.warn("Resource not found: {}", ex.getMessage());

    ErrorResponseDto errorResponse =
        new ErrorResponseDto(
            LocalDateTime.now(),
            HttpStatus.NOT_FOUND.value(),
            "Not Found",
            ex.getMessage(),
            request.getRequestURI());

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
  }

  /**
   * Handles BusinessLogicException and returns a 400 Bad Request response.
   *
   * @param ex the BusinessLogicException
   * @param request the HTTP request that caused the exception
   * @return ResponseEntity with error details and 400 status
   */
  @ExceptionHandler(BusinessLogicException.class)
  public ResponseEntity<ErrorResponseDto> handleBusinessLogicException(
      BusinessLogicException ex, HttpServletRequest request) {
    log.warn("Business logic violation: {}", ex.getMessage());

    ErrorResponseDto errorResponse =
        new ErrorResponseDto(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            ex.getMessage(),
            request.getRequestURI());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  /**
   * Handles validation errors from request body validation.
   *
   * @param ex the MethodArgumentNotValidException
   * @param request the HTTP request that caused the exception
   * @return ResponseEntity with error details and 400 status
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponseDto> handleValidationException(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    log.warn("Validation error: {}", ex.getMessage());

    String message = "Validation failed";
    if (ex.getBindingResult().hasFieldErrors()) {
      message =
          ex.getBindingResult().getFieldErrors().stream()
              .map(error -> error.getField() + ": " + error.getDefaultMessage())
              .reduce((a, b) -> a + "; " + b)
              .orElse("Validation failed");
    }

    ErrorResponseDto errorResponse =
        new ErrorResponseDto(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            message,
            request.getRequestURI());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  /**
   * Handles all other unexpected exceptions and returns a 500 Internal Server Error response.
   *
   * @param ex the unexpected exception
   * @param request the HTTP request that caused the exception
   * @return ResponseEntity with error details and 500 status
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponseDto> handleGenericException(
      Exception ex, HttpServletRequest request) {
    log.error("Unexpected error occurred", ex);

    ErrorResponseDto errorResponse =
        new ErrorResponseDto(
            LocalDateTime.now(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "An unexpected error occurred. Please try again later.",
            request.getRequestURI());

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
  }
}
