package preponderous.viron.mappers;

import org.mapstruct.Mapper;
import preponderous.viron.dto.PlacementDto;
import preponderous.viron.models.Placement;

import java.util.List;

@Mapper(config = StrictMapperConfig.class)
public interface PlacementMapper {
    PlacementDto toDto(Placement placement);
    List<PlacementDto> toDtoList(List<Placement> placements);
}
