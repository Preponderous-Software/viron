package preponderous.viron.repositories;

import org.springframework.stereotype.Repository;
import preponderous.viron.database.DbInteractions;
import preponderous.viron.models.Grid;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class GridRepositoryImpl implements GridRepository {
    private final DbInteractions dbInteractions;

    public GridRepositoryImpl(DbInteractions dbInteractions) {
        this.dbInteractions = dbInteractions;
    }

    private Grid mapResultSetToGrid(ResultSet rs) throws SQLException {
        int id = rs.getInt("grid_id");
        int rows = rs.getInt("rows");
        int columns = rs.getInt("columns");
        String name = rs.getString("name");
        return new Grid(id, rows, columns, name);
    }

    @Override
    public List<Grid> findAll() {
        return dbInteractions.query("SELECT * FROM viron.grid", this::mapResultSetToGrid);
    }

    @Override
    public Optional<Grid> findById(int id) {
        return dbInteractions.queryOne("SELECT * FROM viron.grid WHERE grid_id = ?", this::mapResultSetToGrid, id);
    }

    @Override
    public List<Grid> findByEnvironmentId(int environmentId) {
        String query = "SELECT * FROM viron.grid WHERE grid_id in " +
                "(SELECT grid_id FROM viron.grid_environment WHERE environment_id = ?)";
        return dbInteractions.query(query, this::mapResultSetToGrid, environmentId);
    }

    @Override
    public Optional<Grid> findByEntityId(int entityId) {
        String query = "SELECT * FROM viron.grid WHERE grid_id in " +
                "(SELECT grid_id FROM viron.location_grid WHERE location_id in " +
                "(SELECT location_id FROM viron.entity_location WHERE entity_id = ?))";
        return dbInteractions.queryOne(query, this::mapResultSetToGrid, entityId);
    }

    @Override
    public boolean updateName(int id, String name) {
        String query = "UPDATE viron.grid SET name = ? WHERE grid_id = ?";
        return dbInteractions.update(query, name, id);
    }
}
