package preponderous.viron.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for error responses.
 *
 * <p>This DTO provides a consistent structure for error responses across all API endpoints,
 * including timestamp, status code, error message, and request path.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponseDto {

  /** Timestamp when the error occurred. */
  private LocalDateTime timestamp;

  /** HTTP status code. */
  private int status;

  /** Brief error description. */
  private String error;

  /** Detailed error message. */
  private String message;

  /** Request path where error occurred. */
  private String path;
}
