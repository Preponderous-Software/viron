// Copyright (c) 2024 Preponderous Software
// MIT License

package preponderous.viron.mappers;

import org.junit.jupiter.api.Test;
import preponderous.viron.dto.LocationDto;
import preponderous.viron.models.Location;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// The controller tests mock this mapper, so these exercise the MapStruct-generated implementation
// itself. x and y are given different values so that a swap between them is observable.
public class LocationMapperTest {

    private final LocationMapper mapper = new LocationMapperImpl();

    @Test
    public void testToDto_CopiesEveryField() {
        LocationDto dto = mapper.toDto(new Location(11, 2, 9));

        assertThat(dto.getLocationId()).isEqualTo(11);
        assertThat(dto.getX()).isEqualTo(2);
        assertThat(dto.getY()).isEqualTo(9);
    }

    @Test
    public void testToDto_Null_ReturnsNull() {
        assertThat(mapper.toDto(null)).isNull();
    }

    @Test
    public void testToLocation_CopiesEveryField() {
        Location location = mapper.toLocation(new LocationDto(11, 2, 9));

        assertThat(location.getLocationId()).isEqualTo(11);
        assertThat(location.getX()).isEqualTo(2);
        assertThat(location.getY()).isEqualTo(9);
    }

    @Test
    public void testToLocation_Null_ReturnsNull() {
        assertThat(mapper.toLocation(null)).isNull();
    }

    @Test
    public void testToDtoList_PreservesOrder() {
        List<LocationDto> dtos = mapper.toDtoList(List.of(new Location(1, 0, 1), new Location(2, 1, 0)));

        assertThat(dtos).containsExactly(new LocationDto(1, 0, 1), new LocationDto(2, 1, 0));
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
