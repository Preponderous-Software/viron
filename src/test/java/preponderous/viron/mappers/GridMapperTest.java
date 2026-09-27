// Copyright (c) 2024 Preponderous Software
// MIT License

package preponderous.viron.mappers;

import org.junit.jupiter.api.Test;
import preponderous.viron.dto.GridDto;
import preponderous.viron.models.Grid;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// The controller tests mock this mapper, so these exercise the MapStruct-generated implementation
// itself. Rows and columns are given different values so that a swap between them is observable.
public class GridMapperTest {

    private final GridMapper mapper = new GridMapperImpl();

    @Test
    public void testToDto_CopiesEveryField() {
        GridDto dto = mapper.toDto(new Grid(7, 3, 5, "arena"));

        assertThat(dto.getGridId()).isEqualTo(7);
        assertThat(dto.getRows()).isEqualTo(3);
        assertThat(dto.getColumns()).isEqualTo(5);
        assertThat(dto.getName()).isEqualTo("arena");
    }

    @Test
    public void testToDto_UnnamedGrid_KeepsNameNull() {
        GridDto dto = mapper.toDto(new Grid(7, 3, 5, null));

        assertThat(dto.getName()).isNull();
    }

    @Test
    public void testToDto_Null_ReturnsNull() {
        assertThat(mapper.toDto(null)).isNull();
    }

    @Test
    public void testToGrid_CopiesEveryField() {
        Grid grid = mapper.toGrid(new GridDto(7, 3, 5, "arena"));

        assertThat(grid.getGridId()).isEqualTo(7);
        assertThat(grid.getRows()).isEqualTo(3);
        assertThat(grid.getColumns()).isEqualTo(5);
        assertThat(grid.getName()).isEqualTo("arena");
    }

    @Test
    public void testToGrid_Null_ReturnsNull() {
        assertThat(mapper.toGrid(null)).isNull();
    }

    @Test
    public void testToDtoList_PreservesOrder() {
        List<GridDto> dtos = mapper.toDtoList(List.of(new Grid(1, 3, 5, "a"), new Grid(2, 4, 6, "b")));

        assertThat(dtos).containsExactly(new GridDto(1, 3, 5, "a"), new GridDto(2, 4, 6, "b"));
    }

    @Test
    public void testToDtoList_Empty_ReturnsEmpty() {
        assertThat(mapper.toDtoList(Collections.emptyList())).isEmpty();
    }

    @Test
    public void testToDtoList_Null_ReturnsNull() {
        assertThat(mapper.toDtoList(null)).isNull();
    }
}
