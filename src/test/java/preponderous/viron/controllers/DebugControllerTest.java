package preponderous.viron.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import preponderous.viron.config.DbConfig;
import preponderous.viron.database.DbInteractions;
import preponderous.viron.dto.EntityDto;
import preponderous.viron.dto.EnvironmentDto;
import preponderous.viron.exceptions.ServiceException;
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

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext
class DebugControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EntityService entityService;

    @MockBean
    private EnvironmentService environmentService;

    @MockBean
    private GridService gridService;

    @MockBean
    private LocationService locationService;

    @MockBean
    private EntityMapper entityMapper;

    @MockBean
    private EnvironmentMapper environmentMapper;

    @MockBean
    private DbInteractions dbInteractions;

    @MockBean
    private DbConfig dbConfig;

    // --- POST /api/v1/debug/create-sample-data ---

    @Test
    void createSampleData_Success() throws Exception {
        Environment environment = new Environment(1, "Sample Environment", "2024-01-01");
        Grid grid = new Grid(1, 10, 10);
        List<Location> locations = buildLocationGrid(10, 10);

        when(environmentService.createEnvironment("Sample Environment", 1, 10)).thenReturn(environment);
        when(gridService.getGridsInEnvironment(1)).thenReturn(List.of(grid));
        when(locationService.getLocationsInGrid(1)).thenReturn(locations);
        when(entityService.createEntity(anyString())).thenAnswer(invocation -> {
            String name = invocation.getArgument(0);
            return new Entity(name.hashCode(), name, "2024-01-01");
        });
        doNothing().when(locationService).addEntityToLocation(anyInt(), anyInt());

        EnvironmentDto dto = new EnvironmentDto(1, "Sample Environment", "2024-01-01");
        when(environmentMapper.toDto(environment)).thenReturn(dto);

        mockMvc.perform(post("/api/v1/debug/create-sample-data"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.environmentId").value(1))
                .andExpect(jsonPath("$.name").value("Sample Environment"))
                .andExpect(jsonPath("$.creationDate").value("2024-01-01"));

        verify(environmentService).createEnvironment("Sample Environment", 1, 10);
        verify(gridService).getGridsInEnvironment(1);
        verify(locationService).getLocationsInGrid(1);
        verify(entityService, times(10)).createEntity(anyString());
        verify(locationService, times(10)).addEntityToLocation(anyInt(), anyInt());
        verify(environmentMapper).toDto(environment);
    }

    @Test
    void createSampleData_NoGrids_Returns400() throws Exception {
        Environment environment = new Environment(5, "NoGridEnv", "2024-05-01");

        when(environmentService.createEnvironment("Sample Environment", 1, 10)).thenReturn(environment);
        when(gridService.getGridsInEnvironment(5)).thenReturn(List.of());

        mockMvc.perform(post("/api/v1/debug/create-sample-data"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("No grids found in environment: 5"));
    }

    // --- POST /api/v1/debug/create-world-and-place-entity/{environmentName} ---

    @Test
    void createWorldAndPlaceEntity_Success() throws Exception {
        Environment environment = new Environment(2, "TestWorld", "2024-02-01");
        Grid grid = new Grid(5, 5, 5);
        List<Location> locations = buildLocationGrid(5, 5);

        when(environmentService.createEnvironment("TestWorld", 1, 5)).thenReturn(environment);
        when(gridService.getGridsInEnvironment(2)).thenReturn(List.of(grid));
        when(locationService.getLocationsInGrid(5)).thenReturn(locations);

        Entity entity = new Entity(10, "Tom", "2024-02-01");
        when(entityService.createEntity(anyString())).thenReturn(entity);
        doNothing().when(locationService).addEntityToLocation(anyInt(), anyInt());

        EntityDto dto = new EntityDto(10, "Tom", "2024-02-01");
        when(entityMapper.toDto(entity)).thenReturn(dto);

        mockMvc.perform(post("/api/v1/debug/create-world-and-place-entity/TestWorld"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.entityId").value(10))
                .andExpect(jsonPath("$.name").value("Tom"))
                .andExpect(jsonPath("$.creationDate").value("2024-02-01"));

        verify(environmentService).createEnvironment("TestWorld", 1, 5);
        verify(gridService).getGridsInEnvironment(2);
        verify(entityService).createEntity(anyString());
        verify(locationService).getLocationsInGrid(5);
        verify(locationService).addEntityToLocation(eq(10), anyInt());
        verify(entityMapper).toDto(entity);
    }

    @Test
    void createWorldAndPlaceEntity_ServiceException_Returns500() throws Exception {
        Environment environment = new Environment(3, "FailWorld", "2024-03-01");
        Grid grid = new Grid(6, 5, 5);

        when(environmentService.createEnvironment("FailWorld", 1, 5)).thenReturn(environment);
        when(gridService.getGridsInEnvironment(3)).thenReturn(List.of(grid));
        when(entityService.createEntity(anyString())).thenThrow(new ServiceException("Entity creation failed"));

        mockMvc.perform(post("/api/v1/debug/create-world-and-place-entity/FailWorld"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Entity creation failed"));
    }

    @Test
    void createWorldAndPlaceEntity_NoGrids_Returns400() throws Exception {
        Environment environment = new Environment(6, "NoGridWorld", "2024-06-01");

        when(environmentService.createEnvironment("NoGridWorld", 1, 5)).thenReturn(environment);
        when(gridService.getGridsInEnvironment(6)).thenReturn(List.of());

        mockMvc.perform(post("/api/v1/debug/create-world-and-place-entity/NoGridWorld"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("No grids found in environment: 6"));
    }

    @Test
    void createWorldAndPlaceEntity_NoValidLocation_Returns400() throws Exception {
        Environment environment = new Environment(4, "EmptyWorld", "2024-04-01");
        Grid grid = new Grid(7, 5, 5);

        when(environmentService.createEnvironment("EmptyWorld", 1, 5)).thenReturn(environment);
        when(gridService.getGridsInEnvironment(4)).thenReturn(List.of(grid));

        Entity entity = new Entity(20, "Jerry", "2024-04-01");
        when(entityService.createEntity(anyString())).thenReturn(entity);
        // Return an empty location list so no valid location can be found
        when(locationService.getLocationsInGrid(7)).thenReturn(List.of());

        mockMvc.perform(post("/api/v1/debug/create-world-and-place-entity/EmptyWorld"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").isString());
    }

    /**
     * Builds a list of locations covering all coordinates in a grid of the given size.
     * Location IDs are assigned sequentially starting from 1.
     */
    private List<Location> buildLocationGrid(int rows, int columns) {
        List<Location> locations = new ArrayList<>();
        int id = 1;
        for (int x = 0; x < rows; x++) {
            for (int y = 0; y < columns; y++) {
                locations.add(new Location(id++, x, y));
            }
        }
        return locations;
    }
}