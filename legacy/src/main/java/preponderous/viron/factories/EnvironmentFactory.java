package preponderous.viron.factories;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import preponderous.viron.database.DbInteractions;
import preponderous.viron.exceptions.EnvironmentCreationException;
import preponderous.viron.models.Environment;

@Component
@Slf4j
public class EnvironmentFactory {
    private final DbInteractions dbInteractions;

    @Autowired
    public EnvironmentFactory(DbInteractions dbInteractions) {
        this.dbInteractions = dbInteractions;
    }

    public Environment createEnvironment(String name, int numGrids, int gridSize) throws EnvironmentCreationException {
        log.info("Attempting to create environment: '{}' with {} grids of size {}", name, numGrids, gridSize);
        
        // create environment
        int id = getNextEnvironmentId();
        if (id == -1) {
            log.error("Failed to get next environment id");
            throw new EnvironmentCreationException("Failed to get next environment id");
        }
        String creationDate = new java.util.Date().toString();
        String query = "INSERT INTO viron.environment (environment_id, name, creation_date) VALUES (" + id + ", '" + name + "', '" + creationDate + "')";
        boolean success = dbInteractions.update(query);
        if (!success) {
            log.error("Failed to create environment");
            throw new EnvironmentCreationException("Failed to create environment");
        }

        // create grids
        List<Integer> gridIds = new ArrayList<>();
        for (int i = 0; i < numGrids; i++) {
            int nextGridId = getNextGridId();
            if (nextGridId == -1) {
                log.error("Failed to get next grid id");
                throw new EnvironmentCreationException("Failed to get next grid id");
            }
            gridIds.add(nextGridId);

            query = "INSERT INTO viron.grid (grid_id, rows, columns) VALUES (" + nextGridId + ", " + gridSize + ", " + gridSize + ")";
            success = dbInteractions.update(query);
            if (!success) {
                log.error("Failed to create grid");
                throw new EnvironmentCreationException("Failed to create grid");
            }

            // associate grid with environment
            query = "INSERT INTO viron.grid_environment (grid_id, environment_id) VALUES (" + nextGridId + ", " + id + ")";
            success = dbInteractions.update(query);
            if (!success) {
                log.error("Failed to associate grid with environment");
                throw new EnvironmentCreationException("Failed to associate grid with environment");
            }

            // create locations
            List<Integer> locationIds = new ArrayList<>();
            for (int x = 0; x < gridSize; x++) {
                for (int y = 0; y < gridSize; y++) {
                    int locationId = getNextLocationId();
                    if (locationId == -1) {
                        log.error("Failed to get next location id");
                        throw new EnvironmentCreationException("Failed to get next location id");
                    }
                    query = "INSERT INTO viron.location (location_id, x, y) VALUES (" + locationId + ", " + x + ", " + y + ")";
                    success = dbInteractions.update(query);
                    if (!success) {
                        log.error("Failed to create location");
                        throw new EnvironmentCreationException("Failed to create location");
                    }

                    // associate location with grid
                    query = "INSERT INTO viron.location_grid (location_id, grid_id) VALUES (" + locationId + ", " + nextGridId + ")";
                    success = dbInteractions.update(query);
                    if (!success) {
                        log.error("Failed to associate location with grid");
                        throw new EnvironmentCreationException("Failed to associate location with grid");
                    }
                    locationIds.add(locationId);
                }
            }
            log.info("Locations created: {}", locationIds);
        }
        log.info("Grids created: {}", gridIds);

        Environment environment = new Environment(id, name, creationDate);
        log.info("Successfully created environment: '{}' with id: {} and creation date: {}", name, id, creationDate);
        
        return environment;
    }

    private int getNextEnvironmentId() {
        ResultSet rs = dbInteractions.query("SELECT nextval('viron.environment_id_seq')");
        if (rs == null) {
            return -1;
        }
        try {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            log.error("Error getting next environment id", e);
        }
        return -1;
    }

    private int getNextGridId() {
        ResultSet rs = dbInteractions.query("SELECT nextval('viron.grid_id_seq')");
        if (rs == null) {
            return -1;
        }
        try {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            log.error("Error getting next grid id", e);
        }
        return -1;
    }

    private int getNextLocationId() {
        ResultSet rs = dbInteractions.query("SELECT nextval('viron.location_id_seq')");
        if (rs == null) {
            return -1;
        }
        try {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            log.error("Error getting next location id", e);
        }
        return -1;
    }
}
