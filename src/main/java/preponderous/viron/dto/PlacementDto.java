package preponderous.viron.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data transfer object representing where a placed entity stands")
public class PlacementDto {
    @Schema(description = "Unique identifier of the placed entity")
    private int entityId;

    @Schema(description = "Environment containing the grid the entity is placed in")
    private int environmentId;

    @Schema(description = "Grid containing the location the entity is placed at")
    private int gridId;

    @Schema(description = "Location the entity is placed at")
    private int locationId;

    @Schema(description = "X coordinate of the location")
    private int x;

    @Schema(description = "Y coordinate of the location")
    private int y;
}
