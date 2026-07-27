package preponderous.viron.factories;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
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

    /**
     * Creates an environment containing {@code numGrids} grids, each {@code numRows} by
     * {@code numColumns} locations. Rows and columns are independent — grids need not be square.
     *
     * @param name        name of the environment
     * @param numGrids    number of grids to create in the environment
     * @param numRows     number of rows in each grid
     * @param numColumns  number of columns in each grid
     */
    public Environment createEnvironment(String name, int numGrids, int numRows, int numColumns) throws EnvironmentCreationException {
        log.info("Attempting to create environment: '{}' with {} grids of size {}x{}", name, numGrids, numRows, numColumns);

        // create environment
        int id = getNextEnvironmentId();
        if (id == -1) {
            log.error("Failed to get next environment id");
            throw new EnvironmentCreationException("Failed to get next environment id");
        }
        String creationDate = new java.util.Date().toString();
        String query = "INSERT INTO viron.environment (environment_id, name, creation_date) VALUES (?, ?, ?)";
        boolean success = dbInteractions.update(query, id, name, creationDate);
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

            query = "INSERT INTO viron.grid (grid_id, rows, columns) VALUES (?, ?, ?)";
            success = dbInteractions.update(query, nextGridId, numRows, numColumns);
            if (!success) {
                log.error("Failed to create grid");
                throw new EnvironmentCreationException("Failed to create grid");
            }

            // associate grid with environment
            query = "INSERT INTO viron.grid_environment (grid_id, environment_id) VALUES (?, ?)";
            success = dbInteractions.update(query, nextGridId, id);
            if (!success) {
                log.error("Failed to associate grid with environment");
                throw new EnvironmentCreationException("Failed to associate grid with environment");
            }

            // create locations
            List<Integer> locationIds = new ArrayList<>();
            for (int x = 0; x < numRows; x++) {
                for (int y = 0; y < numColumns; y++) {
                    int locationId = getNextLocationId();
                    if (locationId == -1) {
                        log.error("Failed to get next location id");
                        throw new EnvironmentCreationException("Failed to get next location id");
                    }
                    query = "INSERT INTO viron.location (location_id, x, y) VALUES (?, ?, ?)";
                    success = dbInteractions.update(query, locationId, x, y);
                    if (!success) {
                        log.error("Failed to create location");
                        throw new EnvironmentCreationException("Failed to create location");
                    }

                    // associate location with grid
                    query = "INSERT INTO viron.location_grid (location_id, grid_id) VALUES (?, ?)";
                    success = dbInteractions.update(query, locationId, nextGridId);
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
        Optional<Integer> nextId = dbInteractions.queryOne("SELECT nextval('viron.environment_id_seq')",
                rs -> rs.getInt(1));
        return nextId.orElse(-1);
    }

    private int getNextGridId() {
        Optional<Integer> nextId = dbInteractions.queryOne("SELECT nextval('viron.grid_id_seq')",
                rs -> rs.getInt(1));
        return nextId.orElse(-1);
    }

    private int getNextLocationId() {
        Optional<Integer> nextId = dbInteractions.queryOne("SELECT nextval('viron.location_id_seq')",
                rs -> rs.getInt(1));
        return nextId.orElse(-1);
    }
}
