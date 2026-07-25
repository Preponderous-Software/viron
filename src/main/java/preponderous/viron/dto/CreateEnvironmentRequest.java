package preponderous.viron.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
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
    @Schema(description = "Square shorthand: sets both rows and columns of each grid to this value. "
            + "Ignored when numRows and numColumns are supplied. "
            + "Either gridSize or both numRows and numColumns must be provided.")
    private Integer gridSize;

    @Min(1)
    @Schema(description = "Number of rows in each grid. Must be supplied together with numColumns; "
            + "takes precedence over gridSize.")
    private Integer numRows;

    @Min(1)
    @Schema(description = "Number of columns in each grid. Must be supplied together with numRows; "
            + "takes precedence over gridSize.")
    private Integer numColumns;

    /**
     * Convenience constructor for square grids.
     *
     * @param name     name of the environment to create
     * @param numGrids number of grids to create in the environment
     * @param gridSize rows and columns of each grid
     */
    public CreateEnvironmentRequest(String name, int numGrids, Integer gridSize) {
        this(name, numGrids, gridSize, null, null);
    }

    /**
     * Rows to create each grid with: {@code numRows} when supplied, otherwise {@code gridSize}.
     * Only meaningful once {@link #isGridDimensionsSpecified()} has passed validation.
     */
    @JsonIgnore
    @Schema(hidden = true)
    public int getResolvedNumRows() {
        return numRows != null ? numRows : gridSize;
    }

    /**
     * Columns to create each grid with: {@code numColumns} when supplied, otherwise {@code gridSize}.
     * Only meaningful once {@link #isGridDimensionsSpecified()} has passed validation.
     */
    @JsonIgnore
    @Schema(hidden = true)
    public int getResolvedNumColumns() {
        return numColumns != null ? numColumns : gridSize;
    }

    /**
     * Grid dimensions must be fully specified: either both {@code numRows} and {@code numColumns},
     * or the {@code gridSize} shorthand. Supplying only one of the two dimensions is rejected so a
     * half-specified request never silently falls back to a square grid.
     */
    @JsonIgnore
    @Schema(hidden = true)
    @AssertTrue(message = "either gridSize or both numRows and numColumns must be provided")
    public boolean isGridDimensionsSpecified() {
        if (numRows != null && numColumns != null) {
            return true;
        }
        if (numRows != null || numColumns != null) {
            return false;
        }
        return gridSize != null;
    }
}
