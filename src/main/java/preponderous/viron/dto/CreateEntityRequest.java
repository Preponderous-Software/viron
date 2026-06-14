package preponderous.viron.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for creating an entity")
public class CreateEntityRequest {
    @NotBlank
    @Schema(description = "Name of the entity to create")
    private String name;
}
