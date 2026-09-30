package preponderous.viron.mappers;

import org.mapstruct.Mapper;
import preponderous.viron.dto.LocationDto;
import preponderous.viron.models.Location;

import java.util.List;

@Mapper(config = StrictMapperConfig.class)
public interface LocationMapper {
    LocationDto toDto(Location location);
    Location toLocation(LocationDto dto);
    List<LocationDto> toDtoList(List<Location> locations);
}
