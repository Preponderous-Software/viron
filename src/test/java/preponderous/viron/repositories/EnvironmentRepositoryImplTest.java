package preponderous.viron.repositories;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import preponderous.viron.database.DbInteractions;
import preponderous.viron.models.Environment;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class EnvironmentRepositoryImplTest {

    @MockBean
    private DbInteractions dbInteractions;

    // ---- findAll ----

    @Test
    public void testFindAll_ReturnsEnvironmentsWhenResultSetIsNotEmpty() throws SQLException {
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.query("SELECT * FROM viron.environment")).thenReturn(mockResultSet);
        Mockito.when(mockResultSet.next()).thenReturn(true, true, false);
        Mockito.when(mockResultSet.getInt("environment_id")).thenReturn(1, 2);
        Mockito.when(mockResultSet.getString("name")).thenReturn("Env1", "Env2");
        Mockito.when(mockResultSet.getString("creation_date")).thenReturn("2023-01-01", "2023-02-01");

        EnvironmentRepositoryImpl repository = new EnvironmentRepositoryImpl(dbInteractions);

        List<Environment> result = repository.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getEnvironmentId()).isEqualTo(1);
        assertThat(result.get(0).getName()).isEqualTo("Env1");
        assertThat(result.get(0).getCreationDate()).isEqualTo("2023-01-01");
        assertThat(result.get(1).getEnvironmentId()).isEqualTo(2);
        assertThat(result.get(1).getName()).isEqualTo("Env2");
        assertThat(result.get(1).getCreationDate()).isEqualTo("2023-02-01");
    }

    @Test
    public void testFindAll_ReturnsEmptyListWhenResultSetIsEmpty() throws SQLException {
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.query("SELECT * FROM viron.environment")).thenReturn(mockResultSet);
        Mockito.when(mockResultSet.next()).thenReturn(false);

        EnvironmentRepositoryImpl repository = new EnvironmentRepositoryImpl(dbInteractions);

        List<Environment> result = repository.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    public void testFindAll_ReturnsEmptyListWhenSQLExceptionThrown() throws SQLException {
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.query("SELECT * FROM viron.environment")).thenReturn(mockResultSet);
        Mockito.when(mockResultSet.next()).thenThrow(new SQLException("Simulated SQL Exception"));

        EnvironmentRepositoryImpl repository = new EnvironmentRepositoryImpl(dbInteractions);

        List<Environment> result = repository.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    public void testFindAll_ReturnsEmptyListWhenResultSetIsNull() {
        Mockito.when(dbInteractions.query("SELECT * FROM viron.environment")).thenReturn(null);

        EnvironmentRepositoryImpl repository = new EnvironmentRepositoryImpl(dbInteractions);

        List<Environment> result = repository.findAll();

        assertThat(result).isEmpty();
    }

    // ---- findById ----

    @Test
    public void testFindById_ReturnsEnvironmentWhenRowExists() throws SQLException {
        int id = 1;
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.query("SELECT * FROM viron.environment WHERE environment_id = " + id)).thenReturn(mockResultSet);
        Mockito.when(mockResultSet.next()).thenReturn(true);
        Mockito.when(mockResultSet.getInt("environment_id")).thenReturn(id);
        Mockito.when(mockResultSet.getString("name")).thenReturn("Env1");
        Mockito.when(mockResultSet.getString("creation_date")).thenReturn("2023-01-01");

        EnvironmentRepositoryImpl repository = new EnvironmentRepositoryImpl(dbInteractions);

        Optional<Environment> result = repository.findById(id);

        assertThat(result).isPresent();
        assertThat(result.get().getEnvironmentId()).isEqualTo(id);
        assertThat(result.get().getName()).isEqualTo("Env1");
        assertThat(result.get().getCreationDate()).isEqualTo("2023-01-01");
    }

    @Test
    public void testFindById_ReturnsEmptyWhenNoRow() throws SQLException {
        int id = 1;
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.query("SELECT * FROM viron.environment WHERE environment_id = " + id)).thenReturn(mockResultSet);
        Mockito.when(mockResultSet.next()).thenReturn(false);

        EnvironmentRepositoryImpl repository = new EnvironmentRepositoryImpl(dbInteractions);

        Optional<Environment> result = repository.findById(id);

        assertThat(result).isEmpty();
    }

    @Test
    public void testFindById_ReturnsEmptyWhenSQLExceptionThrown() throws SQLException {
        int id = 1;
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.query("SELECT * FROM viron.environment WHERE environment_id = " + id)).thenReturn(mockResultSet);
        Mockito.when(mockResultSet.next()).thenThrow(new SQLException("Simulated SQL Exception"));

        EnvironmentRepositoryImpl repository = new EnvironmentRepositoryImpl(dbInteractions);

        Optional<Environment> result = repository.findById(id);

        assertThat(result).isEmpty();
    }

    @Test
    public void testFindById_ReturnsEmptyWhenResultSetIsNull() {
        int id = 1;
        Mockito.when(dbInteractions.query("SELECT * FROM viron.environment WHERE environment_id = " + id)).thenReturn(null);

        EnvironmentRepositoryImpl repository = new EnvironmentRepositoryImpl(dbInteractions);

        Optional<Environment> result = repository.findById(id);

        assertThat(result).isEmpty();
    }

    // ---- findByName ----

    @Test
    public void testFindByName_ReturnsEnvironmentWhenRowExists() throws SQLException {
        String name = "Env1";
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.query("SELECT * FROM viron.environment WHERE name = ?", name)).thenReturn(mockResultSet);
        Mockito.when(mockResultSet.next()).thenReturn(true);
        Mockito.when(mockResultSet.getInt("environment_id")).thenReturn(1);
        Mockito.when(mockResultSet.getString("name")).thenReturn(name);
        Mockito.when(mockResultSet.getString("creation_date")).thenReturn("2023-01-01");

        EnvironmentRepositoryImpl repository = new EnvironmentRepositoryImpl(dbInteractions);

        Optional<Environment> result = repository.findByName(name);

        assertThat(result).isPresent();
        assertThat(result.get().getEnvironmentId()).isEqualTo(1);
        assertThat(result.get().getName()).isEqualTo(name);
        assertThat(result.get().getCreationDate()).isEqualTo("2023-01-01");
    }

    @Test
    public void testFindByName_ReturnsEmptyWhenNoRow() throws SQLException {
        String name = "Env1";
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.query("SELECT * FROM viron.environment WHERE name = ?", name)).thenReturn(mockResultSet);
        Mockito.when(mockResultSet.next()).thenReturn(false);

        EnvironmentRepositoryImpl repository = new EnvironmentRepositoryImpl(dbInteractions);

        Optional<Environment> result = repository.findByName(name);

        assertThat(result).isEmpty();
    }

    @Test
    public void testFindByName_ReturnsEmptyWhenSQLExceptionThrown() throws SQLException {
        String name = "Env1";
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.query("SELECT * FROM viron.environment WHERE name = ?", name)).thenReturn(mockResultSet);
        Mockito.when(mockResultSet.next()).thenThrow(new SQLException("Simulated SQL Exception"));

        EnvironmentRepositoryImpl repository = new EnvironmentRepositoryImpl(dbInteractions);

        Optional<Environment> result = repository.findByName(name);

        assertThat(result).isEmpty();
    }

    @Test
    public void testFindByName_ReturnsEmptyWhenResultSetIsNull() {
        String name = "Env1";
        Mockito.when(dbInteractions.query("SELECT * FROM viron.environment WHERE name = ?", name)).thenReturn(null);

        EnvironmentRepositoryImpl repository = new EnvironmentRepositoryImpl(dbInteractions);

        Optional<Environment> result = repository.findByName(name);

        assertThat(result).isEmpty();
    }

    // ---- findByEntityId ----

    @Test
    public void testFindByEntityId_ReturnsEnvironmentWhenRowExists() throws SQLException {
        int entityId = 7;
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.query("SELECT * FROM viron.environment WHERE environment_id = (SELECT environment_id FROM viron.entity WHERE entity_id = " + entityId + ")")).thenReturn(mockResultSet);
        Mockito.when(mockResultSet.next()).thenReturn(true);
        Mockito.when(mockResultSet.getInt("environment_id")).thenReturn(3);
        Mockito.when(mockResultSet.getString("name")).thenReturn("Env3");
        Mockito.when(mockResultSet.getString("creation_date")).thenReturn("2023-03-01");

        EnvironmentRepositoryImpl repository = new EnvironmentRepositoryImpl(dbInteractions);

        Optional<Environment> result = repository.findByEntityId(entityId);

        assertThat(result).isPresent();
        assertThat(result.get().getEnvironmentId()).isEqualTo(3);
        assertThat(result.get().getName()).isEqualTo("Env3");
        assertThat(result.get().getCreationDate()).isEqualTo("2023-03-01");
    }

    @Test
    public void testFindByEntityId_ReturnsEmptyWhenNoRow() throws SQLException {
        int entityId = 7;
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.query("SELECT * FROM viron.environment WHERE environment_id = (SELECT environment_id FROM viron.entity WHERE entity_id = " + entityId + ")")).thenReturn(mockResultSet);
        Mockito.when(mockResultSet.next()).thenReturn(false);

        EnvironmentRepositoryImpl repository = new EnvironmentRepositoryImpl(dbInteractions);

        Optional<Environment> result = repository.findByEntityId(entityId);

        assertThat(result).isEmpty();
    }

    @Test
    public void testFindByEntityId_ReturnsEmptyWhenSQLExceptionThrown() throws SQLException {
        int entityId = 7;
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.query("SELECT * FROM viron.environment WHERE environment_id = (SELECT environment_id FROM viron.entity WHERE entity_id = " + entityId + ")")).thenReturn(mockResultSet);
        Mockito.when(mockResultSet.next()).thenThrow(new SQLException("Simulated SQL Exception"));

        EnvironmentRepositoryImpl repository = new EnvironmentRepositoryImpl(dbInteractions);

        Optional<Environment> result = repository.findByEntityId(entityId);

        assertThat(result).isEmpty();
    }

    @Test
    public void testFindByEntityId_ReturnsEmptyWhenResultSetIsNull() {
        int entityId = 7;
        Mockito.when(dbInteractions.query("SELECT * FROM viron.environment WHERE environment_id = (SELECT environment_id FROM viron.entity WHERE entity_id = " + entityId + ")")).thenReturn(null);

        EnvironmentRepositoryImpl repository = new EnvironmentRepositoryImpl(dbInteractions);

        Optional<Environment> result = repository.findByEntityId(entityId);

        assertThat(result).isEmpty();
    }

    // ---- save ----

    @Test
    public void testSave_ReturnsSameEnvironmentInstance() {
        Environment environment = new Environment(5, "Env5", "2023-05-01");

        EnvironmentRepositoryImpl repository = new EnvironmentRepositoryImpl(dbInteractions);

        Environment result = repository.save(environment);

        assertThat(result).isSameAs(environment);
        Mockito.verifyNoInteractions(dbInteractions);
    }

    // ---- deleteById ----

    @Test
    public void testDeleteById_ReturnsTrueWhenUpdateSucceeds() {
        int id = 1;
        Mockito.when(dbInteractions.update("DELETE FROM viron.environment WHERE environment_id = " + id)).thenReturn(true);

        EnvironmentRepositoryImpl repository = new EnvironmentRepositoryImpl(dbInteractions);

        assertThat(repository.deleteById(id)).isTrue();
    }

    @Test
    public void testDeleteById_ReturnsFalseWhenUpdateFails() {
        int id = 1;
        Mockito.when(dbInteractions.update("DELETE FROM viron.environment WHERE environment_id = " + id)).thenReturn(false);

        EnvironmentRepositoryImpl repository = new EnvironmentRepositoryImpl(dbInteractions);

        assertThat(repository.deleteById(id)).isFalse();
    }

    // ---- updateName ----

    @Test
    public void testUpdateName_ReturnsTrueWhenUpdateSucceeds() {
        int id = 1;
        String name = "NewName";
        Mockito.when(dbInteractions.update("UPDATE viron.environment SET name = ? WHERE environment_id = " + id, name)).thenReturn(true);

        EnvironmentRepositoryImpl repository = new EnvironmentRepositoryImpl(dbInteractions);

        assertThat(repository.updateName(id, name)).isTrue();
    }

    @Test
    public void testUpdateName_ReturnsFalseWhenUpdateFails() {
        int id = 1;
        String name = "NewName";
        Mockito.when(dbInteractions.update("UPDATE viron.environment SET name = ? WHERE environment_id = " + id, name)).thenReturn(false);

        EnvironmentRepositoryImpl repository = new EnvironmentRepositoryImpl(dbInteractions);

        assertThat(repository.updateName(id, name)).isFalse();
    }

    // ---- findEntityIdsByEnvironmentId ----

    @Test
    public void testFindEntityIdsByEnvironmentId_ReturnsIdsWhenResultSetIsNotEmpty() throws SQLException {
        int environmentId = 5;
        String query = "SELECT entity_id FROM viron.entity WHERE entity_id in (SELECT entity_id FROM viron.entity_location WHERE location_id in (SELECT location_id FROM viron.location_grid WHERE grid_id in (SELECT grid_id FROM viron.grid_environment WHERE environment_id = " + environmentId + ")))";
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.query(query)).thenReturn(mockResultSet);
        Mockito.when(mockResultSet.next()).thenReturn(true, true, false);
        Mockito.when(mockResultSet.getInt("entity_id")).thenReturn(11, 22);

        EnvironmentRepositoryImpl repository = new EnvironmentRepositoryImpl(dbInteractions);

        List<Integer> result = repository.findEntityIdsByEnvironmentId(environmentId);

        assertThat(result).containsExactly(11, 22);
    }

    @Test
    public void testFindEntityIdsByEnvironmentId_ReturnsEmptyListWhenResultSetIsEmpty() throws SQLException {
        int environmentId = 5;
        String query = "SELECT entity_id FROM viron.entity WHERE entity_id in (SELECT entity_id FROM viron.entity_location WHERE location_id in (SELECT location_id FROM viron.location_grid WHERE grid_id in (SELECT grid_id FROM viron.grid_environment WHERE environment_id = " + environmentId + ")))";
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.query(query)).thenReturn(mockResultSet);
        Mockito.when(mockResultSet.next()).thenReturn(false);

        EnvironmentRepositoryImpl repository = new EnvironmentRepositoryImpl(dbInteractions);

        List<Integer> result = repository.findEntityIdsByEnvironmentId(environmentId);

        assertThat(result).isEmpty();
    }

    @Test
    public void testFindEntityIdsByEnvironmentId_ReturnsEmptyListWhenSQLExceptionThrown() throws SQLException {
        int environmentId = 5;
        String query = "SELECT entity_id FROM viron.entity WHERE entity_id in (SELECT entity_id FROM viron.entity_location WHERE location_id in (SELECT location_id FROM viron.location_grid WHERE grid_id in (SELECT grid_id FROM viron.grid_environment WHERE environment_id = " + environmentId + ")))";
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.query(query)).thenReturn(mockResultSet);
        Mockito.when(mockResultSet.next()).thenThrow(new SQLException("Simulated SQL Exception"));

        EnvironmentRepositoryImpl repository = new EnvironmentRepositoryImpl(dbInteractions);

        List<Integer> result = repository.findEntityIdsByEnvironmentId(environmentId);

        assertThat(result).isEmpty();
    }

    @Test
    public void testFindEntityIdsByEnvironmentId_ReturnsEmptyListWhenResultSetIsNull() {
        int environmentId = 5;
        String query = "SELECT entity_id FROM viron.entity WHERE entity_id in (SELECT entity_id FROM viron.entity_location WHERE location_id in (SELECT location_id FROM viron.location_grid WHERE grid_id in (SELECT grid_id FROM viron.grid_environment WHERE environment_id = " + environmentId + ")))";
        Mockito.when(dbInteractions.query(query)).thenReturn(null);

        EnvironmentRepositoryImpl repository = new EnvironmentRepositoryImpl(dbInteractions);

        List<Integer> result = repository.findEntityIdsByEnvironmentId(environmentId);

        assertThat(result).isEmpty();
    }

    // ---- findLocationIdsByEnvironmentId ----

    @Test
    public void testFindLocationIdsByEnvironmentId_ReturnsIdsWhenResultSetIsNotEmpty() throws SQLException {
        int environmentId = 5;
        String query = "SELECT location_id FROM viron.location_grid WHERE grid_id in (SELECT grid_id FROM viron.grid_environment WHERE environment_id = " + environmentId + ")";
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.query(query)).thenReturn(mockResultSet);
        Mockito.when(mockResultSet.next()).thenReturn(true, true, false);
        Mockito.when(mockResultSet.getInt("location_id")).thenReturn(33, 44);

        EnvironmentRepositoryImpl repository = new EnvironmentRepositoryImpl(dbInteractions);

        List<Integer> result = repository.findLocationIdsByEnvironmentId(environmentId);

        assertThat(result).containsExactly(33, 44);
    }

    @Test
    public void testFindLocationIdsByEnvironmentId_ReturnsEmptyListWhenResultSetIsEmpty() throws SQLException {
        int environmentId = 5;
        String query = "SELECT location_id FROM viron.location_grid WHERE grid_id in (SELECT grid_id FROM viron.grid_environment WHERE environment_id = " + environmentId + ")";
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.query(query)).thenReturn(mockResultSet);
        Mockito.when(mockResultSet.next()).thenReturn(false);

        EnvironmentRepositoryImpl repository = new EnvironmentRepositoryImpl(dbInteractions);

        List<Integer> result = repository.findLocationIdsByEnvironmentId(environmentId);

        assertThat(result).isEmpty();
    }

    @Test
    public void testFindLocationIdsByEnvironmentId_ReturnsEmptyListWhenSQLExceptionThrown() throws SQLException {
        int environmentId = 5;
        String query = "SELECT location_id FROM viron.location_grid WHERE grid_id in (SELECT grid_id FROM viron.grid_environment WHERE environment_id = " + environmentId + ")";
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.query(query)).thenReturn(mockResultSet);
        Mockito.when(mockResultSet.next()).thenThrow(new SQLException("Simulated SQL Exception"));

        EnvironmentRepositoryImpl repository = new EnvironmentRepositoryImpl(dbInteractions);

        List<Integer> result = repository.findLocationIdsByEnvironmentId(environmentId);

        assertThat(result).isEmpty();
    }

    @Test
    public void testFindLocationIdsByEnvironmentId_ReturnsEmptyListWhenResultSetIsNull() {
        int environmentId = 5;
        String query = "SELECT location_id FROM viron.location_grid WHERE grid_id in (SELECT grid_id FROM viron.grid_environment WHERE environment_id = " + environmentId + ")";
        Mockito.when(dbInteractions.query(query)).thenReturn(null);

        EnvironmentRepositoryImpl repository = new EnvironmentRepositoryImpl(dbInteractions);

        List<Integer> result = repository.findLocationIdsByEnvironmentId(environmentId);

        assertThat(result).isEmpty();
    }

    // ---- findGridIdsByEnvironmentId ----

    @Test
    public void testFindGridIdsByEnvironmentId_ReturnsIdsWhenResultSetIsNotEmpty() throws SQLException {
        int environmentId = 5;
        String query = "SELECT grid_id FROM viron.grid_environment WHERE environment_id = " + environmentId;
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.query(query)).thenReturn(mockResultSet);
        Mockito.when(mockResultSet.next()).thenReturn(true, true, false);
        Mockito.when(mockResultSet.getInt("grid_id")).thenReturn(55, 66);

        EnvironmentRepositoryImpl repository = new EnvironmentRepositoryImpl(dbInteractions);

        List<Integer> result = repository.findGridIdsByEnvironmentId(environmentId);

        assertThat(result).containsExactly(55, 66);
    }

    @Test
    public void testFindGridIdsByEnvironmentId_ReturnsEmptyListWhenResultSetIsEmpty() throws SQLException {
        int environmentId = 5;
        String query = "SELECT grid_id FROM viron.grid_environment WHERE environment_id = " + environmentId;
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.query(query)).thenReturn(mockResultSet);
        Mockito.when(mockResultSet.next()).thenReturn(false);

        EnvironmentRepositoryImpl repository = new EnvironmentRepositoryImpl(dbInteractions);

        List<Integer> result = repository.findGridIdsByEnvironmentId(environmentId);

        assertThat(result).isEmpty();
    }

    @Test
    public void testFindGridIdsByEnvironmentId_ReturnsEmptyListWhenSQLExceptionThrown() throws SQLException {
        int environmentId = 5;
        String query = "SELECT grid_id FROM viron.grid_environment WHERE environment_id = " + environmentId;
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.query(query)).thenReturn(mockResultSet);
        Mockito.when(mockResultSet.next()).thenThrow(new SQLException("Simulated SQL Exception"));

        EnvironmentRepositoryImpl repository = new EnvironmentRepositoryImpl(dbInteractions);

        List<Integer> result = repository.findGridIdsByEnvironmentId(environmentId);

        assertThat(result).isEmpty();
    }

    @Test
    public void testFindGridIdsByEnvironmentId_ReturnsEmptyListWhenResultSetIsNull() {
        int environmentId = 5;
        String query = "SELECT grid_id FROM viron.grid_environment WHERE environment_id = " + environmentId;
        Mockito.when(dbInteractions.query(query)).thenReturn(null);

        EnvironmentRepositoryImpl repository = new EnvironmentRepositoryImpl(dbInteractions);

        List<Integer> result = repository.findGridIdsByEnvironmentId(environmentId);

        assertThat(result).isEmpty();
    }

    // ---- deleteEntityLocation ----

    @Test
    public void testDeleteEntityLocation_ReturnsTrueWhenUpdateSucceeds() {
        int entityId = 9;
        Mockito.when(dbInteractions.update("DELETE FROM viron.entity_location WHERE entity_id = " + entityId)).thenReturn(true);

        EnvironmentRepositoryImpl repository = new EnvironmentRepositoryImpl(dbInteractions);

        assertThat(repository.deleteEntityLocation(entityId)).isTrue();
    }

    @Test
    public void testDeleteEntityLocation_ReturnsFalseWhenUpdateFails() {
        int entityId = 9;
        Mockito.when(dbInteractions.update("DELETE FROM viron.entity_location WHERE entity_id = " + entityId)).thenReturn(false);

        EnvironmentRepositoryImpl repository = new EnvironmentRepositoryImpl(dbInteractions);

        assertThat(repository.deleteEntityLocation(entityId)).isFalse();
    }

    // ---- deleteLocationGrid ----

    @Test
    public void testDeleteLocationGrid_ReturnsTrueWhenUpdateSucceeds() {
        int locationId = 9;
        Mockito.when(dbInteractions.update("DELETE FROM viron.location_grid WHERE location_id = " + locationId)).thenReturn(true);

        EnvironmentRepositoryImpl repository = new EnvironmentRepositoryImpl(dbInteractions);

        assertThat(repository.deleteLocationGrid(locationId)).isTrue();
    }

    @Test
    public void testDeleteLocationGrid_ReturnsFalseWhenUpdateFails() {
        int locationId = 9;
        Mockito.when(dbInteractions.update("DELETE FROM viron.location_grid WHERE location_id = " + locationId)).thenReturn(false);

        EnvironmentRepositoryImpl repository = new EnvironmentRepositoryImpl(dbInteractions);

        assertThat(repository.deleteLocationGrid(locationId)).isFalse();
    }

    // ---- deleteGridEnvironment ----

    @Test
    public void testDeleteGridEnvironment_ReturnsTrueWhenUpdateSucceeds() {
        int gridId = 9;
        Mockito.when(dbInteractions.update("DELETE FROM viron.grid_environment WHERE grid_id = " + gridId)).thenReturn(true);

        EnvironmentRepositoryImpl repository = new EnvironmentRepositoryImpl(dbInteractions);

        assertThat(repository.deleteGridEnvironment(gridId)).isTrue();
    }

    @Test
    public void testDeleteGridEnvironment_ReturnsFalseWhenUpdateFails() {
        int gridId = 9;
        Mockito.when(dbInteractions.update("DELETE FROM viron.grid_environment WHERE grid_id = " + gridId)).thenReturn(false);

        EnvironmentRepositoryImpl repository = new EnvironmentRepositoryImpl(dbInteractions);

        assertThat(repository.deleteGridEnvironment(gridId)).isFalse();
    }

    // ---- deleteEntity ----

    @Test
    public void testDeleteEntity_ReturnsTrueWhenUpdateSucceeds() {
        int entityId = 9;
        Mockito.when(dbInteractions.update("DELETE FROM viron.entity WHERE entity_id = " + entityId)).thenReturn(true);

        EnvironmentRepositoryImpl repository = new EnvironmentRepositoryImpl(dbInteractions);

        assertThat(repository.deleteEntity(entityId)).isTrue();
    }

    @Test
    public void testDeleteEntity_ReturnsFalseWhenUpdateFails() {
        int entityId = 9;
        Mockito.when(dbInteractions.update("DELETE FROM viron.entity WHERE entity_id = " + entityId)).thenReturn(false);

        EnvironmentRepositoryImpl repository = new EnvironmentRepositoryImpl(dbInteractions);

        assertThat(repository.deleteEntity(entityId)).isFalse();
    }

    // ---- deleteLocation ----

    @Test
    public void testDeleteLocation_ReturnsTrueWhenUpdateSucceeds() {
        int locationId = 9;
        Mockito.when(dbInteractions.update("DELETE FROM viron.location WHERE location_id = " + locationId)).thenReturn(true);

        EnvironmentRepositoryImpl repository = new EnvironmentRepositoryImpl(dbInteractions);

        assertThat(repository.deleteLocation(locationId)).isTrue();
    }

    @Test
    public void testDeleteLocation_ReturnsFalseWhenUpdateFails() {
        int locationId = 9;
        Mockito.when(dbInteractions.update("DELETE FROM viron.location WHERE location_id = " + locationId)).thenReturn(false);

        EnvironmentRepositoryImpl repository = new EnvironmentRepositoryImpl(dbInteractions);

        assertThat(repository.deleteLocation(locationId)).isFalse();
    }

    // ---- deleteGrid ----

    @Test
    public void testDeleteGrid_ReturnsTrueWhenUpdateSucceeds() {
        int gridId = 9;
        Mockito.when(dbInteractions.update("DELETE FROM viron.grid WHERE grid_id = " + gridId)).thenReturn(true);

        EnvironmentRepositoryImpl repository = new EnvironmentRepositoryImpl(dbInteractions);

        assertThat(repository.deleteGrid(gridId)).isTrue();
    }

    @Test
    public void testDeleteGrid_ReturnsFalseWhenUpdateFails() {
        int gridId = 9;
        Mockito.when(dbInteractions.update("DELETE FROM viron.grid WHERE grid_id = " + gridId)).thenReturn(false);

        EnvironmentRepositoryImpl repository = new EnvironmentRepositoryImpl(dbInteractions);

        assertThat(repository.deleteGrid(gridId)).isFalse();
    }
}
