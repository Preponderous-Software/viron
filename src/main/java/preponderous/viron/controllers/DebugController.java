package preponderous.viron.controllers;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import preponderous.viron.dto.EntityDto;
import preponderous.viron.dto.EnvironmentDto;
import preponderous.viron.exceptions.InvalidRequestException;
import preponderous.viron.mappers.EntityMapper;
import preponderous.viron.mappers.EnvironmentMapper;
import preponderous.viron.models.Entity;
import preponderous.viron.models.Environment;
import preponderous.viron.models.Grid;
import preponderous.viron.models.Location;
import preponderous.viron.services.EntityService;
import preponderous.viron.services.EnvironmentService;
import preponderous.viron.services.GridService;
import preponderous.viron.services.LocationService;

@RestController
@RequestMapping("/api/v1/debug")
@Slf4j
@Validated
public class DebugController {
    private final EntityService entityService;
    private final EnvironmentService environmentService;
    private final GridService gridService;
    private final LocationService locationService;
    private final EntityMapper entityMapper;
    private final EnvironmentMapper environmentMapper;

    private final List<String> entityNamePool = List.of("Tom", "Jerry", "Spike", "Tyke", "Nibbles", "Butch", "Tuffy", "Lightning", "Mammy", "Quacker", "Toodles", "Droopy", "Screwy", "Meathead", "George", "Dripple", "McWolf");

    public DebugController(EntityService entityService, EnvironmentService environmentService,
                           GridService gridService, LocationService locationService,
                           EntityMapper entityMapper, EnvironmentMapper environmentMapper) {
        this.entityService = entityService;
        this.environmentService = environmentService;
        this.gridService = gridService;
        this.locationService = locationService;
        this.entityMapper = entityMapper;
        this.environmentMapper = environmentMapper;
    }

    /**
     * Creates a sample environment with a single 10x10 grid and places ten entities in random, valid locations within the grid.
     * It ensures the entities are properly created and assigned to valid locations in the grid.
     */
    @PostMapping("/create-sample-data")
    @ResponseStatus(HttpStatus.CREATED)
    public EnvironmentDto createSampleData() {
        // create an environment with one 10x10 grid
        Environment environment = environmentService.createEnvironment("Sample Environment", 1, 10);
        List<Grid> grids = gridService.getGridsInEnvironment(environment.getEnvironmentId());
        if (grids.isEmpty()) {
            throw new InvalidRequestException("No grids found in environment: " + environment.getEnvironmentId());
        }
        Grid grid = grids.getFirst();
        List<Location> locations = locationService.getLocationsInGrid(grid.getGridId());

        // create ten entities and place them in the environment
        for (int i = 0; i < 10; i++) {
            Entity entity = entityService.createEntity(entityNamePool.get(i));

            // get random location in the grid
            int x = (int) (Math.random() * grid.getRows());
            int y = (int) (Math.random() * grid.getColumns());
            Location location = null;
            for (Location value : locations) {
                if (value.getX() == x && value.getY() == y) {
                    location = value;
                    break;
                }
            }
            if (location == null) {
                throw new InvalidRequestException("No valid location found at coordinates (" + x + ", " + y + ")");
            }
            locationService.addEntityToLocation(entity.getEntityId(), location.getLocationId());
        }

        return environmentMapper.toDto(environment);
    }

    /**
     * Creates an environment with a single grid of fixed size, generates a random entity,
     * and places the entity at a random valid location within the grid.
     *
     * @param environmentName the name of the environment to be created
     */
    @PostMapping("/create-world-and-place-entity/{environmentName}")
    @ResponseStatus(HttpStatus.CREATED)
    public EntityDto createWorldAndPlaceEntity(@PathVariable @NotBlank String environmentName) {
        // create an environment
        int numGrids = 1;
        int gridSize = 5;
        Environment environment = environmentService.createEnvironment(environmentName, numGrids, gridSize);
        log.info("Environment created: {} with ID {}", environment.getName(), environment.getEnvironmentId());

        // get grid info
        List<Grid> grids = gridService.getGridsInEnvironment(environment.getEnvironmentId());
        if (grids.isEmpty()) {
            throw new InvalidRequestException("No grids found in environment: " + environment.getEnvironmentId());
        }
        Grid grid = grids.getFirst();
        log.info("Grid created: {} with size {}x{}", grid.getGridId(), grid.getRows(), grid.getColumns());

        // create an entity
        String entityName = entityNamePool.get((int) (Math.random() * entityNamePool.size()));
        Entity entity = entityService.createEntity(entityName);
        log.info("Entity created: {}", entity.getName());

        // place entity in grid
        int entityRow = (int) (Math.random() * grid.getRows());
        int entityColumn = (int) (Math.random() * grid.getColumns());
        List<Location> locations = locationService.getLocationsInGrid(grid.getGridId());
        Location location = null;
        for (Location l : locations) {
            if (l.getX() == entityRow && l.getY() == entityColumn) {
                location = l;
                break;
            }
        }
        if (location == null) {
            throw new InvalidRequestException("No valid location found for entity at row " + entityRow + " and column " + entityColumn);
        }
        locationService.addEntityToLocation(entity.getEntityId(), location.getLocationId());
        log.info("Entity {} placed at location ({}, {})", entity.getName(), entityRow, entityColumn);
        return entityMapper.toDto(entity);
    }
}