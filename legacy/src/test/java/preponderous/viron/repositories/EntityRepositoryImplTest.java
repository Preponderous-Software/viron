package preponderous.viron.repositories;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import preponderous.viron.database.DbInteractions;
import preponderous.viron.models.Entity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class EntityRepositoryImplTest {

    @MockBean
    private DbInteractions dbInteractions;

    @Test
    public void testFindAll_ReturnsEntitiesWhenResultSetIsNotEmpty() throws SQLException {
        // Arrange
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);

        Mockito.when(dbInteractions.query("SELECT * FROM viron.entity")).thenReturn(mockResultSet);
        Mockito.when(mockResultSet.next()).thenReturn(true, true, false); // 2 entities in the result set
        Mockito.when(mockResultSet.getInt("entity_id")).thenReturn(1, 2);
        Mockito.when(mockResultSet.getString("name")).thenReturn("Entity1", "Entity2");
        Mockito.when(mockResultSet.getString("creation_date")).thenReturn("2023-01-01", "2023-02-01");

        EntityRepositoryImpl repository = new EntityRepositoryImpl(dbInteractions);

        // Act
        List<Entity> result = repository.findAll();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getEntityId()).isEqualTo(1);
        assertThat(result.get(0).getName()).isEqualTo("Entity1");
        assertThat(result.get(0).getCreationDate()).isEqualTo("2023-01-01");
        assertThat(result.get(1).getEntityId()).isEqualTo(2);
        assertThat(result.get(1).getName()).isEqualTo("Entity2");
        assertThat(result.get(1).getCreationDate()).isEqualTo("2023-02-01");
    }

    @Test
    public void testFindAll_ReturnsEmptyListWhenResultSetIsEmpty() throws SQLException {
        // Arrange
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);

        Mockito.when(dbInteractions.query("SELECT * FROM viron.entity")).thenReturn(mockResultSet);
        Mockito.when(mockResultSet.next()).thenReturn(false); // No entities in the result set

        EntityRepositoryImpl repository = new EntityRepositoryImpl(dbInteractions);

        // Act
        List<Entity> result = repository.findAll();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    public void testFindAll_ReturnsEmptyListWhenResultSetIsNull() {
        // Arrange
        Mockito.when(dbInteractions.query("SELECT * FROM viron.entity")).thenReturn(null); // ResultSet is null

        EntityRepositoryImpl repository = new EntityRepositoryImpl(dbInteractions);

        // Act
        List<Entity> result = repository.findAll();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    public void testFindAll_HandlesSQLExceptionGracefully() throws SQLException {
        // Arrange
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);

        Mockito.when(dbInteractions.query("SELECT * FROM viron.entity")).thenReturn(mockResultSet);
        Mockito.when(mockResultSet.next()).thenThrow(new SQLException("Simulated SQL Exception"));

        EntityRepositoryImpl repository = new EntityRepositoryImpl(dbInteractions);

        // Act
        List<Entity> result = repository.findAll();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    public void testFindById_ReturnsEntityWhenFound() throws SQLException {
        // Arrange
        int entityId = 1;
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);

        Mockito.when(dbInteractions.query("SELECT * FROM viron.entity WHERE entity_id = " + entityId)).thenReturn(mockResultSet);
        Mockito.when(mockResultSet.next()).thenReturn(true); // Entity is found
        Mockito.when(mockResultSet.getInt("entity_id")).thenReturn(entityId);
        Mockito.when(mockResultSet.getString("name")).thenReturn("Entity1");
        Mockito.when(mockResultSet.getString("creation_date")).thenReturn("2023-01-01");

        EntityRepositoryImpl repository = new EntityRepositoryImpl(dbInteractions);

        // Act
        Optional<Entity> result = repository.findById(entityId);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getEntityId()).isEqualTo(entityId);
        assertThat(result.get().getName()).isEqualTo("Entity1");
        assertThat(result.get().getCreationDate()).isEqualTo("2023-01-01");
    }

    @Test
    public void testFindById_ReturnsEmptyWhenNotFound() throws SQLException {
        // Arrange
        int entityId = 1;
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);

        Mockito.when(dbInteractions.query("SELECT * FROM viron.entity WHERE entity_id = " + entityId)).thenReturn(mockResultSet);
        Mockito.when(mockResultSet.next()).thenReturn(false); // Entity is not found

        EntityRepositoryImpl repository = new EntityRepositoryImpl(dbInteractions);

        // Act
        Optional<Entity> result = repository.findById(entityId);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    public void testFindById_ReturnsEmptyWhenResultSetIsNull() {
        // Arrange
        int entityId = 1;
        Mockito.when(dbInteractions.query("SELECT * FROM viron.entity WHERE entity_id = " + entityId)).thenReturn(null); // ResultSet is null

        EntityRepositoryImpl repository = new EntityRepositoryImpl(dbInteractions);

        // Act
        Optional<Entity> result = repository.findById(entityId);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    public void testFindById_HandlesSQLExceptionGracefully() throws SQLException {
        // Arrange
        int entityId = 1;
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);

        Mockito.when(dbInteractions.query("SELECT * FROM viron.entity WHERE entity_id = " + entityId)).thenReturn(mockResultSet);
        Mockito.when(mockResultSet.next()).thenThrow(new SQLException("Simulated SQL Exception"));

        EntityRepositoryImpl repository = new EntityRepositoryImpl(dbInteractions);

        // Act
        Optional<Entity> result = repository.findById(entityId);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    public void testFindByEnvironmentId_ReturnsEntitiesWhenResultSetIsNotEmpty() throws SQLException {
        // Arrange
        int environmentId = 5;
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.query("SELECT * FROM viron.entity WHERE entity_id in " +
                        "(SELECT entity_id FROM viron.entity_location WHERE location_id in " +
                        "(SELECT location_id FROM viron.location_grid WHERE grid_id in " +
                        "(SELECT grid_id FROM viron.grid_environment WHERE environment_id = " + environmentId + ")))"))
                .thenReturn(mockResultSet);
        Mockito.when(mockResultSet.next()).thenReturn(true, true, false); // 2 entities in the result set
        Mockito.when(mockResultSet.getInt("entity_id")).thenReturn(1, 2);
        Mockito.when(mockResultSet.getString("name")).thenReturn("Entity1", "Entity2");
        Mockito.when(mockResultSet.getString("creation_date")).thenReturn("2023-01-01", "2023-02-01");

        EntityRepositoryImpl repository = new EntityRepositoryImpl(dbInteractions);

        // Act
        List<Entity> result = repository.findByEnvironmentId(environmentId);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getEntityId()).isEqualTo(1);
        assertThat(result.get(0).getName()).isEqualTo("Entity1");
        assertThat(result.get(0).getCreationDate()).isEqualTo("2023-01-01");
        assertThat(result.get(1).getEntityId()).isEqualTo(2);
        assertThat(result.get(1).getName()).isEqualTo("Entity2");
        assertThat(result.get(1).getCreationDate()).isEqualTo("2023-02-01");
    }

    @Test
    public void testFindByEnvironmentId_ReturnsEmptyListWhenResultSetIsEmpty() throws SQLException {
        // Arrange
        int environmentId = 5;
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.query("SELECT * FROM viron.entity WHERE entity_id in " +
                        "(SELECT entity_id FROM viron.entity_location WHERE location_id in " +
                        "(SELECT location_id FROM viron.location_grid WHERE grid_id in " +
                        "(SELECT grid_id FROM viron.grid_environment WHERE environment_id = " + environmentId + ")))"))
                .thenReturn(mockResultSet);
        Mockito.when(mockResultSet.next()).thenReturn(false); // No entities in the result set

        EntityRepositoryImpl repository = new EntityRepositoryImpl(dbInteractions);

        // Act
        List<Entity> result = repository.findByEnvironmentId(environmentId);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    public void testFindByEnvironmentId_ReturnsEmptyListWhenResultSetIsNull() {
        // Arrange
        int environmentId = 5;
        Mockito.when(dbInteractions.query("SELECT * FROM viron.entity WHERE entity_id in " +
                        "(SELECT entity_id FROM viron.entity_location WHERE location_id in " +
                        "(SELECT location_id FROM viron.location_grid WHERE grid_id in " +
                        "(SELECT grid_id FROM viron.grid_environment WHERE environment_id = " + environmentId + ")))"))
                .thenReturn(null); // ResultSet is null

        EntityRepositoryImpl repository = new EntityRepositoryImpl(dbInteractions);

        // Act
        List<Entity> result = repository.findByEnvironmentId(environmentId);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    public void testFindByEnvironmentId_HandlesSQLExceptionGracefully() throws SQLException {
        // Arrange
        int environmentId = 5;
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.query("SELECT * FROM viron.entity WHERE entity_id in " +
                        "(SELECT entity_id FROM viron.entity_location WHERE location_id in " +
                        "(SELECT location_id FROM viron.location_grid WHERE grid_id in " +
                        "(SELECT grid_id FROM viron.grid_environment WHERE environment_id = " + environmentId + ")))"))
                .thenReturn(mockResultSet);
        Mockito.when(mockResultSet.next()).thenThrow(new SQLException("Simulated SQL Exception"));

        EntityRepositoryImpl repository = new EntityRepositoryImpl(dbInteractions);

        // Act
        List<Entity> result = repository.findByEnvironmentId(environmentId);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    public void testFindByGridId_ReturnsEntitiesWhenResultSetIsNotEmpty() throws SQLException {
        // Arrange
        int gridId = 10;
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.query("SELECT * FROM viron.entity WHERE entity_id in " +
                        "(SELECT entity_id FROM viron.entity_location WHERE location_id in " +
                        "(SELECT location_id FROM viron.location_grid WHERE grid_id = " + gridId + "))"))
                .thenReturn(mockResultSet);
        Mockito.when(mockResultSet.next()).thenReturn(true, true, false); // 2 entities in the result set
        Mockito.when(mockResultSet.getInt("entity_id")).thenReturn(1, 2);
        Mockito.when(mockResultSet.getString("name")).thenReturn("Entity1", "Entity2");
        Mockito.when(mockResultSet.getString("creation_date")).thenReturn("2023-01-01", "2023-02-01");

        EntityRepositoryImpl repository = new EntityRepositoryImpl(dbInteractions);

        // Act
        List<Entity> result = repository.findByGridId(gridId);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getEntityId()).isEqualTo(1);
        assertThat(result.get(0).getName()).isEqualTo("Entity1");
        assertThat(result.get(0).getCreationDate()).isEqualTo("2023-01-01");
        assertThat(result.get(1).getEntityId()).isEqualTo(2);
        assertThat(result.get(1).getName()).isEqualTo("Entity2");
        assertThat(result.get(1).getCreationDate()).isEqualTo("2023-02-01");
    }

    @Test
    public void testFindByGridId_ReturnsEmptyListWhenResultSetIsEmpty() throws SQLException {
        // Arrange
        int gridId = 10;
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.query("SELECT * FROM viron.entity WHERE entity_id in " +
                        "(SELECT entity_id FROM viron.entity_location WHERE location_id in " +
                        "(SELECT location_id FROM viron.location_grid WHERE grid_id = " + gridId + "))"))
                .thenReturn(mockResultSet);
        Mockito.when(mockResultSet.next()).thenReturn(false); // No entities in the result set

        EntityRepositoryImpl repository = new EntityRepositoryImpl(dbInteractions);

        // Act
        List<Entity> result = repository.findByGridId(gridId);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    public void testFindByGridId_ReturnsEmptyListWhenResultSetIsNull() {
        // Arrange
        int gridId = 10;
        Mockito.when(dbInteractions.query("SELECT * FROM viron.entity WHERE entity_id in " +
                        "(SELECT entity_id FROM viron.entity_location WHERE location_id in " +
                        "(SELECT location_id FROM viron.location_grid WHERE grid_id = " + gridId + "))"))
                .thenReturn(null); // ResultSet is null

        EntityRepositoryImpl repository = new EntityRepositoryImpl(dbInteractions);

        // Act
        List<Entity> result = repository.findByGridId(gridId);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    public void testFindByGridId_HandlesSQLExceptionGracefully() throws SQLException {
        // Arrange
        int gridId = 10;
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.query("SELECT * FROM viron.entity WHERE entity_id in " +
                        "(SELECT entity_id FROM viron.entity_location WHERE location_id in " +
                        "(SELECT location_id FROM viron.location_grid WHERE grid_id = " + gridId + "))"))
                .thenReturn(mockResultSet);
        Mockito.when(mockResultSet.next()).thenThrow(new SQLException("Simulated SQL Exception"));

        EntityRepositoryImpl repository = new EntityRepositoryImpl(dbInteractions);

        // Act
        List<Entity> result = repository.findByGridId(gridId);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    public void testFindByLocationId_ReturnsEntitiesWhenResultSetIsNotEmpty() throws SQLException {
        // Arrange
        int locationId = 15;
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.query("SELECT * FROM viron.entity WHERE entity_id in " +
                        "(SELECT entity_id FROM viron.entity_location WHERE location_id = " + locationId + ")"))
                .thenReturn(mockResultSet);
        Mockito.when(mockResultSet.next()).thenReturn(true, true, false); // 2 entities in the result set
        Mockito.when(mockResultSet.getInt("entity_id")).thenReturn(1, 2);
        Mockito.when(mockResultSet.getString("name")).thenReturn("Entity1", "Entity2");
        Mockito.when(mockResultSet.getString("creation_date")).thenReturn("2023-01-01", "2023-02-01");

        EntityRepositoryImpl repository = new EntityRepositoryImpl(dbInteractions);

        // Act
        List<Entity> result = repository.findByLocationId(locationId);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getEntityId()).isEqualTo(1);
        assertThat(result.get(0).getName()).isEqualTo("Entity1");
        assertThat(result.get(0).getCreationDate()).isEqualTo("2023-01-01");
        assertThat(result.get(1).getEntityId()).isEqualTo(2);
        assertThat(result.get(1).getName()).isEqualTo("Entity2");
        assertThat(result.get(1).getCreationDate()).isEqualTo("2023-02-01");
    }

    @Test
    public void testFindByLocationId_ReturnsEmptyListWhenResultSetIsEmpty() throws SQLException {
        // Arrange
        int locationId = 15;
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.query("SELECT * FROM viron.entity WHERE entity_id in " +
                        "(SELECT entity_id FROM viron.entity_location WHERE location_id = " + locationId + ")"))
                .thenReturn(mockResultSet);
        Mockito.when(mockResultSet.next()).thenReturn(false); // No entities in the result set

        EntityRepositoryImpl repository = new EntityRepositoryImpl(dbInteractions);

        // Act
        List<Entity> result = repository.findByLocationId(locationId);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    public void testFindByLocationId_ReturnsEmptyListWhenResultSetIsNull() {
        // Arrange
        int locationId = 15;
        Mockito.when(dbInteractions.query("SELECT * FROM viron.entity WHERE entity_id in " +
                        "(SELECT entity_id FROM viron.entity_location WHERE location_id = " + locationId + ")"))
                .thenReturn(null); // ResultSet is null

        EntityRepositoryImpl repository = new EntityRepositoryImpl(dbInteractions);

        // Act
        List<Entity> result = repository.findByLocationId(locationId);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    public void testFindByLocationId_HandlesSQLExceptionGracefully() throws SQLException {
        // Arrange
        int locationId = 15;
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.query("SELECT * FROM viron.entity WHERE entity_id in " +
                        "(SELECT entity_id FROM viron.entity_location WHERE location_id = " + locationId + ")"))
                .thenReturn(mockResultSet);
        Mockito.when(mockResultSet.next()).thenThrow(new SQLException("Simulated SQL Exception"));

        EntityRepositoryImpl repository = new EntityRepositoryImpl(dbInteractions);

        // Act
        List<Entity> result = repository.findByLocationId(locationId);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    public void testFindEntitiesNotInAnyLocation_ReturnsEntitiesWhenResultSetIsNotEmpty() throws SQLException {
        // Arrange
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);

        Mockito.when(dbInteractions.query("SELECT * FROM viron.entity WHERE entity_id not in (SELECT entity_id FROM viron.entity_location)"))
                .thenReturn(mockResultSet);
        Mockito.when(mockResultSet.next()).thenReturn(true, true, false); // 2 entities in the result set
        Mockito.when(mockResultSet.getInt("entity_id")).thenReturn(1, 2);
        Mockito.when(mockResultSet.getString("name")).thenReturn("Entity1", "Entity2");
        Mockito.when(mockResultSet.getString("creation_date")).thenReturn("2023-01-01", "2023-02-01");

        EntityRepositoryImpl repository = new EntityRepositoryImpl(dbInteractions);

        // Act
        List<Entity> result = repository.findEntitiesNotInAnyLocation();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getEntityId()).isEqualTo(1);
        assertThat(result.get(0).getName()).isEqualTo("Entity1");
        assertThat(result.get(0).getCreationDate()).isEqualTo("2023-01-01");
        assertThat(result.get(1).getEntityId()).isEqualTo(2);
        assertThat(result.get(1).getName()).isEqualTo("Entity2");
        assertThat(result.get(1).getCreationDate()).isEqualTo("2023-02-01");
    }

    @Test
    public void testFindEntitiesNotInAnyLocation_ReturnsEmptyListWhenResultSetIsEmpty() throws SQLException {
        // Arrange
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);

        Mockito.when(dbInteractions.query("SELECT * FROM viron.entity WHERE entity_id not in (SELECT entity_id FROM viron.entity_location)"))
                .thenReturn(mockResultSet);
        Mockito.when(mockResultSet.next()).thenReturn(false); // No entities in the result set

        EntityRepositoryImpl repository = new EntityRepositoryImpl(dbInteractions);

        // Act
        List<Entity> result = repository.findEntitiesNotInAnyLocation();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    public void testFindEntitiesNotInAnyLocation_ReturnsEmptyListWhenResultSetIsNull() {
        // Arrange
        Mockito.when(dbInteractions.query("SELECT * FROM viron.entity WHERE entity_id not in (SELECT entity_id FROM viron.entity_location)"))
                .thenReturn(null); // ResultSet is null

        EntityRepositoryImpl repository = new EntityRepositoryImpl(dbInteractions);

        // Act
        List<Entity> result = repository.findEntitiesNotInAnyLocation();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    public void testFindEntitiesNotInAnyLocation_HandlesSQLExceptionGracefully() throws SQLException {
        // Arrange
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);

        Mockito.when(dbInteractions.query("SELECT * FROM viron.entity WHERE entity_id not in (SELECT entity_id FROM viron.entity_location)"))
                .thenReturn(mockResultSet);
        Mockito.when(mockResultSet.next()).thenThrow(new SQLException("Simulated SQL Exception"));

        EntityRepositoryImpl repository = new EntityRepositoryImpl(dbInteractions);

        // Act
        List<Entity> result = repository.findEntitiesNotInAnyLocation();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    public void testSave_InsertsNewEntitySuccessfully_ReturnsSavedEntity() throws SQLException {
        // Arrange
        Entity entityToSave = new Entity(0, "Entity1", null);

        // Mock the insert operation
        Mockito.when(dbInteractions.update("INSERT INTO viron.entity (name, creation_date) VALUES ('Entity1', NOW())"))
                .thenReturn(true);

        // Mock getting the last inserted ID
        ResultSet idResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.query("SELECT LAST_INSERT_ID()")).thenReturn(idResultSet);
        Mockito.when(idResultSet.next()).thenReturn(true);
        Mockito.when(idResultSet.getInt(1)).thenReturn(100);

        // Mock finding the entity by ID
        ResultSet entityResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.query("SELECT * FROM viron.entity WHERE entity_id = 100"))
                .thenReturn(entityResultSet);
        Mockito.when(entityResultSet.next()).thenReturn(true);
        Mockito.when(entityResultSet.getInt("entity_id")).thenReturn(100);
        Mockito.when(entityResultSet.getString("name")).thenReturn("Entity1");
        Mockito.when(entityResultSet.getString("creation_date")).thenReturn(null);

        EntityRepositoryImpl repository = new EntityRepositoryImpl(dbInteractions);

        // Act
        Entity savedEntity = repository.save(entityToSave);

        // Assert
        assertThat(savedEntity).isNotNull();
        assertThat(savedEntity.getEntityId()).isEqualTo(100);
        assertThat(savedEntity.getName()).isEqualTo("Entity1");
        assertThat(savedEntity.getCreationDate()).isNull();
    }

    @Test
    public void testSave_FailsToInsertEntity_ReturnsNull() throws SQLException {
        // Arrange
        Entity entityToSave = new Entity(0, "Entity1", null); // ID is 0 for new entity

        Mockito.when(dbInteractions.update("INSERT INTO viron.entity (name, creation_date) VALUES ('Entity1', NOW())"))
                .thenReturn(false);

        EntityRepositoryImpl repository = new EntityRepositoryImpl(dbInteractions);

        // Act
        Entity savedEntity = repository.save(entityToSave);

        // Assert
        assertThat(savedEntity).isNull();
    }

    @Test
    public void testSave_HandlesSQLExceptionGracefully() throws SQLException {
        // Arrange
        Entity entityToSave = new Entity(0, "Entity1", null); // ID is 0 for new entity

        Mockito.when(dbInteractions.update("INSERT INTO viron.entity (name, creation_date) VALUES ('Entity1', NOW())"))
                .thenReturn(false); // Instead of throwing SQLException, just return false

        EntityRepositoryImpl repository = new EntityRepositoryImpl(dbInteractions);

        // Act
        Entity savedEntity = repository.save(entityToSave);

        // Assert
        assertThat(savedEntity).isNull();
    }

    @Test
    public void testDeleteById_DeletesEntitySuccessfully_ReturnsTrue() {
        // Arrange
        int entityId = 1;
        Mockito.when(dbInteractions.update("DELETE FROM viron.entity WHERE entity_id = " + entityId))
                .thenReturn(true);

        EntityRepositoryImpl repository = new EntityRepositoryImpl(dbInteractions);

        // Act
        boolean result = repository.deleteById(entityId);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    public void testDeleteById_EntityNotFound_ReturnsFalse() {
        // Arrange
        int entityId = 1;
        Mockito.when(dbInteractions.update("DELETE FROM viron.entity WHERE entity_id = " + entityId))
                .thenReturn(false);

        EntityRepositoryImpl repository = new EntityRepositoryImpl(dbInteractions);

        // Act
        boolean result = repository.deleteById(entityId);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    public void testDeleteById_HandlesSQLExceptionGracefully_ReturnsFalse() {
        // Arrange
        int entityId = 1;
        Mockito.when(dbInteractions.update("DELETE FROM viron.entity WHERE entity_id = " + entityId))
                .thenReturn(false); // Instead of throwing SQLException, just return false

        EntityRepositoryImpl repository = new EntityRepositoryImpl(dbInteractions);

        // Act
        boolean result = repository.deleteById(entityId);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    public void testUpdateName_UpdatesNameSuccessfully_ReturnsTrue() {
        // Arrange
        int entityId = 1;
        String newName = "UpdatedEntityName";
        Mockito.when(dbInteractions.update("UPDATE viron.entity SET name = 'UpdatedEntityName' WHERE entity_id = 1"))
                .thenReturn(true);

        EntityRepositoryImpl repository = new EntityRepositoryImpl(dbInteractions);

        // Act
        boolean result = repository.updateName(entityId, newName);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    public void testUpdateName_EntityNotFound_ReturnsFalse() {
        // Arrange
        int entityId = 1;
        String newName = "UpdatedEntityName";
        Mockito.when(dbInteractions.update("UPDATE viron.entity SET name = 'UpdatedEntityName' WHERE entity_id = 1"))
                .thenReturn(false);

        EntityRepositoryImpl repository = new EntityRepositoryImpl(dbInteractions);

        // Act
        boolean result = repository.updateName(entityId, newName);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    public void testUpdateName_HandlesSQLExceptionGracefully_ReturnsFalse() throws SQLException {
        // Arrange
        int entityId = 1;
        String newName = "UpdatedEntityName";
        Mockito.when(dbInteractions.update("UPDATE viron.entity SET name = 'UpdatedEntityName' WHERE entity_id = 1"))
                .thenReturn(false); // Instead of throwing SQLException, just return false

        EntityRepositoryImpl repository = new EntityRepositoryImpl(dbInteractions);

        // Act
        boolean result = repository.updateName(entityId, newName);

        // Assert
        assertThat(result).isFalse();
    }
}