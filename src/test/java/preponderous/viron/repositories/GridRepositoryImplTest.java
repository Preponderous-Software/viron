package preponderous.viron.repositories;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import preponderous.viron.database.DbInteractions;
import preponderous.viron.models.Grid;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class GridRepositoryImplTest {

    @MockBean
    private DbInteractions dbInteractions;

    // ---- findAll ----

    @Test
    public void testFindAll_ReturnsGridsWhenResultSetIsNotEmpty() throws SQLException {
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.query("SELECT * FROM viron.grid")).thenReturn(mockResultSet);
        Mockito.when(mockResultSet.next()).thenReturn(true, true, false);
        Mockito.when(mockResultSet.getInt("grid_id")).thenReturn(1, 2);
        Mockito.when(mockResultSet.getInt("rows")).thenReturn(10, 20);
        Mockito.when(mockResultSet.getInt("columns")).thenReturn(5, 15);

        GridRepositoryImpl repository = new GridRepositoryImpl(dbInteractions);

        List<Grid> result = repository.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getGridId()).isEqualTo(1);
        assertThat(result.get(0).getRows()).isEqualTo(10);
        assertThat(result.get(0).getColumns()).isEqualTo(5);
        assertThat(result.get(1).getGridId()).isEqualTo(2);
        assertThat(result.get(1).getRows()).isEqualTo(20);
        assertThat(result.get(1).getColumns()).isEqualTo(15);
    }

    @Test
    public void testFindAll_ReturnsEmptyListWhenResultSetIsEmpty() throws SQLException {
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.query("SELECT * FROM viron.grid")).thenReturn(mockResultSet);
        Mockito.when(mockResultSet.next()).thenReturn(false);

        GridRepositoryImpl repository = new GridRepositoryImpl(dbInteractions);

        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    public void testFindAll_ReturnsEmptyListWhenResultSetIsNull() {
        Mockito.when(dbInteractions.query("SELECT * FROM viron.grid")).thenReturn(null);

        GridRepositoryImpl repository = new GridRepositoryImpl(dbInteractions);

        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    public void testFindAll_ReturnsEmptyListWhenSQLExceptionThrown() throws SQLException {
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.query("SELECT * FROM viron.grid")).thenReturn(mockResultSet);
        Mockito.when(mockResultSet.next()).thenThrow(new SQLException("boom"));

        GridRepositoryImpl repository = new GridRepositoryImpl(dbInteractions);

        assertThat(repository.findAll()).isEmpty();
    }

    // ---- findById ----

    @Test
    public void testFindById_ReturnsGridWhenRowExists() throws SQLException {
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.query("SELECT * FROM viron.grid WHERE grid_id = 1")).thenReturn(mockResultSet);
        Mockito.when(mockResultSet.next()).thenReturn(true);
        Mockito.when(mockResultSet.getInt("grid_id")).thenReturn(1);
        Mockito.when(mockResultSet.getInt("rows")).thenReturn(10);
        Mockito.when(mockResultSet.getInt("columns")).thenReturn(5);

        GridRepositoryImpl repository = new GridRepositoryImpl(dbInteractions);

        Optional<Grid> result = repository.findById(1);

        assertThat(result).isPresent();
        assertThat(result.get().getGridId()).isEqualTo(1);
        assertThat(result.get().getRows()).isEqualTo(10);
        assertThat(result.get().getColumns()).isEqualTo(5);
    }

    @Test
    public void testFindById_ReturnsEmptyWhenNoRow() throws SQLException {
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.query("SELECT * FROM viron.grid WHERE grid_id = 99")).thenReturn(mockResultSet);
        Mockito.when(mockResultSet.next()).thenReturn(false);

        GridRepositoryImpl repository = new GridRepositoryImpl(dbInteractions);

        assertThat(repository.findById(99)).isEmpty();
    }

    @Test
    public void testFindById_ReturnsEmptyWhenResultSetIsNull() {
        Mockito.when(dbInteractions.query("SELECT * FROM viron.grid WHERE grid_id = 1")).thenReturn(null);

        GridRepositoryImpl repository = new GridRepositoryImpl(dbInteractions);

        assertThat(repository.findById(1)).isEmpty();
    }

    @Test
    public void testFindById_ReturnsEmptyWhenSQLExceptionThrown() throws SQLException {
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.query("SELECT * FROM viron.grid WHERE grid_id = 1")).thenReturn(mockResultSet);
        Mockito.when(mockResultSet.next()).thenThrow(new SQLException("boom"));

        GridRepositoryImpl repository = new GridRepositoryImpl(dbInteractions);

        assertThat(repository.findById(1)).isEmpty();
    }

    // ---- findByEnvironmentId ----

    @Test
    public void testFindByEnvironmentId_ReturnsGridsWhenRowsExist() throws SQLException {
        String query = "SELECT * FROM viron.grid WHERE grid_id in " +
                "(SELECT grid_id FROM viron.grid_environment WHERE environment_id = 7)";
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.query(query)).thenReturn(mockResultSet);
        Mockito.when(mockResultSet.next()).thenReturn(true, false);
        Mockito.when(mockResultSet.getInt("grid_id")).thenReturn(3);
        Mockito.when(mockResultSet.getInt("rows")).thenReturn(8);
        Mockito.when(mockResultSet.getInt("columns")).thenReturn(8);

        GridRepositoryImpl repository = new GridRepositoryImpl(dbInteractions);

        List<Grid> result = repository.findByEnvironmentId(7);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getGridId()).isEqualTo(3);
    }

    @Test
    public void testFindByEnvironmentId_ReturnsEmptyListWhenResultSetIsNull() {
        String query = "SELECT * FROM viron.grid WHERE grid_id in " +
                "(SELECT grid_id FROM viron.grid_environment WHERE environment_id = 7)";
        Mockito.when(dbInteractions.query(query)).thenReturn(null);

        GridRepositoryImpl repository = new GridRepositoryImpl(dbInteractions);

        assertThat(repository.findByEnvironmentId(7)).isEmpty();
    }

    // ---- findByEntityId ----

    @Test
    public void testFindByEntityId_ReturnsGridWhenRowExists() throws SQLException {
        String query = "SELECT * FROM viron.grid WHERE grid_id in " +
                "(SELECT grid_id FROM viron.location_grid WHERE location_id in " +
                "(SELECT location_id FROM viron.entity_location WHERE entity_id = 42))";
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.query(query)).thenReturn(mockResultSet);
        Mockito.when(mockResultSet.next()).thenReturn(true);
        Mockito.when(mockResultSet.getInt("grid_id")).thenReturn(9);
        Mockito.when(mockResultSet.getInt("rows")).thenReturn(4);
        Mockito.when(mockResultSet.getInt("columns")).thenReturn(4);

        GridRepositoryImpl repository = new GridRepositoryImpl(dbInteractions);

        Optional<Grid> result = repository.findByEntityId(42);

        assertThat(result).isPresent();
        assertThat(result.get().getGridId()).isEqualTo(9);
    }

    @Test
    public void testFindByEntityId_ReturnsEmptyWhenResultSetIsNull() {
        String query = "SELECT * FROM viron.grid WHERE grid_id in " +
                "(SELECT grid_id FROM viron.location_grid WHERE location_id in " +
                "(SELECT location_id FROM viron.entity_location WHERE entity_id = 42))";
        Mockito.when(dbInteractions.query(query)).thenReturn(null);

        GridRepositoryImpl repository = new GridRepositoryImpl(dbInteractions);

        assertThat(repository.findByEntityId(42)).isEmpty();
    }
}
