package preponderous.viron.repositories;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import preponderous.viron.database.DbInteractions;
import preponderous.viron.models.Entity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
public class EntityRepositoryImpl implements EntityRepository {
    private final DbInteractions dbInteractions;

    public EntityRepositoryImpl(DbInteractions dbInteractions) {
        this.dbInteractions = dbInteractions;
    }

    private Entity mapResultSetToEntity(ResultSet rs) throws SQLException {
        int id = rs.getInt("entity_id");
        String name = rs.getString("name");
        String creationDate = rs.getString("creation_date");
        return new Entity(id, name, creationDate);
    }

    @Override
    public List<Entity> findAll() {
        List<Entity> entities = new ArrayList<>();
        ResultSet rs = dbInteractions.query("SELECT * FROM viron.entity");
        if (rs == null) {
            log.error("Error getting entities: ResultSet is null");
            return entities;
        }
        try {
            while (rs.next()) {
                entities.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            log.error("Error getting entities: {}", e.getMessage());
        }
        return entities;
    }

    @Override
    public Optional<Entity> findById(int id) {
        ResultSet rs = dbInteractions.query("SELECT * FROM viron.entity WHERE entity_id = " + id);
        if (rs == null) {
            log.error("Error getting entity by id: ResultSet is null");
            return Optional.empty();
        }
        try {
            if (rs.next()) {
                return Optional.of(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            log.error("Error getting entity by id: {}", e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Entity> findByEnvironmentId(int environmentId) {
        List<Entity> entities = new ArrayList<>();
        String query = "SELECT * FROM viron.entity WHERE entity_id in " +
                "(SELECT entity_id FROM viron.entity_location WHERE location_id in " +
                "(SELECT location_id FROM viron.location_grid WHERE grid_id in " +
                "(SELECT grid_id FROM viron.grid_environment WHERE environment_id = " + environmentId + ")))";

        ResultSet rs = dbInteractions.query(query);
        if (rs == null) {
            log.error("Error getting entities in environment: ResultSet is null");
            return entities;
        }
        try {
            while (rs.next()) {
                entities.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            log.error("Error getting entities in environment: {}", e.getMessage());
        }
        return entities;
    }

    @Override
    public List<Entity> findByGridId(int gridId) {
        List<Entity> entities = new ArrayList<>();
        String query = "SELECT * FROM viron.entity WHERE entity_id in " +
                "(SELECT entity_id FROM viron.entity_location WHERE location_id in " +
                "(SELECT location_id FROM viron.location_grid WHERE grid_id = " + gridId + "))";

        ResultSet rs = dbInteractions.query(query);
        if (rs == null) {
            log.error("Error getting entities in grid: ResultSet is null");
            return entities;
        }
        try {
            while (rs.next()) {
                entities.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            log.error("Error getting entities in grid: {}", e.getMessage());
        }
        return entities;
    }

    @Override
    public List<Entity> findByLocationId(int locationId) {
        List<Entity> entities = new ArrayList<>();
        String query = "SELECT * FROM viron.entity WHERE entity_id in " +
                "(SELECT entity_id FROM viron.entity_location WHERE location_id = " + locationId + ")";

        ResultSet rs = dbInteractions.query(query);
        if (rs == null) {
            log.error("Error getting entities in location: ResultSet is null");
            return entities;
        }
        try {
            while (rs.next()) {
                entities.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            log.error("Error getting entities in location: {}", e.getMessage());
        }
        return entities;
    }

    @Override
    public List<Entity> findEntitiesNotInAnyLocation() {
        List<Entity> entities = new ArrayList<>();
        String query = "SELECT * FROM viron.entity WHERE entity_id not in " +
                "(SELECT entity_id FROM viron.entity_location)";

        ResultSet rs = dbInteractions.query(query);
        if (rs == null) {
            log.error("Error getting entities not in any location: ResultSet is null");
            return entities;
        }
        try {
            while (rs.next()) {
                entities.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            log.error("Error getting entities not in any location: {}", e.getMessage());
        }
        return entities;
    }

    @Override
    public Entity save(Entity entity) {
        if (entity.getEntityId() == 0) {
            String query = String.format("INSERT INTO viron.entity (name, creation_date) VALUES ('%s', NOW())",
                    entity.getName());
            if (dbInteractions.update(query)) {
                // Get the last inserted id
                ResultSet rs = dbInteractions.query("SELECT LAST_INSERT_ID()");
                try {
                    if (rs != null && rs.next()) {
                        return findById(rs.getInt(1)).orElse(null);
                    }
                } catch (SQLException e) {
                    log.error("Error getting last inserted id: {}", e.getMessage());
                }
            }
        }
        return null;
    }

    @Override
    public boolean deleteById(int id) {
        String query = "DELETE FROM viron.entity WHERE entity_id = " + id;
        return dbInteractions.update(query);
    }

    @Override
    public boolean updateName(int id, String name) {
        String query = String.format("UPDATE viron.entity SET name = '%s' WHERE entity_id = %d",
                name, id);
        return dbInteractions.update(query);
    }
}