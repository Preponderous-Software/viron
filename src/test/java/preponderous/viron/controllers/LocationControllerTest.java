package preponderous.viron.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import preponderous.viron.config.DbConfig;
import preponderous.viron.database.DbInteractions;
import preponderous.viron.dto.LocationDto;
import preponderous.viron.mappers.LocationMapper;
import preponderous.viron.models.Location;
import preponderous.viron.repositories.LocationRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
class LocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LocationRepository locationRepository;

    @MockBean
    private LocationMapper locationMapper;

    @MockBean
    private DbInteractions dbInteractions;

    @MockBean
    private DbConfig dbConfig;

    // --- GET /api/v1/locations ---

    @Test
    void getAllLocations_Success() throws Exception {
        List<Location> locations = List.of(
                new Location(1, 10, 20),
                new Location(2, 30, 40)
        );
        List<LocationDto> dtos = List.of(
                new LocationDto(1, 10, 20),
                new LocationDto(2, 30, 40)
        );
        when(locationRepository.findAll()).thenReturn(locations);
        when(locationMapper.toDtoList(locations)).thenReturn(dtos);

        mockMvc.perform(get("/api/v1/locations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].locationId").value(1))
                .andExpect(jsonPath("$[0].x").value(10))
                .andExpect(jsonPath("$[0].y").value(20))
                .andExpect(jsonPath("$[1].locationId").value(2))
                .andExpect(jsonPath("$[1].x").value(30))
                .andExpect(jsonPath("$[1].y").value(40));

        verify(locationRepository).findAll();
        verify(locationMapper).toDtoList(locations);
    }

    @Test
    void getAllLocations_EmptyList() throws Exception {
        when(locationRepository.findAll()).thenReturn(Collections.emptyList());
        when(locationMapper.toDtoList(Collections.emptyList())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/locations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getAllLocations_RepositoryThrowsException() throws Exception {
        when(locationRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/v1/locations"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").isString());
    }

    // --- GET /api/v1/locations/{id} ---

    @Test
    void getLocationById_Success() throws Exception {
        Location location = new Location(1, 10, 20);
        LocationDto dto = new LocationDto(1, 10, 20);
        when(locationRepository.findById(1)).thenReturn(Optional.of(location));
        when(locationMapper.toDto(location)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/locations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locationId").value(1))
                .andExpect(jsonPath("$.x").value(10))
                .andExpect(jsonPath("$.y").value(20));

        verify(locationRepository).findById(1);
        verify(locationMapper).toDto(location);
    }

    @Test
    void getLocationById_NotFound() throws Exception {
        when(locationRepository.findById(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/locations/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Location not found with id: 999"));
    }

    @Test
    void getLocationById_RepositoryThrowsException() throws Exception {
        when(locationRepository.findById(1)).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/v1/locations/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").isString());
    }

    // --- GET /api/v1/locations/environment/{environmentId} ---

    @Test
    void getLocationsInEnvironment_Success() throws Exception {
        List<Location> locations = List.of(new Location(1, 10, 20));
        List<LocationDto> dtos = List.of(new LocationDto(1, 10, 20));
        when(locationRepository.findByEnvironmentId(1)).thenReturn(locations);
        when(locationMapper.toDtoList(locations)).thenReturn(dtos);

        mockMvc.perform(get("/api/v1/locations/environment/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].locationId").value(1))
                .andExpect(jsonPath("$[0].x").value(10))
                .andExpect(jsonPath("$[0].y").value(20));

        verify(locationRepository).findByEnvironmentId(1);
        verify(locationMapper).toDtoList(locations);
    }

    @Test
    void getLocationsInEnvironment_EmptyList() throws Exception {
        when(locationRepository.findByEnvironmentId(1)).thenReturn(Collections.emptyList());
        when(locationMapper.toDtoList(Collections.emptyList())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/locations/environment/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getLocationsInEnvironment_RepositoryThrowsException() throws Exception {
        when(locationRepository.findByEnvironmentId(1)).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/v1/locations/environment/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").isString());
    }

    // --- GET /api/v1/locations/grid/{gridId} ---

    @Test
    void getLocationsInGrid_Success() throws Exception {
        List<Location> locations = List.of(new Location(5, 50, 60));
        List<LocationDto> dtos = List.of(new LocationDto(5, 50, 60));
        when(locationRepository.findByGridId(3)).thenReturn(locations);
        when(locationMapper.toDtoList(locations)).thenReturn(dtos);

        mockMvc.perform(get("/api/v1/locations/grid/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].locationId").value(5))
                .andExpect(jsonPath("$[0].x").value(50))
                .andExpect(jsonPath("$[0].y").value(60));

        verify(locationRepository).findByGridId(3);
        verify(locationMapper).toDtoList(locations);
    }

    @Test
    void getLocationsInGrid_RepositoryThrowsException() throws Exception {
        when(locationRepository.findByGridId(1)).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/v1/locations/grid/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").isString());
    }

    // --- GET /api/v1/locations/grid/{gridId}/unoccupied ---

    @Test
    void getUnoccupiedLocationsInGrid_Success() throws Exception {
        List<Location> locations = List.of(new Location(5, 50, 60));
        List<LocationDto> dtos = List.of(new LocationDto(5, 50, 60));
        when(locationRepository.findUnoccupiedByGridId(3)).thenReturn(locations);
        when(locationMapper.toDtoList(locations)).thenReturn(dtos);

        mockMvc.perform(get("/api/v1/locations/grid/3/unoccupied"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].locationId").value(5))
                .andExpect(jsonPath("$[0].x").value(50))
                .andExpect(jsonPath("$[0].y").value(60));

        verify(locationRepository).findUnoccupiedByGridId(3);
        verify(locationMapper).toDtoList(locations);
    }

    @Test
    void getUnoccupiedLocationsInGrid_EmptyList() throws Exception {
        when(locationRepository.findUnoccupiedByGridId(3)).thenReturn(Collections.emptyList());
        when(locationMapper.toDtoList(Collections.emptyList())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/locations/grid/3/unoccupied"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getUnoccupiedLocationsInGrid_RepositoryThrowsException() throws Exception {
        when(locationRepository.findUnoccupiedByGridId(3)).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/v1/locations/grid/3/unoccupied"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").isString());
    }

    // --- GET /api/v1/locations/{locationId}/neighbors ---

    @Test
    void getNeighbors_Success() throws Exception {
        Location location = new Location(5, 1, 1);
        List<Location> gridLocations = List.of(
                location,
                new Location(6, 1, 2),   // adjacent (dy=1)
                new Location(7, 2, 2),   // adjacent diagonally (dx=1, dy=1)
                new Location(8, 5, 5)    // not adjacent
        );
        List<LocationDto> neighborDtos = List.of(
                new LocationDto(6, 1, 2),
                new LocationDto(7, 2, 2)
        );
        when(locationRepository.findById(5)).thenReturn(Optional.of(location));
        when(locationRepository.getGridIdOfLocation(5)).thenReturn(Optional.of(3));
        when(locationRepository.findByGridId(3)).thenReturn(gridLocations);
        when(locationMapper.toDtoList(List.of(gridLocations.get(1), gridLocations.get(2)))).thenReturn(neighborDtos);

        mockMvc.perform(get("/api/v1/locations/5/neighbors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].locationId").value(6))
                .andExpect(jsonPath("$[1].locationId").value(7));
    }

    @Test
    void getNeighbors_LocationNotFound() throws Exception {
        when(locationRepository.findById(5)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/locations/5/neighbors"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Location not found with id: 5"));
    }

    @Test
    void getNeighbors_NotInAnyGrid() throws Exception {
        when(locationRepository.findById(5)).thenReturn(Optional.of(new Location(5, 1, 1)));
        when(locationRepository.getGridIdOfLocation(5)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/locations/5/neighbors"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Location 5 is not in any grid"));
    }

    // --- GET /api/v1/locations/entity/{entityId} ---

    @Test
    void getLocationOfEntity_Success() throws Exception {
        Location location = new Location(1, 10, 20);
        LocationDto dto = new LocationDto(1, 10, 20);
        when(locationRepository.findByEntityId(1)).thenReturn(Optional.of(location));
        when(locationMapper.toDto(location)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/locations/entity/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locationId").value(1))
                .andExpect(jsonPath("$.x").value(10))
                .andExpect(jsonPath("$.y").value(20));

        verify(locationRepository).findByEntityId(1);
        verify(locationMapper).toDto(location);
    }

    @Test
    void getLocationOfEntity_NotFound() throws Exception {
        when(locationRepository.findByEntityId(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/locations/entity/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Location not found for entity: 999"));
    }

    @Test
    void getLocationOfEntity_RepositoryThrowsException() throws Exception {
        when(locationRepository.findByEntityId(1)).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/v1/locations/entity/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").isString());
    }

    // --- PUT /api/v1/locations/{locationId}/entity/{entityId} ---

    @Test
    void addEntityToLocation_Success() throws Exception {
        when(locationRepository.findById(2)).thenReturn(Optional.of(new Location(2, 10, 20)));
        when(locationRepository.addEntityToLocation(1, 2)).thenReturn(true);

        mockMvc.perform(put("/api/v1/locations/2/entity/1"))
                .andExpect(status().isOk());

        verify(locationRepository).addEntityToLocation(1, 2);
    }

    @Test
    void addEntityToLocation_NotFound() throws Exception {
        when(locationRepository.findById(2)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/v1/locations/2/entity/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Location not found with id: 2"));
    }

    @Test
    void addEntityToLocation_RepositoryThrowsException() throws Exception {
        when(locationRepository.findById(2)).thenReturn(Optional.of(new Location(2, 10, 20)));
        when(locationRepository.addEntityToLocation(1, 2)).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(put("/api/v1/locations/2/entity/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").isString());
    }

    // --- DELETE /api/v1/locations/{locationId}/entity/{entityId} ---

    @Test
    void removeEntityFromLocation_Success() throws Exception {
        when(locationRepository.findById(2)).thenReturn(Optional.of(new Location(2, 10, 20)));
        when(locationRepository.removeEntityFromLocation(1, 2)).thenReturn(true);

        mockMvc.perform(delete("/api/v1/locations/2/entity/1"))
                .andExpect(status().isNoContent());

        verify(locationRepository).removeEntityFromLocation(1, 2);
    }

    @Test
    void removeEntityFromLocation_NotFound() throws Exception {
        when(locationRepository.findById(2)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/v1/locations/2/entity/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Location not found with id: 2"));
    }

    @Test
    void removeEntityFromLocation_RepositoryThrowsException() throws Exception {
        when(locationRepository.findById(2)).thenReturn(Optional.of(new Location(2, 10, 20)));
        when(locationRepository.removeEntityFromLocation(1, 2)).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(delete("/api/v1/locations/2/entity/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").isString());
    }

    // --- DELETE /api/v1/locations/entity/{entityId} ---

    @Test
    void removeEntityFromCurrentLocation_Success() throws Exception {
        when(locationRepository.findByEntityId(1)).thenReturn(Optional.of(new Location(5, 10, 20)));
        when(locationRepository.removeEntityFromCurrentLocation(1)).thenReturn(true);

        mockMvc.perform(delete("/api/v1/locations/entity/1"))
                .andExpect(status().isNoContent());

        verify(locationRepository).removeEntityFromCurrentLocation(1);
    }

    @Test
    void removeEntityFromCurrentLocation_NotFound() throws Exception {
        when(locationRepository.findByEntityId(999)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/v1/locations/entity/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Location not found for entity: 999"));
    }

    @Test
    void removeEntityFromCurrentLocation_RepositoryThrowsException() throws Exception {
        when(locationRepository.findByEntityId(1)).thenReturn(Optional.of(new Location(5, 10, 20)));
        when(locationRepository.removeEntityFromCurrentLocation(1)).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(delete("/api/v1/locations/entity/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").isString());
    }

    // --- GET /api/v1/locations/{locationId}/entities (occupancy) ---

    @Test
    void getEntityIdsAtLocation_Success() throws Exception {
        when(locationRepository.findById(5)).thenReturn(Optional.of(new Location(5, 1, 1)));
        when(locationRepository.getEntityIdsAtLocation(5)).thenReturn(List.of(11, 22));

        mockMvc.perform(get("/api/v1/locations/5/entities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0]").value(11))
                .andExpect(jsonPath("$[1]").value(22));
    }

    @Test
    void getEntityIdsAtLocation_LocationNotFound() throws Exception {
        when(locationRepository.findById(5)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/locations/5/entities"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // --- GET /api/v1/locations/{locationId}/occupied ---

    @Test
    void isLocationOccupied_TrueWhenEntitiesPresent() throws Exception {
        when(locationRepository.findById(5)).thenReturn(Optional.of(new Location(5, 1, 1)));
        when(locationRepository.getEntityIdsAtLocation(5)).thenReturn(List.of(11));

        mockMvc.perform(get("/api/v1/locations/5/occupied"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void isLocationOccupied_FalseWhenEmpty() throws Exception {
        when(locationRepository.findById(5)).thenReturn(Optional.of(new Location(5, 1, 1)));
        when(locationRepository.getEntityIdsAtLocation(5)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/locations/5/occupied"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    // --- PUT /api/v1/locations/{locationId}/entity/{entityId}/move ---

    @Test
    void moveEntityToLocation_Success() throws Exception {
        when(locationRepository.findByEntityId(1)).thenReturn(Optional.of(new Location(5, 0, 0)));
        when(locationRepository.findById(9)).thenReturn(Optional.of(new Location(9, 1, 0)));
        when(locationRepository.getGridIdOfLocation(5)).thenReturn(Optional.of(3));
        when(locationRepository.getGridIdOfLocation(9)).thenReturn(Optional.of(3));
        when(locationRepository.getEntityIdsAtLocation(9)).thenReturn(Collections.emptyList());
        when(locationRepository.moveEntityToLocation(1, 9)).thenReturn(true);

        mockMvc.perform(put("/api/v1/locations/9/entity/1/move"))
                .andExpect(status().isNoContent());

        verify(locationRepository).moveEntityToLocation(1, 9);
    }

    @Test
    void moveEntityToLocation_EntityNotPlaced() throws Exception {
        when(locationRepository.findByEntityId(1)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/v1/locations/9/entity/1/move"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Entity 1 is not placed at any location"));
    }

    @Test
    void moveEntityToLocation_TargetNotFound() throws Exception {
        when(locationRepository.findByEntityId(1)).thenReturn(Optional.of(new Location(5, 0, 0)));
        when(locationRepository.findById(9)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/v1/locations/9/entity/1/move"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Location not found with id: 9"));
    }

    @Test
    void moveEntityToLocation_DifferentGridIsBadRequest() throws Exception {
        when(locationRepository.findByEntityId(1)).thenReturn(Optional.of(new Location(5, 0, 0)));
        when(locationRepository.findById(9)).thenReturn(Optional.of(new Location(9, 1, 0)));
        when(locationRepository.getGridIdOfLocation(5)).thenReturn(Optional.of(3));
        when(locationRepository.getGridIdOfLocation(9)).thenReturn(Optional.of(4));

        mockMvc.perform(put("/api/v1/locations/9/entity/1/move"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void moveEntityToLocation_NotAdjacentIsBadRequest() throws Exception {
        when(locationRepository.findByEntityId(1)).thenReturn(Optional.of(new Location(5, 0, 0)));
        when(locationRepository.findById(9)).thenReturn(Optional.of(new Location(9, 5, 5)));
        when(locationRepository.getGridIdOfLocation(5)).thenReturn(Optional.of(3));
        when(locationRepository.getGridIdOfLocation(9)).thenReturn(Optional.of(3));

        mockMvc.perform(put("/api/v1/locations/9/entity/1/move"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Target location 9 is not adjacent to entity 1's current location"));

        verify(locationRepository, never()).moveEntityToLocation(anyInt(), anyInt());
    }

    @Test
    void moveEntityToLocation_OccupiedTargetIsConflict() throws Exception {
        when(locationRepository.findByEntityId(1)).thenReturn(Optional.of(new Location(5, 0, 0)));
        when(locationRepository.findById(9)).thenReturn(Optional.of(new Location(9, 1, 0)));
        when(locationRepository.getGridIdOfLocation(5)).thenReturn(Optional.of(3));
        when(locationRepository.getGridIdOfLocation(9)).thenReturn(Optional.of(3));
        when(locationRepository.getEntityIdsAtLocation(9)).thenReturn(List.of(99));

        mockMvc.perform(put("/api/v1/locations/9/entity/1/move"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        verify(locationRepository, never()).moveEntityToLocation(anyInt(), anyInt());
    }
}
