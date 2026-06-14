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
}
