package preponderous.viron.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for creating an environment")
public class CreateEnvironmentRequest {
    @NotBlank
    @Schema(description = "Name of the environment to create")
    private String name;

    @Min(1)
    @Schema(description = "Number of grids to create in the environment")
    private int numGrids;

    @Min(1)
    @Schema(description = "Size (rows and columns) of each grid")
    private int gridSize;
}
