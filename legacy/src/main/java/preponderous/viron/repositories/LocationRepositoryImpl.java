package preponderous.viron.repositories;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import preponderous.viron.database.DbInteractions;
import preponderous.viron.models.Location;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
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
        List<Location> locations = new ArrayList<>();
        ResultSet rs = dbInteractions.query("SELECT * FROM viron.location");
        if (rs == null) {
            log.error("Error getting locations: ResultSet is null");
            return locations;
        }
        try {
            while (rs.next()) {
                locations.add(mapResultSetToLocation(rs));
            }
        } catch (SQLException e) {
            log.error("Error getting locations: {}", e.getMessage());
        }
        return locations;
    }

    @Override
    public Optional<Location> findById(int id) {
        ResultSet rs = dbInteractions.query("SELECT * FROM viron.location WHERE location_id = " + id);
        try {
            if (rs != null && rs.next()) {
                return Optional.of(mapResultSetToLocation(rs));
            }
        } catch (SQLException e) {
            log.error("Error getting location by id: {}", e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Location> findByEnvironmentId(int environmentId) {
        List<Location> locations = new ArrayList<>();
        String query = "SELECT * FROM viron.location WHERE location_id in " +
                "(SELECT location_id FROM viron.location_grid WHERE grid_id in " +
                "(SELECT grid_id FROM viron.grid_environment WHERE environment_id = " + environmentId + "))";
        ResultSet rs = dbInteractions.query(query);
        try {
            while (rs != null && rs.next()) {
                locations.add(mapResultSetToLocation(rs));
            }
        } catch (SQLException e) {
            log.error("Error getting locations in environment: {}", e.getMessage());
        }
        return locations;
    }

    @Override
    public List<Location> findByGridId(int gridId) {
        List<Location> locations = new ArrayList<>();
        String query = "SELECT * FROM viron.location WHERE location_id in " +
                "(SELECT location_id FROM viron.location_grid WHERE grid_id = " + gridId + ")";
        ResultSet rs = dbInteractions.query(query);
        try {
            while (rs != null && rs.next()) {
                locations.add(mapResultSetToLocation(rs));
            }
        } catch (SQLException e) {
            log.error("Error getting locations in grid: {}", e.getMessage());
        }
        return locations;
    }

    @Override
    public Optional<Location> findByEntityId(int entityId) {
        String query = "SELECT * FROM viron.location WHERE location_id in " +
                "(SELECT location_id FROM viron.entity_location WHERE entity_id = " + entityId + ")";
        ResultSet rs = dbInteractions.query(query);
        try {
            if (rs != null && rs.next()) {
                return Optional.of(mapResultSetToLocation(rs));
            }
        } catch (SQLException e) {
            log.error("Error getting location of entity: {}", e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public boolean addEntityToLocation(int entityId, int locationId) {
        String query = "INSERT INTO viron.entity_location (entity_id, location_id) VALUES (" +
                entityId + ", " + locationId + ")";
        return dbInteractions.update(query);
    }

    @Override
    public boolean removeEntityFromLocation(int entityId, int locationId) {
        String query = "DELETE FROM viron.entity_location WHERE entity_id = " +
                entityId + " AND location_id = " + locationId;
        return dbInteractions.update(query);
    }

    @Override
    public boolean removeEntityFromCurrentLocation(int entityId) {
        String query = "DELETE FROM viron.entity_location WHERE entity_id = " + entityId;
        return dbInteractions.update(query);
    }
}