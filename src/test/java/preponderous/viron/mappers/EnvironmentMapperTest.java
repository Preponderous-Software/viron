// Copyright (c) 2024 Preponderous Software
// MIT License

package preponderous.viron.mappers;

import org.junit.jupiter.api.Test;
import preponderous.viron.dto.EnvironmentDto;
import preponderous.viron.models.Environment;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// The controller tests mock this mapper, so these exercise the MapStruct-generated implementation
// itself.
public class EnvironmentMapperTest {

    private final EnvironmentMapper mapper = new EnvironmentMapperImpl();

    @Test
    public void testToDto_CopiesEveryField() {
        EnvironmentDto dto = mapper.toDto(new Environment(3, "Earth", "2024-01-02"));

        assertThat(dto.getEnvironmentId()).isEqualTo(3);
        assertThat(dto.getName()).isEqualTo("Earth");
        assertThat(dto.getCreationDate()).isEqualTo("2024-01-02");
    }

    @Test
    public void testToDto_Null_ReturnsNull() {
        assertThat(mapper.toDto(null)).isNull();
    }

    @Test
    public void testToEnvironment_CopiesEveryField() {
        Environment environment = mapper.toEnvironment(new EnvironmentDto(3, "Earth", "2024-01-02"));

        assertThat(environment.getEnvironmentId()).isEqualTo(3);
        assertThat(environment.getName()).isEqualTo("Earth");
        assertThat(environment.getCreationDate()).isEqualTo("2024-01-02");
    }

    @Test
    public void testToEnvironment_Null_ReturnsNull() {
        assertThat(mapper.toEnvironment(null)).isNull();
    }

    @Test
    public void testToDtoList_PreservesOrder() {
        List<EnvironmentDto> dtos = mapper.toDtoList(List.of(
                new Environment(1, "Earth", "2024-01-01"),
                new Environment(2, "Mars", "2024-01-02")));

        assertThat(dtos).containsExactly(
                new EnvironmentDto(1, "Earth", "2024-01-01"),
                new EnvironmentDto(2, "Mars", "2024-01-02"));
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
