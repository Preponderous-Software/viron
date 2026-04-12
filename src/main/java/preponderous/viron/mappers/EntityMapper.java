package preponderous.viron.mappers;

import org.mapstruct.Mapper;
import preponderous.viron.dto.EntityDto;
import preponderous.viron.models.Entity;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EntityMapper {
    EntityDto toDto(Entity entity);
    Entity toEntity(EntityDto dto);
    List<EntityDto> toDtoList(List<Entity> entities);
}
