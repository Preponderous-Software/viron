package preponderous.viron.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for updating an entity's name")
public class UpdateEntityNameRequest {
    @NotBlank
    @Schema(description = "New name for the entity")
    private String name;
}
