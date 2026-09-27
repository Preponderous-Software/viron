// Copyright (c) 2024 Preponderous Software
// MIT License

package preponderous.viron.mappers;

import org.junit.jupiter.api.Test;
import preponderous.viron.dto.EntityDto;
import preponderous.viron.models.Entity;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// The controller tests mock this mapper, so these exercise the MapStruct-generated implementation
// itself.
public class EntityMapperTest {

    private final EntityMapper mapper = new EntityMapperImpl();

    @Test
    public void testToDto_CopiesEveryField() {
        EntityDto dto = mapper.toDto(new Entity(4, "Bob", "2024-01-02"));

        assertThat(dto.getEntityId()).isEqualTo(4);
        assertThat(dto.getName()).isEqualTo("Bob");
        assertThat(dto.getCreationDate()).isEqualTo("2024-01-02");
    }

    @Test
    public void testToDto_Null_ReturnsNull() {
        assertThat(mapper.toDto(null)).isNull();
    }

    @Test
    public void testToEntity_CopiesEveryField() {
        Entity entity = mapper.toEntity(new EntityDto(4, "Bob", "2024-01-02"));

        assertThat(entity.getEntityId()).isEqualTo(4);
        assertThat(entity.getName()).isEqualTo("Bob");
        assertThat(entity.getCreationDate()).isEqualTo("2024-01-02");
    }

    @Test
    public void testToEntity_Null_ReturnsNull() {
        assertThat(mapper.toEntity(null)).isNull();
    }

    @Test
    public void testToDtoList_PreservesOrder() {
        List<EntityDto> dtos = mapper.toDtoList(List.of(
                new Entity(1, "Alice", "2024-01-01"),
                new Entity(2, "Bob", "2024-01-02")));

        assertThat(dtos).containsExactly(
                new EntityDto(1, "Alice", "2024-01-01"),
                new EntityDto(2, "Bob", "2024-01-02"));
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
