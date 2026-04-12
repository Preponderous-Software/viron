package preponderous.viron.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data transfer object representing an entity")
public class EntityDto {
    @Schema(description = "Unique identifier of the entity")
    private int entityId;

    @Schema(description = "Name of the entity")
    private String name;

    @Schema(description = "Date when the entity was created")
    private String creationDate;
}
