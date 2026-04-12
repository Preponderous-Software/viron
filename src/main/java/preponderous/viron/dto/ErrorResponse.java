package preponderous.viron.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standard error response body")
public class ErrorResponse {
    @Schema(description = "HTTP status code")
    private int status;

    @Schema(description = "Error message describing what went wrong")
    private String message;
}
