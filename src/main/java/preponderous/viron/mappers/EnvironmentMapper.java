package preponderous.viron.mappers;

import org.mapstruct.Mapper;
import preponderous.viron.dto.EnvironmentDto;
import preponderous.viron.models.Environment;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EnvironmentMapper {
    EnvironmentDto toDto(Environment environment);
    Environment toEnvironment(EnvironmentDto dto);
    List<EnvironmentDto> toDtoList(List<Environment> environments);
}
