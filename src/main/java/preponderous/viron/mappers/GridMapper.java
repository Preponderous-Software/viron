package preponderous.viron.mappers;

import org.mapstruct.Mapper;
import preponderous.viron.dto.GridDto;
import preponderous.viron.models.Grid;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GridMapper {
    GridDto toDto(Grid grid);
    Grid toGrid(GridDto dto);
    List<GridDto> toDtoList(List<Grid> grids);
}
