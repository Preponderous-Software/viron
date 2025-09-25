package preponderous.viron.repositories;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import preponderous.viron.database.DbInteractions;
import preponderous.viron.models.Grid;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
public class GridRepositoryImpl implements GridRepository {
    private final DbInteractions dbInteractions;

    public GridRepositoryImpl(DbInteractions dbInteractions) {
        this.dbInteractions = dbInteractions;
    }

    private Grid mapResultSetToGrid(ResultSet rs) throws SQLException {
        int id = rs.getInt("grid_id");
        int rows = rs.getInt("rows");
        int columns = rs.getInt("columns");
        return new Grid(id, rows, columns);
    }

    @Override
    public List<Grid> findAll() {
        List<Grid> grids = new ArrayList<>();
        ResultSet rs = dbInteractions.query("SELECT * FROM viron.grid");
        if (rs == null) {
            log.error("Error getting grids: ResultSet is null");
            return grids;
        }
        try {
            while (rs.next()) {
                grids.add(mapResultSetToGrid(rs));
            }
        } catch (SQLException e) {
            log.error("Error getting grids: {}", e.getMessage());
        }
        return grids;
    }

    @Override
    public Optional<Grid> findById(int id) {
        ResultSet rs = dbInteractions.query("SELECT * FROM viron.grid WHERE grid_id = " + id);
        if (rs == null) {
            log.error("Error getting grid by id: ResultSet is null");
            return Optional.empty();
        }
        try {
            if (rs.next()) {
                return Optional.of(mapResultSetToGrid(rs));
            }
        } catch (SQLException e) {
            log.error("Error getting grid by id: {}", e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Grid> findByEnvironmentId(int environmentId) {
        List<Grid> grids = new ArrayList<>();
        String query = "SELECT * FROM viron.grid WHERE grid_id in " +
                "(SELECT grid_id FROM viron.grid_environment WHERE environment_id = " + environmentId + ")";
        ResultSet rs = dbInteractions.query(query);
        if (rs == null) {
            log.error("Error getting grids for environment: ResultSet is null");
            return grids;
        }
        try {
            while (rs.next()) {
                grids.add(mapResultSetToGrid(rs));
            }
        } catch (SQLException e) {
            log.error("Error getting grids for environment: {}", e.getMessage());
        }
        return grids;
    }

    @Override
    public Optional<Grid> findByEntityId(int entityId) {
        String query = "SELECT * FROM viron.grid WHERE grid_id in " +
                "(SELECT grid_id FROM viron.location_grid WHERE location_id in " +
                "(SELECT location_id FROM viron.entity_location WHERE entity_id = " + entityId + "))";
        ResultSet rs = dbInteractions.query(query);
        if (rs == null) {
            log.error("Error getting grid of entity: ResultSet is null");
            return Optional.empty();
        }
        try {
            if (rs.next()) {
                return Optional.of(mapResultSetToGrid(rs));
            }
        } catch (SQLException e) {
            log.error("Error getting grid of entity: {}", e.getMessage());
        }
        return Optional.empty();
    }
}