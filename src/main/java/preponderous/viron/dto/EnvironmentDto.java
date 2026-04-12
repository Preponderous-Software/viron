package preponderous.viron.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data transfer object representing an environment")
public class EnvironmentDto {
    @Schema(description = "Unique identifier of the environment")
    private int environmentId;

    @Schema(description = "Name of the environment")
    private String name;

    @Schema(description = "Date when the environment was created")
    private String creationDate;
}
