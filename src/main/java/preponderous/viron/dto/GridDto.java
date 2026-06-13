package preponderous.viron.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data transfer object representing a grid")
public class GridDto {
    @Schema(description = "Unique identifier of the grid")
    private int gridId;

    @Schema(description = "Number of rows in the grid")
    private int rows;

    @Schema(description = "Number of columns in the grid")
    private int columns;
}
