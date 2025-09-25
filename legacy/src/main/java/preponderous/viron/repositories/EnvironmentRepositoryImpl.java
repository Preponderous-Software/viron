package preponderous.viron.repositories;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import preponderous.viron.database.DbInteractions;
import preponderous.viron.models.Environment;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
public class EnvironmentRepositoryImpl implements EnvironmentRepository {
    private final DbInteractions dbInteractions;

    @Autowired
    public EnvironmentRepositoryImpl(DbInteractions dbInteractions) {
        this.dbInteractions = dbInteractions;
    }

    @Override
    public List<Environment> findAll() {
        List<Environment> environments = new ArrayList<>();
        ResultSet rs = dbInteractions.query("SELECT * FROM viron.environment");
        try {
            while (rs.next()) {
                environments.add(mapResultSetToEnvironment(rs));
            }
        } catch (SQLException e) {
            log.error("Error finding all environments: {}", e.getMessage());
        }
        return environments;
    }

    @Override
    public Optional<Environment> findById(int id) {
        ResultSet rs = dbInteractions.query("SELECT * FROM viron.environment WHERE environment_id = " + id);
        try {
            if (rs.next()) {
                return Optional.of(mapResultSetToEnvironment(rs));
            }
        } catch (SQLException e) {
            log.error("Error finding environment by id: {}", e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<Environment> findByName(String name) {
        ResultSet rs = dbInteractions.query("SELECT * FROM viron.environment WHERE name = '" + name + "'");
        try {
            if (rs.next()) {
                return Optional.of(mapResultSetToEnvironment(rs));
            }
        } catch (SQLException e) {
            log.error("Error finding environment by name: {}", e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<Environment> findByEntityId(int entityId) {
        ResultSet rs = dbInteractions.query("SELECT * FROM viron.environment WHERE environment_id = (SELECT environment_id FROM viron.entity WHERE entity_id = " + entityId + ")");
        try {
            if (rs.next()) {
                return Optional.of(mapResultSetToEnvironment(rs));
            }
        } catch (SQLException e) {
            log.error("Error finding environment by entity id: {}", e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Environment save(Environment environment) {
        return environment;
    }

    @Override
    public boolean deleteById(int id) {
        String query = "DELETE FROM viron.environment WHERE environment_id = " + id;
        return dbInteractions.update(query);
    }

    @Override
    public boolean updateName(int id, String name) {
        String query = "UPDATE viron.environment SET name = '" + name + "' WHERE environment_id = " + id;
        return dbInteractions.update(query);
    }

    @Override
    public List<Integer> findEntityIdsByEnvironmentId(int environmentId) {
        List<Integer> entityIds = new ArrayList<>();
        ResultSet rs = dbInteractions.query("SELECT entity_id FROM viron.entity WHERE entity_id in (SELECT entity_id FROM viron.entity_location WHERE location_id in (SELECT location_id FROM viron.location_grid WHERE grid_id in (SELECT grid_id FROM viron.grid_environment WHERE environment_id = " + environmentId + ")))");
        try {
            while (rs.next()) {
                entityIds.add(rs.getInt("entity_id"));
            }
        } catch (SQLException e) {
            log.error("Error finding entity ids: {}", e.getMessage());
        }
        return entityIds;
    }

    @Override
    public List<Integer> findLocationIdsByEnvironmentId(int environmentId) {
        List<Integer> locationIds = new ArrayList<>();
        ResultSet rs = dbInteractions.query("SELECT location_id FROM viron.location_grid WHERE grid_id in (SELECT grid_id FROM viron.grid_environment WHERE environment_id = " + environmentId + ")");
        try {
            while (rs.next()) {
                locationIds.add(rs.getInt("location_id"));
            }
        } catch (SQLException e) {
            log.error("Error finding location ids: {}", e.getMessage());
        }
        return locationIds;
    }

    @Override
    public List<Integer> findGridIdsByEnvironmentId(int environmentId) {
        List<Integer> gridIds = new ArrayList<>();
        ResultSet rs = dbInteractions.query("SELECT grid_id FROM viron.grid_environment WHERE environment_id = " + environmentId);
        try {
            while (rs.next()) {
                gridIds.add(rs.getInt("grid_id"));
            }
        } catch (SQLException e) {
            log.error("Error finding grid ids: {}", e.getMessage());
        }
        return gridIds;
    }

    @Override
    public boolean deleteEntityLocation(int entityId) {
        return dbInteractions.update("DELETE FROM viron.entity_location WHERE entity_id = " + entityId);
    }

    @Override
    public boolean deleteLocationGrid(int locationId) {
        return dbInteractions.update("DELETE FROM viron.location_grid WHERE location_id = " + locationId);
    }

    @Override
    public boolean deleteGridEnvironment(int gridId) {
        return dbInteractions.update("DELETE FROM viron.grid_environment WHERE grid_id = " + gridId);
    }

    @Override
    public boolean deleteEntity(int entityId) {
        return dbInteractions.update("DELETE FROM viron.entity WHERE entity_id = " + entityId);
    }

    @Override
    public boolean deleteLocation(int locationId) {
        return dbInteractions.update("DELETE FROM viron.location WHERE location_id = " + locationId);
    }

    @Override
    public boolean deleteGrid(int gridId) {
        return dbInteractions.update("DELETE FROM viron.grid WHERE grid_id = " + gridId);
    }

    private Environment mapResultSetToEnvironment(ResultSet rs) throws SQLException {
        return new Environment(
                rs.getInt("environment_id"),
                rs.getString("name"),
                rs.getString("creation_date")
        );
    }
}