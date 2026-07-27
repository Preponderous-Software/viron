package preponderous.viron.repositories;

import org.springframework.stereotype.Repository;
import preponderous.viron.database.DbInteractions;
import preponderous.viron.models.Location;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class LocationRepositoryImpl implements LocationRepository {
    private final DbInteractions dbInteractions;

    public LocationRepositoryImpl(DbInteractions dbInteractions) {
        this.dbInteractions = dbInteractions;
    }

    private Location mapResultSetToLocation(ResultSet rs) throws SQLException {
        int id = rs.getInt("location_id");
        int x = rs.getInt("x");
        int y = rs.getInt("y");
        return new Location(id, x, y);
    }

    @Override
    public List<Location> findAll() {
        return dbInteractions.query("SELECT * FROM viron.location", this::mapResultSetToLocation);
    }

    @Override
    public Optional<Location> findById(int id) {
        return dbInteractions.queryOne("SELECT * FROM viron.location WHERE location_id = ?",
                this::mapResultSetToLocation, id);
    }

    @Override
    public List<Location> findByEnvironmentId(int environmentId) {
        String query = "SELECT * FROM viron.location WHERE location_id in " +
                "(SELECT location_id FROM viron.location_grid WHERE grid_id in " +
                "(SELECT grid_id FROM viron.grid_environment WHERE environment_id = ?))";
        return dbInteractions.query(query, this::mapResultSetToLocation, environmentId);
    }

    @Override
    public List<Location> findByGridId(int gridId) {
        String query = "SELECT * FROM viron.location WHERE location_id in " +
                "(SELECT location_id FROM viron.location_grid WHERE grid_id = ?)";
        return dbInteractions.query(query, this::mapResultSetToLocation, gridId);
    }

    @Override
    public Optional<Location> findByEntityId(int entityId) {
        String query = "SELECT * FROM viron.location WHERE location_id in " +
                "(SELECT location_id FROM viron.entity_location WHERE entity_id = ?)";
        return dbInteractions.queryOne(query, this::mapResultSetToLocation, entityId);
    }

    @Override
    public boolean addEntityToLocation(int entityId, int locationId) {
        String query = "INSERT INTO viron.entity_location (entity_id, location_id) VALUES (?, ?)";
        return dbInteractions.update(query, entityId, locationId);
    }

    @Override
    public boolean removeEntityFromLocation(int entityId, int locationId) {
        String query = "DELETE FROM viron.entity_location WHERE entity_id = ? AND location_id = ?";
        return dbInteractions.update(query, entityId, locationId);
    }

    @Override
    public boolean removeEntityFromCurrentLocation(int entityId) {
        String query = "DELETE FROM viron.entity_location WHERE entity_id = ?";
        return dbInteractions.update(query, entityId);
    }

    @Override
    public List<Integer> getEntityIdsAtLocation(int locationId) {
        String query = "SELECT entity_id FROM viron.entity_location WHERE location_id = ?";
        return dbInteractions.query(query, rs -> rs.getInt("entity_id"), locationId);
    }

    @Override
    public List<Location> findUnoccupiedByGridId(int gridId) {
        String query = "SELECT * FROM viron.location WHERE location_id in " +
                "(SELECT location_id FROM viron.location_grid WHERE grid_id = ?) " +
                "AND location_id not in (SELECT location_id FROM viron.entity_location)";
        return dbInteractions.query(query, this::mapResultSetToLocation, gridId);
    }

    @Override
    public Optional<Integer> getGridIdOfLocation(int locationId) {
        String query = "SELECT grid_id FROM viron.location_grid WHERE location_id = ?";
        return dbInteractions.queryOne(query, rs -> rs.getInt("grid_id"), locationId);
    }

    @Override
    public boolean moveEntityToLocation(int entityId, int targetLocationId) {
        // Single atomic statement: re-point the entity's existing placement to the target.
        String query = "UPDATE viron.entity_location SET location_id = ? WHERE entity_id = ?";
        return dbInteractions.update(query, targetLocationId, entityId);
    }
}
