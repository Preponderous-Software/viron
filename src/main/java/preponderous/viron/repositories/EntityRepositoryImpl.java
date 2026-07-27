package preponderous.viron.repositories;

import org.springframework.stereotype.Repository;
import preponderous.viron.database.DbInteractions;
import preponderous.viron.models.Entity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
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
        return dbInteractions.query("SELECT * FROM viron.entity", this::mapResultSetToEntity);
    }

    @Override
    public Optional<Entity> findById(int id) {
        return dbInteractions.queryOne("SELECT * FROM viron.entity WHERE entity_id = ?",
                this::mapResultSetToEntity, id);
    }

    @Override
    public List<Entity> findByEnvironmentId(int environmentId) {
        String query = "SELECT * FROM viron.entity WHERE entity_id in " +
                "(SELECT entity_id FROM viron.entity_location WHERE location_id in " +
                "(SELECT location_id FROM viron.location_grid WHERE grid_id in " +
                "(SELECT grid_id FROM viron.grid_environment WHERE environment_id = ?)))";

        return dbInteractions.query(query, this::mapResultSetToEntity, environmentId);
    }

    @Override
    public List<Entity> findByGridId(int gridId) {
        String query = "SELECT * FROM viron.entity WHERE entity_id in " +
                "(SELECT entity_id FROM viron.entity_location WHERE location_id in " +
                "(SELECT location_id FROM viron.location_grid WHERE grid_id = ?))";

        return dbInteractions.query(query, this::mapResultSetToEntity, gridId);
    }

    @Override
    public List<Entity> findByLocationId(int locationId) {
        String query = "SELECT * FROM viron.entity WHERE entity_id in " +
                "(SELECT entity_id FROM viron.entity_location WHERE location_id = ?)";

        return dbInteractions.query(query, this::mapResultSetToEntity, locationId);
    }

    @Override
    public List<Entity> findEntitiesNotInAnyLocation() {
        String query = "SELECT * FROM viron.entity WHERE entity_id not in " +
                "(SELECT entity_id FROM viron.entity_location)";

        return dbInteractions.query(query, this::mapResultSetToEntity);
    }

    @Override
    public Entity save(Entity entity) {
        if (entity.getEntityId() == 0) {
            String query = "INSERT INTO viron.entity (name, creation_date) VALUES (?, NOW())";
            if (dbInteractions.update(query, entity.getName())) {
                // Get the last inserted id
                Optional<Integer> lastInsertedId =
                        dbInteractions.queryOne("SELECT LAST_INSERT_ID()", rs -> rs.getInt(1));
                return lastInsertedId.flatMap(this::findById).orElse(null);
            }
        }
        return null;
    }

    @Override
    public boolean deleteById(int id) {
        String query = "DELETE FROM viron.entity WHERE entity_id = ?";
        return dbInteractions.update(query, id);
    }

    @Override
    public boolean updateName(int id, String name) {
        String query = "UPDATE viron.entity SET name = ? WHERE entity_id = ?";
        return dbInteractions.update(query, name, id);
    }
}
