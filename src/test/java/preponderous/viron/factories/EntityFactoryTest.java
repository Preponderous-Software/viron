package preponderous.viron.factories;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import preponderous.viron.database.DbInteractions;
import preponderous.viron.exceptions.EntityCreationException;
import preponderous.viron.models.Entity;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class EntityFactoryTest {

    private static final String NEXT_ID_QUERY = "SELECT nextval('viron.entity_id_seq')";

    @MockBean
    private DbInteractions dbInteractions;

    @Test
    public void testCreateEntity_Success_ReturnsEntity() {
        Mockito.when(dbInteractions.<Integer>queryOne(eq(NEXT_ID_QUERY), any())).thenReturn(Optional.of(5));
        Mockito.when(dbInteractions.update(anyString(), any(), any())).thenReturn(true);

        EntityFactory factory = new EntityFactory(dbInteractions);

        Entity result = factory.createEntity("Bob");

        assertThat(result).isNotNull();
        assertThat(result.getEntityId()).isEqualTo(5);
        assertThat(result.getName()).isEqualTo("Bob");
    }

    // #138: a nextval query that yields no id (failed query or no row) must not be inserted
    // as a -1 sentinel. DbInteractions reports both cases as an empty Optional.
    @Test
    public void testCreateEntity_NextIdUnavailable_ThrowsAndDoesNotInsert() {
        Mockito.when(dbInteractions.<Integer>queryOne(eq(NEXT_ID_QUERY), any())).thenReturn(Optional.empty());

        EntityFactory factory = new EntityFactory(dbInteractions);

        assertThatThrownBy(() -> factory.createEntity("Bob"))
                .isInstanceOf(EntityCreationException.class)
                .hasMessage("Failed to get next entity id");
        verify(dbInteractions, never()).update(anyString(), any(), any());
    }

    @Test
    public void testCreateEntity_InsertFails_Throws() {
        Mockito.when(dbInteractions.<Integer>queryOne(eq(NEXT_ID_QUERY), any())).thenReturn(Optional.of(5));
        Mockito.when(dbInteractions.update(anyString(), any(), any())).thenReturn(false);

        EntityFactory factory = new EntityFactory(dbInteractions);

        assertThatThrownBy(() -> factory.createEntity("Bob"))
                .isInstanceOf(EntityCreationException.class)
                .hasMessageContaining("Error creating entity with name: Bob");
    }
}
