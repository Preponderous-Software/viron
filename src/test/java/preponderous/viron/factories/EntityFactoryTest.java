package preponderous.viron.factories;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import preponderous.viron.database.DbInteractions;
import preponderous.viron.exceptions.EntityCreationException;
import preponderous.viron.models.Entity;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class EntityFactoryTest {

    private static final String NEXT_ID_QUERY = "SELECT nextval('viron.entity_id_seq')";

    @MockBean
    private DbInteractions dbInteractions;

    @Test
    public void testCreateEntity_Success_ReturnsEntity() throws SQLException {
        ResultSet idRs = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.query(NEXT_ID_QUERY)).thenReturn(idRs);
        Mockito.when(idRs.next()).thenReturn(true);
        Mockito.when(idRs.getInt(1)).thenReturn(5);
        Mockito.when(dbInteractions.update(anyString(), any(), any())).thenReturn(true);

        EntityFactory factory = new EntityFactory(dbInteractions);

        Entity result = factory.createEntity("Bob");

        assertThat(result).isNotNull();
        assertThat(result.getEntityId()).isEqualTo(5);
        assertThat(result.getName()).isEqualTo("Bob");
    }

    // #138 case 1: a null ResultSet from a failed nextval query must not NPE.
    @Test
    public void testCreateEntity_NullResultSet_ThrowsAndDoesNotInsert() {
        Mockito.when(dbInteractions.query(NEXT_ID_QUERY)).thenReturn(null);

        EntityFactory factory = new EntityFactory(dbInteractions);

        assertThatThrownBy(() -> factory.createEntity("Bob"))
                .isInstanceOf(EntityCreationException.class)
                .hasMessage("Failed to get next entity id");
        verify(dbInteractions, never()).update(anyString());
    }

    // #138 case 2: a -1 sentinel id must not be inserted.
    @Test
    public void testCreateEntity_NextIdUnavailable_ThrowsAndDoesNotInsert() throws SQLException {
        ResultSet idRs = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.query(NEXT_ID_QUERY)).thenReturn(idRs);
        Mockito.when(idRs.next()).thenReturn(false); // no row -> getNextEntityId() returns -1

        EntityFactory factory = new EntityFactory(dbInteractions);

        assertThatThrownBy(() -> factory.createEntity("Bob"))
                .isInstanceOf(EntityCreationException.class)
                .hasMessage("Failed to get next entity id");
        verify(dbInteractions, never()).update(anyString());
    }

    @Test
    public void testCreateEntity_InsertFails_Throws() throws SQLException {
        ResultSet idRs = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.query(NEXT_ID_QUERY)).thenReturn(idRs);
        Mockito.when(idRs.next()).thenReturn(true);
        Mockito.when(idRs.getInt(1)).thenReturn(5);
        Mockito.when(dbInteractions.update(anyString(), any(), any())).thenReturn(false);

        EntityFactory factory = new EntityFactory(dbInteractions);

        assertThatThrownBy(() -> factory.createEntity("Bob"))
                .isInstanceOf(EntityCreationException.class)
                .hasMessageContaining("Error creating entity with name: Bob");
    }
}
