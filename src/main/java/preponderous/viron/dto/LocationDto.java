package preponderous.viron.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data transfer object representing a location")
public class LocationDto {
    @Schema(description = "Unique identifier of the location")
    private int locationId;

    @Schema(description = "X coordinate of the location")
    private int x;

    @Schema(description = "Y coordinate of the location")
    private int y;
}
