package preponderous.viron.mappers;

import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

// Shared by every mapper so that a DTO or model property with no counterpart on the other side
// fails the build, instead of compiling with a warning and leaving the field unset in responses.
@MapperConfig(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface StrictMapperConfig {
}
