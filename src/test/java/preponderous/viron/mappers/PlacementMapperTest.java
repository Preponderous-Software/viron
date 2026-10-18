// Copyright (c) 2024 Preponderous Software
// MIT License

package preponderous.viron.mappers;

import org.junit.jupiter.api.Test;
import preponderous.viron.dto.PlacementDto;
import preponderous.viron.models.Placement;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// Exercises the MapStruct-generated implementation itself. Every field is given a distinct value so
// that a swap between any two of them is observable.
public class PlacementMapperTest {

    private final PlacementMapper mapper = new PlacementMapperImpl();

    @Test
    public void testToDto_CopiesEveryField() {
        PlacementDto dto = mapper.toDto(new Placement(1, 7, 11, 110, 5, 6));

        assertThat(dto.getEntityId()).isEqualTo(1);
        assertThat(dto.getEnvironmentId()).isEqualTo(7);
        assertThat(dto.getGridId()).isEqualTo(11);
        assertThat(dto.getLocationId()).isEqualTo(110);
        assertThat(dto.getX()).isEqualTo(5);
        assertThat(dto.getY()).isEqualTo(6);
    }

    @Test
    public void testToDto_Null_ReturnsNull() {
        assertThat(mapper.toDto(null)).isNull();
    }

    @Test
    public void testToDtoList_PreservesOrder() {
        List<PlacementDto> dtos = mapper.toDtoList(List.of(
                new Placement(2, 7, 10, 100, 1, 2),
                new Placement(1, 8, 20, 200, 3, 4)));

        assertThat(dtos).containsExactly(
                new PlacementDto(2, 7, 10, 100, 1, 2),
                new PlacementDto(1, 8, 20, 200, 3, 4));
    }

    @Test
    public void testToDtoList_Empty_ReturnsEmpty() {
        assertThat(mapper.toDtoList(Collections.emptyList())).isEmpty();
    }
}
