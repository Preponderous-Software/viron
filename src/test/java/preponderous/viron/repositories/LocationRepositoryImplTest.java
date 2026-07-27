package preponderous.viron.repositories;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import preponderous.viron.database.DbInteractions;
import preponderous.viron.models.Location;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static preponderous.viron.database.ResultSetAnswers.mapsAllRows;
import static preponderous.viron.database.ResultSetAnswers.mapsFirstRow;

@SpringBootTest
public class LocationRepositoryImplTest {

    @MockBean
    private DbInteractions dbInteractions;

    // ---- findAll ----

    @Test
    public void testFindAll_ReturnsLocationsWhenResultSetIsNotEmpty() throws SQLException {
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.<Location>query(eq("SELECT * FROM viron.location"), any())).thenAnswer(mapsAllRows(mockResultSet));
        Mockito.when(mockResultSet.next()).thenReturn(true, true, false);
        Mockito.when(mockResultSet.getInt("location_id")).thenReturn(1, 2);
        Mockito.when(mockResultSet.getInt("x")).thenReturn(3, 7);
        Mockito.when(mockResultSet.getInt("y")).thenReturn(4, 8);

        LocationRepositoryImpl repository = new LocationRepositoryImpl(dbInteractions);

        List<Location> result = repository.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getLocationId()).isEqualTo(1);
        assertThat(result.get(0).getX()).isEqualTo(3);
        assertThat(result.get(0).getY()).isEqualTo(4);
        assertThat(result.get(1).getLocationId()).isEqualTo(2);
        assertThat(result.get(1).getX()).isEqualTo(7);
        assertThat(result.get(1).getY()).isEqualTo(8);
    }

    @Test
    public void testFindAll_ReturnsEmptyListWhenResultSetIsEmpty() throws SQLException {
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.<Location>query(eq("SELECT * FROM viron.location"), any())).thenAnswer(mapsAllRows(mockResultSet));
        Mockito.when(mockResultSet.next()).thenReturn(false);

        LocationRepositoryImpl repository = new LocationRepositoryImpl(dbInteractions);

        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    public void testFindAll_ReturnsEmptyListWhenQueryFails() {
        Mockito.when(dbInteractions.<Location>query(eq("SELECT * FROM viron.location"), any())).thenReturn(Collections.emptyList());

        LocationRepositoryImpl repository = new LocationRepositoryImpl(dbInteractions);

        assertThat(repository.findAll()).isEmpty();
    }

    // ---- findById ----

    @Test
    public void testFindById_ReturnsLocationWhenRowExists() throws SQLException {
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.<Location>queryOne(eq("SELECT * FROM viron.location WHERE location_id = ?"), any(), eq(1))).thenAnswer(mapsFirstRow(mockResultSet));
        Mockito.when(mockResultSet.next()).thenReturn(true);
        Mockito.when(mockResultSet.getInt("location_id")).thenReturn(1);
        Mockito.when(mockResultSet.getInt("x")).thenReturn(3);
        Mockito.when(mockResultSet.getInt("y")).thenReturn(4);

        LocationRepositoryImpl repository = new LocationRepositoryImpl(dbInteractions);

        Optional<Location> result = repository.findById(1);

        assertThat(result).isPresent();
        assertThat(result.get().getLocationId()).isEqualTo(1);
        assertThat(result.get().getX()).isEqualTo(3);
        assertThat(result.get().getY()).isEqualTo(4);
    }

    @Test
    public void testFindById_ReturnsEmptyWhenNoRow() throws SQLException {
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.<Location>queryOne(eq("SELECT * FROM viron.location WHERE location_id = ?"), any(), eq(99))).thenAnswer(mapsFirstRow(mockResultSet));
        Mockito.when(mockResultSet.next()).thenReturn(false);

        LocationRepositoryImpl repository = new LocationRepositoryImpl(dbInteractions);

        assertThat(repository.findById(99)).isEmpty();
    }

    @Test
    public void testFindById_ReturnsEmptyWhenQueryFails() {
        Mockito.when(dbInteractions.<Location>queryOne(eq("SELECT * FROM viron.location WHERE location_id = ?"), any(), eq(1))).thenReturn(Optional.empty());

        LocationRepositoryImpl repository = new LocationRepositoryImpl(dbInteractions);

        assertThat(repository.findById(1)).isEmpty();
    }

    // ---- findByEnvironmentId ----

    @Test
    public void testFindByEnvironmentId_ReturnsLocationsWhenRowsExist() throws SQLException {
        String query = "SELECT * FROM viron.location WHERE location_id in " +
                "(SELECT location_id FROM viron.location_grid WHERE grid_id in " +
                "(SELECT grid_id FROM viron.grid_environment WHERE environment_id = ?))";
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.<Location>query(eq(query), any(), eq(7))).thenAnswer(mapsAllRows(mockResultSet));
        Mockito.when(mockResultSet.next()).thenReturn(true, false);
        Mockito.when(mockResultSet.getInt("location_id")).thenReturn(5);
        Mockito.when(mockResultSet.getInt("x")).thenReturn(1);
        Mockito.when(mockResultSet.getInt("y")).thenReturn(2);

        LocationRepositoryImpl repository = new LocationRepositoryImpl(dbInteractions);

        List<Location> result = repository.findByEnvironmentId(7);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLocationId()).isEqualTo(5);
    }

    @Test
    public void testFindByEnvironmentId_ReturnsEmptyListWhenQueryFails() {
        String query = "SELECT * FROM viron.location WHERE location_id in " +
                "(SELECT location_id FROM viron.location_grid WHERE grid_id in " +
                "(SELECT grid_id FROM viron.grid_environment WHERE environment_id = ?))";
        Mockito.when(dbInteractions.<Location>query(eq(query), any(), eq(7))).thenReturn(Collections.emptyList());

        LocationRepositoryImpl repository = new LocationRepositoryImpl(dbInteractions);

        assertThat(repository.findByEnvironmentId(7)).isEmpty();
    }

    // ---- findByGridId ----

    @Test
    public void testFindByGridId_ReturnsLocationsWhenRowsExist() throws SQLException {
        String query = "SELECT * FROM viron.location WHERE location_id in " +
                "(SELECT location_id FROM viron.location_grid WHERE grid_id = ?)";
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.<Location>query(eq(query), any(), eq(3))).thenAnswer(mapsAllRows(mockResultSet));
        Mockito.when(mockResultSet.next()).thenReturn(true, true, false);
        Mockito.when(mockResultSet.getInt("location_id")).thenReturn(5, 6);
        Mockito.when(mockResultSet.getInt("x")).thenReturn(1, 2);
        Mockito.when(mockResultSet.getInt("y")).thenReturn(1, 2);

        LocationRepositoryImpl repository = new LocationRepositoryImpl(dbInteractions);

        List<Location> result = repository.findByGridId(3);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getLocationId()).isEqualTo(5);
        assertThat(result.get(1).getLocationId()).isEqualTo(6);
    }

    @Test
    public void testFindByGridId_ReturnsEmptyListWhenQueryFails() {
        String query = "SELECT * FROM viron.location WHERE location_id in " +
                "(SELECT location_id FROM viron.location_grid WHERE grid_id = ?)";
        Mockito.when(dbInteractions.<Location>query(eq(query), any(), eq(3))).thenReturn(Collections.emptyList());

        LocationRepositoryImpl repository = new LocationRepositoryImpl(dbInteractions);

        assertThat(repository.findByGridId(3)).isEmpty();
    }

    // ---- findUnoccupiedByGridId ----

    @Test
    public void testFindUnoccupiedByGridId_ReturnsLocationsWhenRowsExist() throws SQLException {
        String query = "SELECT * FROM viron.location WHERE location_id in " +
                "(SELECT location_id FROM viron.location_grid WHERE grid_id = ?) " +
                "AND location_id not in (SELECT location_id FROM viron.entity_location)";
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.<Location>query(eq(query), any(), eq(3))).thenAnswer(mapsAllRows(mockResultSet));
        Mockito.when(mockResultSet.next()).thenReturn(true, true, false);
        Mockito.when(mockResultSet.getInt("location_id")).thenReturn(5, 6);
        Mockito.when(mockResultSet.getInt("x")).thenReturn(1, 2);
        Mockito.when(mockResultSet.getInt("y")).thenReturn(1, 2);

        LocationRepositoryImpl repository = new LocationRepositoryImpl(dbInteractions);

        List<Location> result = repository.findUnoccupiedByGridId(3);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getLocationId()).isEqualTo(5);
        assertThat(result.get(1).getLocationId()).isEqualTo(6);
    }

    @Test
    public void testFindUnoccupiedByGridId_ReturnsEmptyListWhenQueryFails() {
        String query = "SELECT * FROM viron.location WHERE location_id in " +
                "(SELECT location_id FROM viron.location_grid WHERE grid_id = ?) " +
                "AND location_id not in (SELECT location_id FROM viron.entity_location)";
        Mockito.when(dbInteractions.<Location>query(eq(query), any(), eq(3))).thenReturn(Collections.emptyList());

        LocationRepositoryImpl repository = new LocationRepositoryImpl(dbInteractions);

        assertThat(repository.findUnoccupiedByGridId(3)).isEmpty();
    }

    // ---- findByEntityId ----

    @Test
    public void testFindByEntityId_ReturnsLocationWhenRowExists() throws SQLException {
        String query = "SELECT * FROM viron.location WHERE location_id in " +
                "(SELECT location_id FROM viron.entity_location WHERE entity_id = ?)";
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.<Location>queryOne(eq(query), any(), eq(42))).thenAnswer(mapsFirstRow(mockResultSet));
        Mockito.when(mockResultSet.next()).thenReturn(true);
        Mockito.when(mockResultSet.getInt("location_id")).thenReturn(8);
        Mockito.when(mockResultSet.getInt("x")).thenReturn(0);
        Mockito.when(mockResultSet.getInt("y")).thenReturn(0);

        LocationRepositoryImpl repository = new LocationRepositoryImpl(dbInteractions);

        Optional<Location> result = repository.findByEntityId(42);

        assertThat(result).isPresent();
        assertThat(result.get().getLocationId()).isEqualTo(8);
    }

    @Test
    public void testFindByEntityId_ReturnsEmptyWhenQueryFails() {
        String query = "SELECT * FROM viron.location WHERE location_id in " +
                "(SELECT location_id FROM viron.entity_location WHERE entity_id = ?)";
        Mockito.when(dbInteractions.<Location>queryOne(eq(query), any(), eq(42))).thenReturn(Optional.empty());

        LocationRepositoryImpl repository = new LocationRepositoryImpl(dbInteractions);

        assertThat(repository.findByEntityId(42)).isEmpty();
    }

    // ---- addEntityToLocation / removeEntityFromLocation / removeEntityFromCurrentLocation ----

    @Test
    public void testAddEntityToLocation_DelegatesToUpdateAndReturnsResult() {
        String query = "INSERT INTO viron.entity_location (entity_id, location_id) VALUES (?, ?)";
        Mockito.when(dbInteractions.update(query, 42, 8)).thenReturn(true);

        LocationRepositoryImpl repository = new LocationRepositoryImpl(dbInteractions);

        assertThat(repository.addEntityToLocation(42, 8)).isTrue();
        Mockito.verify(dbInteractions).update(query, 42, 8);
    }

    @Test
    public void testAddEntityToLocation_ReturnsFalseWhenUpdateFails() {
        String query = "INSERT INTO viron.entity_location (entity_id, location_id) VALUES (?, ?)";
        Mockito.when(dbInteractions.update(query, 42, 8)).thenReturn(false);

        LocationRepositoryImpl repository = new LocationRepositoryImpl(dbInteractions);

        assertThat(repository.addEntityToLocation(42, 8)).isFalse();
    }

    @Test
    public void testRemoveEntityFromLocation_DelegatesToUpdateAndReturnsResult() {
        String query = "DELETE FROM viron.entity_location WHERE entity_id = ? AND location_id = ?";
        Mockito.when(dbInteractions.update(query, 42, 8)).thenReturn(true);

        LocationRepositoryImpl repository = new LocationRepositoryImpl(dbInteractions);

        assertThat(repository.removeEntityFromLocation(42, 8)).isTrue();
        Mockito.verify(dbInteractions).update(query, 42, 8);
    }

    @Test
    public void testRemoveEntityFromCurrentLocation_DelegatesToUpdateAndReturnsResult() {
        String query = "DELETE FROM viron.entity_location WHERE entity_id = ?";
        Mockito.when(dbInteractions.update(query, 42)).thenReturn(true);

        LocationRepositoryImpl repository = new LocationRepositoryImpl(dbInteractions);

        assertThat(repository.removeEntityFromCurrentLocation(42)).isTrue();
        Mockito.verify(dbInteractions).update(query, 42);
    }

    // ---- getEntityIdsAtLocation ----

    @Test
    public void testGetEntityIdsAtLocation_ReturnsIdsWhenResultSetNotEmpty() throws SQLException {
        ResultSet rs = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.<Integer>query(eq("SELECT entity_id FROM viron.entity_location WHERE location_id = ?"), any(), eq(7)))
                .thenAnswer(mapsAllRows(rs));
        Mockito.when(rs.next()).thenReturn(true, true, false);
        Mockito.when(rs.getInt("entity_id")).thenReturn(11, 22);

        LocationRepositoryImpl repository = new LocationRepositoryImpl(dbInteractions);

        assertThat(repository.getEntityIdsAtLocation(7)).containsExactly(11, 22);
    }

    @Test
    public void testGetEntityIdsAtLocation_ReturnsEmptyWhenQueryFails() {
        Mockito.when(dbInteractions.<Integer>query(Mockito.anyString(), any(), any())).thenReturn(Collections.emptyList());

        LocationRepositoryImpl repository = new LocationRepositoryImpl(dbInteractions);

        assertThat(repository.getEntityIdsAtLocation(7)).isEmpty();
    }

    // ---- getGridIdOfLocation ----

    @Test
    public void testGetGridIdOfLocation_ReturnsGridWhenPresent() throws SQLException {
        ResultSet rs = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.<Integer>queryOne(eq("SELECT grid_id FROM viron.location_grid WHERE location_id = ?"), any(), eq(5)))
                .thenAnswer(mapsFirstRow(rs));
        Mockito.when(rs.next()).thenReturn(true);
        Mockito.when(rs.getInt("grid_id")).thenReturn(3);

        LocationRepositoryImpl repository = new LocationRepositoryImpl(dbInteractions);

        assertThat(repository.getGridIdOfLocation(5)).contains(3);
    }

    @Test
    public void testGetGridIdOfLocation_EmptyWhenNoRow() throws SQLException {
        ResultSet rs = Mockito.mock(ResultSet.class);
        Mockito.when(dbInteractions.<Integer>queryOne(Mockito.anyString(), any(), any())).thenAnswer(mapsFirstRow(rs));
        Mockito.when(rs.next()).thenReturn(false);

        LocationRepositoryImpl repository = new LocationRepositoryImpl(dbInteractions);

        assertThat(repository.getGridIdOfLocation(5)).isEmpty();
    }

    // ---- moveEntityToLocation ----

    @Test
    public void testMoveEntityToLocation_DelegatesToUpdate() {
        String query = "UPDATE viron.entity_location SET location_id = ? WHERE entity_id = ?";
        Mockito.when(dbInteractions.update(query, 9, 4)).thenReturn(true);

        LocationRepositoryImpl repository = new LocationRepositoryImpl(dbInteractions);

        assertThat(repository.moveEntityToLocation(4, 9)).isTrue();
        Mockito.verify(dbInteractions).update(query, 9, 4);
    }
}
