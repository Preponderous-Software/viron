package preponderous.viron.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import preponderous.viron.database.DbInteractions;
import preponderous.viron.models.Environment;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class EnvironmentRepositoryImpl implements EnvironmentRepository {
    private final DbInteractions dbInteractions;

    @Autowired
    public EnvironmentRepositoryImpl(DbInteractions dbInteractions) {
        this.dbInteractions = dbInteractions;
    }

    @Override
    public List<Environment> findAll() {
        return dbInteractions.query("SELECT * FROM viron.environment", this::mapResultSetToEnvironment);
    }

    @Override
    public Optional<Environment> findById(int id) {
        return dbInteractions.queryOne("SELECT * FROM viron.environment WHERE environment_id = ?",
                this::mapResultSetToEnvironment, id);
    }

    @Override
    public Optional<Environment> findByName(String name) {
        return dbInteractions.queryOne("SELECT * FROM viron.environment WHERE name = ?",
                this::mapResultSetToEnvironment, name);
    }

    @Override
    public Optional<Environment> findByEntityId(int entityId) {
        return dbInteractions.queryOne(
                "SELECT * FROM viron.environment WHERE environment_id = (SELECT environment_id FROM viron.entity WHERE entity_id = ?)",
                this::mapResultSetToEnvironment, entityId);
    }

    @Override
    public Environment save(Environment environment) {
        return environment;
    }

    @Override
    public boolean deleteById(int id) {
        String query = "DELETE FROM viron.environment WHERE environment_id = ?";
        return dbInteractions.update(query, id);
    }

    @Override
    public boolean updateName(int id, String name) {
        String query = "UPDATE viron.environment SET name = ? WHERE environment_id = ?";
        return dbInteractions.update(query, name, id);
    }

    @Override
    public List<Integer> findEntityIdsByEnvironmentId(int environmentId) {
        return dbInteractions.query(
                "SELECT entity_id FROM viron.entity WHERE entity_id in (SELECT entity_id FROM viron.entity_location WHERE location_id in (SELECT location_id FROM viron.location_grid WHERE grid_id in (SELECT grid_id FROM viron.grid_environment WHERE environment_id = ?)))",
                rs -> rs.getInt("entity_id"), environmentId);
    }

    @Override
    public List<Integer> findLocationIdsByEnvironmentId(int environmentId) {
        return dbInteractions.query(
                "SELECT location_id FROM viron.location_grid WHERE grid_id in (SELECT grid_id FROM viron.grid_environment WHERE environment_id = ?)",
                rs -> rs.getInt("location_id"), environmentId);
    }

    @Override
    public List<Integer> findGridIdsByEnvironmentId(int environmentId) {
        return dbInteractions.query(
                "SELECT grid_id FROM viron.grid_environment WHERE environment_id = ?",
                rs -> rs.getInt("grid_id"), environmentId);
    }

    @Override
    public boolean deleteEntityLocation(int entityId) {
        return dbInteractions.update("DELETE FROM viron.entity_location WHERE entity_id = ?", entityId);
    }

    @Override
    public boolean deleteLocationGrid(int locationId) {
        return dbInteractions.update("DELETE FROM viron.location_grid WHERE location_id = ?", locationId);
    }

    @Override
    public boolean deleteGridEnvironment(int gridId) {
        return dbInteractions.update("DELETE FROM viron.grid_environment WHERE grid_id = ?", gridId);
    }

    @Override
    public boolean deleteEntity(int entityId) {
        return dbInteractions.update("DELETE FROM viron.entity WHERE entity_id = ?", entityId);
    }

    @Override
    public boolean deleteLocation(int locationId) {
        return dbInteractions.update("DELETE FROM viron.location WHERE location_id = ?", locationId);
    }

    @Override
    public boolean deleteGrid(int gridId) {
        return dbInteractions.update("DELETE FROM viron.grid WHERE grid_id = ?", gridId);
    }

    private Environment mapResultSetToEnvironment(ResultSet rs) throws SQLException {
        return new Environment(
                rs.getInt("environment_id"),
                rs.getString("name"),
                rs.getString("creation_date")
        );
    }
}
