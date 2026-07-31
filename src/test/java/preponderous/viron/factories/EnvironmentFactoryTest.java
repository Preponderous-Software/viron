package preponderous.viron.factories;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import preponderous.viron.database.DbInteractions;
import preponderous.viron.exceptions.EnvironmentCreationException;
import preponderous.viron.models.Environment;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class EnvironmentFactoryTest {

    private static final String NEXT_ENVIRONMENT_ID_QUERY = "SELECT nextval('viron.environment_id_seq')";
    private static final String NEXT_GRID_ID_QUERY = "SELECT nextval('viron.grid_id_seq')";
    private static final String NEXT_LOCATION_ID_QUERY = "SELECT nextval('viron.location_id_seq')";

    private static final String ENVIRONMENT_INSERT = "INSERT INTO viron.environment (environment_id, name, creation_date) VALUES (?, ?, ?)";
    private static final String GRID_INSERT = "INSERT INTO viron.grid (grid_id, rows, columns) VALUES (?, ?, ?)";
    private static final String GRID_ENVIRONMENT_ASSOCIATION_INSERT = "INSERT INTO viron.grid_environment (grid_id, environment_id) VALUES (?, ?)";
    private static final String LOCATION_INSERT = "INSERT INTO viron.location (location_id, x, y) VALUES (?, ?, ?)";
    private static final String LOCATION_GRID_ASSOCIATION_INSERT = "INSERT INTO viron.location_grid (location_id, grid_id) VALUES (?, ?)";

    @MockBean
    private DbInteractions dbInteractions;

    private void stubHappyPathUpTo(String... queriesToSucceed) {
        for (String query : queriesToSucceed) {
            Mockito.when(dbInteractions.update(eq(query), any(), any(), any())).thenReturn(true);
            Mockito.when(dbInteractions.update(eq(query), any(), any())).thenReturn(true);
        }
    }

    @Test
    public void testCreateEnvironment_Success_ReturnsEnvironment() {
        Mockito.when(dbInteractions.<Integer>queryOne(eq(NEXT_ENVIRONMENT_ID_QUERY), any())).thenReturn(Optional.of(10));
        Mockito.when(dbInteractions.<Integer>queryOne(eq(NEXT_GRID_ID_QUERY), any())).thenReturn(Optional.of(20));
        Mockito.when(dbInteractions.<Integer>queryOne(eq(NEXT_LOCATION_ID_QUERY), any())).thenReturn(Optional.of(30));
        stubHappyPathUpTo(ENVIRONMENT_INSERT, GRID_INSERT, GRID_ENVIRONMENT_ASSOCIATION_INSERT, LOCATION_INSERT, LOCATION_GRID_ASSOCIATION_INSERT);

        EnvironmentFactory factory = new EnvironmentFactory(dbInteractions);

        Environment result = factory.createEnvironment("TestEnv", 1, 1, 1);

        assertThat(result).isNotNull();
        assertThat(result.getEnvironmentId()).isEqualTo(10);
        assertThat(result.getName()).isEqualTo("TestEnv");
        assertThat(result.getCreationDate()).isNotNull();

        verify(dbInteractions).update(eq(ENVIRONMENT_INSERT), eq(10), eq("TestEnv"), any());
        verify(dbInteractions).update(eq(GRID_INSERT), eq(20), eq(1), eq(1));
        verify(dbInteractions).update(eq(GRID_ENVIRONMENT_ASSOCIATION_INSERT), eq(20), eq(10));
        verify(dbInteractions).update(eq(LOCATION_INSERT), eq(30), eq(0), eq(0));
        verify(dbInteractions).update(eq(LOCATION_GRID_ASSOCIATION_INSERT), eq(30), eq(20));
    }

    @Test
    public void testCreateEnvironment_NextEnvironmentIdUnavailable_ThrowsAndDoesNotInsert() {
        Mockito.when(dbInteractions.<Integer>queryOne(eq(NEXT_ENVIRONMENT_ID_QUERY), any())).thenReturn(Optional.empty());

        EnvironmentFactory factory = new EnvironmentFactory(dbInteractions);

        assertThatThrownBy(() -> factory.createEnvironment("TestEnv", 1, 1, 1))
                .isInstanceOf(EnvironmentCreationException.class)
                .hasMessage("Failed to get next environment id");
        verify(dbInteractions, never()).update(eq(ENVIRONMENT_INSERT), any(), any(), any());
    }

    @Test
    public void testCreateEnvironment_EnvironmentInsertFails_Throws() {
        Mockito.when(dbInteractions.<Integer>queryOne(eq(NEXT_ENVIRONMENT_ID_QUERY), any())).thenReturn(Optional.of(10));
        Mockito.when(dbInteractions.update(eq(ENVIRONMENT_INSERT), any(), any(), any())).thenReturn(false);

        EnvironmentFactory factory = new EnvironmentFactory(dbInteractions);

        assertThatThrownBy(() -> factory.createEnvironment("TestEnv", 1, 1, 1))
                .isInstanceOf(EnvironmentCreationException.class)
                .hasMessage("Failed to create environment");
        verify(dbInteractions, never()).update(eq(GRID_INSERT), any(), any(), any());
    }

    @Test
    public void testCreateEnvironment_NextGridIdUnavailable_ThrowsAndDoesNotInsertGrid() {
        Mockito.when(dbInteractions.<Integer>queryOne(eq(NEXT_ENVIRONMENT_ID_QUERY), any())).thenReturn(Optional.of(10));
        Mockito.when(dbInteractions.<Integer>queryOne(eq(NEXT_GRID_ID_QUERY), any())).thenReturn(Optional.empty());
        stubHappyPathUpTo(ENVIRONMENT_INSERT);

        EnvironmentFactory factory = new EnvironmentFactory(dbInteractions);

        assertThatThrownBy(() -> factory.createEnvironment("TestEnv", 1, 1, 1))
                .isInstanceOf(EnvironmentCreationException.class)
                .hasMessage("Failed to get next grid id");
        verify(dbInteractions, never()).update(eq(GRID_INSERT), any(), any(), any());
    }

    @Test
    public void testCreateEnvironment_GridInsertFails_Throws() {
        Mockito.when(dbInteractions.<Integer>queryOne(eq(NEXT_ENVIRONMENT_ID_QUERY), any())).thenReturn(Optional.of(10));
        Mockito.when(dbInteractions.<Integer>queryOne(eq(NEXT_GRID_ID_QUERY), any())).thenReturn(Optional.of(20));
        stubHappyPathUpTo(ENVIRONMENT_INSERT);
        Mockito.when(dbInteractions.update(eq(GRID_INSERT), any(), any(), any())).thenReturn(false);

        EnvironmentFactory factory = new EnvironmentFactory(dbInteractions);

        assertThatThrownBy(() -> factory.createEnvironment("TestEnv", 1, 1, 1))
                .isInstanceOf(EnvironmentCreationException.class)
                .hasMessage("Failed to create grid");
        verify(dbInteractions, never()).update(eq(GRID_ENVIRONMENT_ASSOCIATION_INSERT), any(), any());
    }

    @Test
    public void testCreateEnvironment_GridEnvironmentAssociationFails_Throws() {
        Mockito.when(dbInteractions.<Integer>queryOne(eq(NEXT_ENVIRONMENT_ID_QUERY), any())).thenReturn(Optional.of(10));
        Mockito.when(dbInteractions.<Integer>queryOne(eq(NEXT_GRID_ID_QUERY), any())).thenReturn(Optional.of(20));
        stubHappyPathUpTo(ENVIRONMENT_INSERT, GRID_INSERT);
        Mockito.when(dbInteractions.update(eq(GRID_ENVIRONMENT_ASSOCIATION_INSERT), any(), any())).thenReturn(false);

        EnvironmentFactory factory = new EnvironmentFactory(dbInteractions);

        assertThatThrownBy(() -> factory.createEnvironment("TestEnv", 1, 1, 1))
                .isInstanceOf(EnvironmentCreationException.class)
                .hasMessage("Failed to associate grid with environment");
        verify(dbInteractions, never()).update(eq(LOCATION_INSERT), any(), any(), any());
    }

    @Test
    public void testCreateEnvironment_NextLocationIdUnavailable_ThrowsAndDoesNotInsertLocation() {
        Mockito.when(dbInteractions.<Integer>queryOne(eq(NEXT_ENVIRONMENT_ID_QUERY), any())).thenReturn(Optional.of(10));
        Mockito.when(dbInteractions.<Integer>queryOne(eq(NEXT_GRID_ID_QUERY), any())).thenReturn(Optional.of(20));
        Mockito.when(dbInteractions.<Integer>queryOne(eq(NEXT_LOCATION_ID_QUERY), any())).thenReturn(Optional.empty());
        stubHappyPathUpTo(ENVIRONMENT_INSERT, GRID_INSERT, GRID_ENVIRONMENT_ASSOCIATION_INSERT);

        EnvironmentFactory factory = new EnvironmentFactory(dbInteractions);

        assertThatThrownBy(() -> factory.createEnvironment("TestEnv", 1, 1, 1))
                .isInstanceOf(EnvironmentCreationException.class)
                .hasMessage("Failed to get next location id");
        verify(dbInteractions, never()).update(eq(LOCATION_INSERT), any(), any(), any());
    }

    @Test
    public void testCreateEnvironment_LocationInsertFails_Throws() {
        Mockito.when(dbInteractions.<Integer>queryOne(eq(NEXT_ENVIRONMENT_ID_QUERY), any())).thenReturn(Optional.of(10));
        Mockito.when(dbInteractions.<Integer>queryOne(eq(NEXT_GRID_ID_QUERY), any())).thenReturn(Optional.of(20));
        Mockito.when(dbInteractions.<Integer>queryOne(eq(NEXT_LOCATION_ID_QUERY), any())).thenReturn(Optional.of(30));
        stubHappyPathUpTo(ENVIRONMENT_INSERT, GRID_INSERT, GRID_ENVIRONMENT_ASSOCIATION_INSERT);
        Mockito.when(dbInteractions.update(eq(LOCATION_INSERT), any(), any(), any())).thenReturn(false);

        EnvironmentFactory factory = new EnvironmentFactory(dbInteractions);

        assertThatThrownBy(() -> factory.createEnvironment("TestEnv", 1, 1, 1))
                .isInstanceOf(EnvironmentCreationException.class)
                .hasMessage("Failed to create location");
        verify(dbInteractions, never()).update(eq(LOCATION_GRID_ASSOCIATION_INSERT), any(), any());
    }

    @Test
    public void testCreateEnvironment_LocationGridAssociationFails_Throws() {
        Mockito.when(dbInteractions.<Integer>queryOne(eq(NEXT_ENVIRONMENT_ID_QUERY), any())).thenReturn(Optional.of(10));
        Mockito.when(dbInteractions.<Integer>queryOne(eq(NEXT_GRID_ID_QUERY), any())).thenReturn(Optional.of(20));
        Mockito.when(dbInteractions.<Integer>queryOne(eq(NEXT_LOCATION_ID_QUERY), any())).thenReturn(Optional.of(30));
        stubHappyPathUpTo(ENVIRONMENT_INSERT, GRID_INSERT, GRID_ENVIRONMENT_ASSOCIATION_INSERT, LOCATION_INSERT);
        Mockito.when(dbInteractions.update(eq(LOCATION_GRID_ASSOCIATION_INSERT), any(), any())).thenReturn(false);

        EnvironmentFactory factory = new EnvironmentFactory(dbInteractions);

        assertThatThrownBy(() -> factory.createEnvironment("TestEnv", 1, 1, 1))
                .isInstanceOf(EnvironmentCreationException.class)
                .hasMessage("Failed to associate location with grid");
    }
}
