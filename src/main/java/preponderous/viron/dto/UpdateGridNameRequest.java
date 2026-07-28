package preponderous.viron.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for updating a grid's name")
public class UpdateGridNameRequest {
    @NotBlank
    @Schema(description = "New name for the grid")
    private String name;
}
