package preponderous.viron.controllers;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.web.servlet.MockMvc;
import preponderous.viron.database.DbInteractions;
import preponderous.viron.dto.LocationDto;
import preponderous.viron.mappers.LocationMapper;
import preponderous.viron.models.Entity;
import preponderous.viron.models.Location;
import preponderous.viron.repositories.EntityRepository;
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
    private EntityRepository entityRepository;

    @MockBean
    private LocationMapper locationMapper;

    @MockBean
    private DbInteractions dbInteractions;

    // DbConfig is left real, unlike the collaborators above: the transaction boundary on
    // moveEntityToLocation opens a connection from the pool DbConfig configures, and a mock would
    // supply it a null JDBC URL.

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
        when(entityRepository.findById(1)).thenReturn(Optional.of(new Entity(1, "Entity1", "2024-01-01")));
        when(locationRepository.findByEntityId(1)).thenReturn(Optional.empty());
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

        verify(locationRepository, never()).addEntityToLocation(anyInt(), anyInt());
    }

    @Test
    void addEntityToLocation_EntityNotFound() throws Exception {
        when(locationRepository.findById(2)).thenReturn(Optional.of(new Location(2, 10, 20)));
        when(entityRepository.findById(1)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/v1/locations/2/entity/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Entity not found with id: 1"));

        verify(locationRepository, never()).addEntityToLocation(anyInt(), anyInt());
    }

    @Test
    void addEntityToLocation_AlreadyAtTargetLocation_IsNoOp() throws Exception {
        when(locationRepository.findById(2)).thenReturn(Optional.of(new Location(2, 10, 20)));
        when(entityRepository.findById(1)).thenReturn(Optional.of(new Entity(1, "Entity1", "2024-01-01")));
        when(locationRepository.findByEntityId(1)).thenReturn(Optional.of(new Location(2, 10, 20)));

        mockMvc.perform(put("/api/v1/locations/2/entity/1"))
                .andExpect(status().isOk());

        verify(locationRepository, never()).addEntityToLocation(anyInt(), anyInt());
    }

    @Test
    void addEntityToLocation_AlreadyAtAnotherLocation_Conflict() throws Exception {
        when(locationRepository.findById(2)).thenReturn(Optional.of(new Location(2, 10, 20)));
        when(entityRepository.findById(1)).thenReturn(Optional.of(new Entity(1, "Entity1", "2024-01-01")));
        when(locationRepository.findByEntityId(1)).thenReturn(Optional.of(new Location(7, 30, 40)));

        mockMvc.perform(put("/api/v1/locations/2/entity/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Entity 1 is already placed at location 7"));

        verify(locationRepository, never()).addEntityToLocation(anyInt(), anyInt());
    }

    @Test
    void addEntityToLocation_UpdateFails() throws Exception {
        when(locationRepository.findById(2)).thenReturn(Optional.of(new Location(2, 10, 20)));
        when(entityRepository.findById(1)).thenReturn(Optional.of(new Entity(1, "Entity1", "2024-01-01")));
        when(locationRepository.findByEntityId(1)).thenReturn(Optional.empty());
        when(locationRepository.addEntityToLocation(1, 2)).thenReturn(false);

        mockMvc.perform(put("/api/v1/locations/2/entity/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Failed to add entity 1 to location 2"));
    }

    // #200: the guard read no placement, a concurrent request won the race, and the insert was
    // rejected by the primary key on entity_id. The loser is told where the entity actually is.
    @Test
    void addEntityToLocation_LostRaceWithConcurrentPlacementElsewhere_Conflict() throws Exception {
        when(locationRepository.findById(2)).thenReturn(Optional.of(new Location(2, 10, 20)));
        when(entityRepository.findById(1)).thenReturn(Optional.of(new Entity(1, "Entity1", "2024-01-01")));
        when(locationRepository.findByEntityId(1))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(new Location(7, 30, 40)));
        when(locationRepository.addEntityToLocation(1, 2))
                .thenThrow(new DuplicateKeyException("entity 1 is already placed"));

        mockMvc.perform(put("/api/v1/locations/2/entity/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Entity 1 is already placed at location 7"));
    }

    // Both requests wanted the same location, so the loser got the outcome it asked for: the
    // endpoint stays idempotent whether the two arrive concurrently or in sequence.
    @Test
    void addEntityToLocation_LostRaceWithConcurrentPlacementAtSameLocation_IsNoOp() throws Exception {
        when(locationRepository.findById(2)).thenReturn(Optional.of(new Location(2, 10, 20)));
        when(entityRepository.findById(1)).thenReturn(Optional.of(new Entity(1, "Entity1", "2024-01-01")));
        when(locationRepository.findByEntityId(1))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(new Location(2, 10, 20)));
        when(locationRepository.addEntityToLocation(1, 2))
                .thenThrow(new DuplicateKeyException("entity 1 is already placed"));

        mockMvc.perform(put("/api/v1/locations/2/entity/1"))
                .andExpect(status().isOk());
    }

    // The winning placement was removed again before it could be read back: still a conflict
    // rather than a fault, but with no location left to name.
    @Test
    void addEntityToLocation_LostRaceAndWinningPlacementIsGone_Conflict() throws Exception {
        when(locationRepository.findById(2)).thenReturn(Optional.of(new Location(2, 10, 20)));
        when(entityRepository.findById(1)).thenReturn(Optional.of(new Entity(1, "Entity1", "2024-01-01")));
        when(locationRepository.findByEntityId(1)).thenReturn(Optional.empty());
        when(locationRepository.addEntityToLocation(1, 2))
                .thenThrow(new DuplicateKeyException("entity 1 is already placed"));

        mockMvc.perform(put("/api/v1/locations/2/entity/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(
                        "Entity 1 was placed by a concurrent request and could not be added to location 2"));
    }

    @Test
    void addEntityToLocation_RepositoryThrowsException() throws Exception {
        when(locationRepository.findById(2)).thenReturn(Optional.of(new Location(2, 10, 20)));
        when(entityRepository.findById(1)).thenReturn(Optional.of(new Entity(1, "Entity1", "2024-01-01")));
        when(locationRepository.findByEntityId(1)).thenReturn(Optional.empty());
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
        when(locationRepository.lockPlacementOfEntity(1)).thenReturn(true);
        when(locationRepository.findByEntityId(1)).thenReturn(Optional.of(new Location(2, 10, 20)));
        when(locationRepository.removeEntityFromLocation(1, 2)).thenReturn(true);

        mockMvc.perform(delete("/api/v1/locations/2/entity/1"))
                .andExpect(status().isNoContent());

        verify(locationRepository).removeEntityFromLocation(1, 2);
    }

    /** An entity that is placed somewhere else is not at the location the request names (#210). */
    @Test
    void removeEntityFromLocation_EntityPlacedElsewhereIsNotFound() throws Exception {
        when(locationRepository.findById(2)).thenReturn(Optional.of(new Location(2, 10, 20)));
        when(locationRepository.lockPlacementOfEntity(1)).thenReturn(true);
        when(locationRepository.findByEntityId(1)).thenReturn(Optional.of(new Location(7, 30, 40)));

        mockMvc.perform(delete("/api/v1/locations/2/entity/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Entity 1 is not at location 2"));

        verify(locationRepository, never()).removeEntityFromLocation(anyInt(), anyInt());
    }

    /**
     * Neither is an entity that is not placed anywhere, or does not exist at all (#210). There is
     * no placement row to lock, so the request is answered without reading any further.
     */
    @Test
    void removeEntityFromLocation_UnplacedEntityIsNotFound() throws Exception {
        when(locationRepository.findById(2)).thenReturn(Optional.of(new Location(2, 10, 20)));
        when(locationRepository.lockPlacementOfEntity(1)).thenReturn(false);

        mockMvc.perform(delete("/api/v1/locations/2/entity/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Entity 1 is not at location 2"));

        verify(locationRepository, never()).findByEntityId(anyInt());
        verify(locationRepository, never()).removeEntityFromLocation(anyInt(), anyInt());
    }

    /**
     * The placement was locked but names a different location than the one read a moment earlier,
     * which is the same "not at that location" answer arrived at after the lock rather than before.
     */
    @Test
    void removeEntityFromLocation_PlacementMovedBeforeTheLockIsNotFound() throws Exception {
        when(locationRepository.findById(2)).thenReturn(Optional.of(new Location(2, 10, 20)));
        when(locationRepository.lockPlacementOfEntity(1)).thenReturn(true);
        when(locationRepository.findByEntityId(1)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/v1/locations/2/entity/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Entity 1 is not at location 2"));

        verify(locationRepository, never()).removeEntityFromLocation(anyInt(), anyInt());
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
        when(locationRepository.lockPlacementOfEntity(1)).thenReturn(true);
        when(locationRepository.findByEntityId(1)).thenReturn(Optional.of(new Location(2, 10, 20)));
        when(locationRepository.removeEntityFromLocation(1, 2)).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(delete("/api/v1/locations/2/entity/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").isString());
    }

    // --- DELETE /api/v1/locations/entity/{entityId} ---

    @Test
    void removeEntityFromCurrentLocation_Success() throws Exception {
        when(locationRepository.lockPlacementOfEntity(1)).thenReturn(true);
        when(locationRepository.removeEntityFromCurrentLocation(1)).thenReturn(true);

        mockMvc.perform(delete("/api/v1/locations/entity/1"))
                .andExpect(status().isNoContent());

        verify(locationRepository).removeEntityFromCurrentLocation(1);
    }

    /**
     * The placement is locked before it is deleted, so that a second removal of the same placement
     * waits and is then told the entity is not placed, rather than passing an unguarded check and
     * writing nothing.
     */
    @Test
    void removeEntityFromCurrentLocation_LocksThePlacementBeforeDeletingIt() throws Exception {
        when(locationRepository.lockPlacementOfEntity(1)).thenReturn(true);
        when(locationRepository.removeEntityFromCurrentLocation(1)).thenReturn(true);

        mockMvc.perform(delete("/api/v1/locations/entity/1"))
                .andExpect(status().isNoContent());

        InOrder inOrder = inOrder(locationRepository);
        inOrder.verify(locationRepository).lockPlacementOfEntity(1);
        inOrder.verify(locationRepository).removeEntityFromCurrentLocation(1);
    }

    /** There is no placement row to lock, which is how an unplaced entity is recognised. */
    @Test
    void removeEntityFromCurrentLocation_NotFound() throws Exception {
        when(locationRepository.lockPlacementOfEntity(999)).thenReturn(false);

        mockMvc.perform(delete("/api/v1/locations/entity/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Location not found for entity: 999"));

        verify(locationRepository, never()).removeEntityFromCurrentLocation(anyInt());
    }

    @Test
    void removeEntityFromCurrentLocation_RepositoryThrowsException() throws Exception {
        when(locationRepository.lockPlacementOfEntity(1)).thenReturn(true);
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
        when(locationRepository.lockPlacementOfEntity(1)).thenReturn(true);
        when(locationRepository.findByEntityId(1)).thenReturn(Optional.of(new Location(5, 0, 0)));
        when(locationRepository.findById(9)).thenReturn(Optional.of(new Location(9, 1, 0)));
        when(locationRepository.getGridIdOfLocation(5)).thenReturn(Optional.of(3));
        when(locationRepository.getGridIdOfLocation(9)).thenReturn(Optional.of(3));
        when(locationRepository.lockLocation(9)).thenReturn(true);
        when(locationRepository.getEntityIdsAtLocation(9)).thenReturn(Collections.emptyList());
        when(locationRepository.moveEntityToLocation(1, 9)).thenReturn(true);

        mockMvc.perform(put("/api/v1/locations/9/entity/1/move"))
                .andExpect(status().isNoContent());

        verify(locationRepository).moveEntityToLocation(1, 9);
    }

    /**
     * The target's occupancy is only read once the target is locked, so that a concurrent move
     * cannot claim it in between (#203). The entity's placement is locked before the target,
     * matching the order the environment cascade delete takes the same two locks in — the reverse
     * order would let a move and a delete wait on each other in a cycle.
     */
    @Test
    void moveEntityToLocation_LocksTheTargetBeforeReadingItsOccupancy() throws Exception {
        when(locationRepository.lockPlacementOfEntity(1)).thenReturn(true);
        when(locationRepository.findByEntityId(1)).thenReturn(Optional.of(new Location(5, 0, 0)));
        when(locationRepository.findById(9)).thenReturn(Optional.of(new Location(9, 1, 0)));
        when(locationRepository.getGridIdOfLocation(5)).thenReturn(Optional.of(3));
        when(locationRepository.getGridIdOfLocation(9)).thenReturn(Optional.of(3));
        when(locationRepository.lockLocation(9)).thenReturn(true);
        when(locationRepository.getEntityIdsAtLocation(9)).thenReturn(Collections.emptyList());
        when(locationRepository.moveEntityToLocation(1, 9)).thenReturn(true);

        mockMvc.perform(put("/api/v1/locations/9/entity/1/move"))
                .andExpect(status().isNoContent());

        InOrder inOrder = inOrder(locationRepository);
        inOrder.verify(locationRepository).lockPlacementOfEntity(1);
        inOrder.verify(locationRepository).lockLocation(9);
        inOrder.verify(locationRepository).getEntityIdsAtLocation(9);
        inOrder.verify(locationRepository).moveEntityToLocation(1, 9);
    }

    /**
     * A target that no longer exists by the time it is locked was deleted between the two reads;
     * that is the same missing target the earlier check answers for, so it gets the same answer.
     */
    @Test
    void moveEntityToLocation_TargetDeletedBeforeLockIsNotFound() throws Exception {
        when(locationRepository.lockPlacementOfEntity(1)).thenReturn(true);
        when(locationRepository.findByEntityId(1)).thenReturn(Optional.of(new Location(5, 0, 0)));
        when(locationRepository.findById(9)).thenReturn(Optional.of(new Location(9, 1, 0)));
        when(locationRepository.getGridIdOfLocation(5)).thenReturn(Optional.of(3));
        when(locationRepository.getGridIdOfLocation(9)).thenReturn(Optional.of(3));
        when(locationRepository.lockLocation(9)).thenReturn(false);

        mockMvc.perform(put("/api/v1/locations/9/entity/1/move"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Location not found with id: 9"));

        verify(locationRepository, never()).moveEntityToLocation(anyInt(), anyInt());
    }

    /**
     * The target exists and is well-formed, but its lock was held by another request for longer
     * than the bound {@code DataSourceConfig} sets (#212). That is a transient condition, told to
     * the client as one: 503 with a {@code Retry-After}, not the 500 it used to fall through to,
     * and not the move's 409, which would claim the target is occupied.
     */
    @Test
    void moveEntityToLocation_LockWaitTimedOutIsServiceUnavailable() throws Exception {
        when(locationRepository.lockPlacementOfEntity(1)).thenReturn(true);
        when(locationRepository.findByEntityId(1)).thenReturn(Optional.of(new Location(5, 0, 0)));
        when(locationRepository.findById(9)).thenReturn(Optional.of(new Location(9, 1, 0)));
        when(locationRepository.getGridIdOfLocation(5)).thenReturn(Optional.of(3));
        when(locationRepository.getGridIdOfLocation(9)).thenReturn(Optional.of(3));
        when(locationRepository.lockLocation(9))
                .thenThrow(new CannotAcquireLockException("Could not take lock: lock timeout"));

        mockMvc.perform(put("/api/v1/locations/9/entity/1/move"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(header().string("Retry-After", "1"))
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.message")
                        .value("The requested resource is in use by another request; retry shortly"));

        verify(locationRepository, never()).moveEntityToLocation(anyInt(), anyInt());
    }

    /** There is no placement row to lock, which is how an unplaced entity is recognised. */
    @Test
    void moveEntityToLocation_EntityNotPlaced() throws Exception {
        when(locationRepository.lockPlacementOfEntity(1)).thenReturn(false);

        mockMvc.perform(put("/api/v1/locations/9/entity/1/move"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Entity 1 is not placed at any location"));

        verify(locationRepository, never()).findByEntityId(anyInt());
    }

    /**
     * The placement was locked but could not be read back. The foreign keys make that unreachable
     * in practice, so the guard exists only so that an unreadable placement is reported as the
     * absent placement it looks like rather than as a null dereference.
     */
    @Test
    void moveEntityToLocation_LockedPlacementThatCannotBeReadIsNotFound() throws Exception {
        when(locationRepository.lockPlacementOfEntity(1)).thenReturn(true);
        when(locationRepository.findByEntityId(1)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/v1/locations/9/entity/1/move"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Entity 1 is not placed at any location"));
    }

    @Test
    void moveEntityToLocation_TargetNotFound() throws Exception {
        when(locationRepository.lockPlacementOfEntity(1)).thenReturn(true);
        when(locationRepository.findByEntityId(1)).thenReturn(Optional.of(new Location(5, 0, 0)));
        when(locationRepository.findById(9)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/v1/locations/9/entity/1/move"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Location not found with id: 9"));
    }

    @Test
    void moveEntityToLocation_DifferentGridIsBadRequest() throws Exception {
        when(locationRepository.lockPlacementOfEntity(1)).thenReturn(true);
        when(locationRepository.findByEntityId(1)).thenReturn(Optional.of(new Location(5, 0, 0)));
        when(locationRepository.findById(9)).thenReturn(Optional.of(new Location(9, 1, 0)));
        when(locationRepository.getGridIdOfLocation(5)).thenReturn(Optional.of(3));
        when(locationRepository.getGridIdOfLocation(9)).thenReturn(Optional.of(4));

        mockMvc.perform(put("/api/v1/locations/9/entity/1/move"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void moveEntityToLocation_NotAdjacentIsBadRequest() throws Exception {
        when(locationRepository.lockPlacementOfEntity(1)).thenReturn(true);
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
        when(locationRepository.lockPlacementOfEntity(1)).thenReturn(true);
        when(locationRepository.findByEntityId(1)).thenReturn(Optional.of(new Location(5, 0, 0)));
        when(locationRepository.findById(9)).thenReturn(Optional.of(new Location(9, 1, 0)));
        when(locationRepository.getGridIdOfLocation(5)).thenReturn(Optional.of(3));
        when(locationRepository.getGridIdOfLocation(9)).thenReturn(Optional.of(3));
        when(locationRepository.lockLocation(9)).thenReturn(true);
        when(locationRepository.getEntityIdsAtLocation(9)).thenReturn(List.of(99));

        mockMvc.perform(put("/api/v1/locations/9/entity/1/move"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        verify(locationRepository, never()).moveEntityToLocation(anyInt(), anyInt());
    }
}
