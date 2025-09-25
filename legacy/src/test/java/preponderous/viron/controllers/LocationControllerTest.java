package preponderous.viron.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import preponderous.viron.models.Location;
import preponderous.viron.repositories.LocationRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class LocationControllerTest {
    private LocationRepository locationRepository;
    private LocationController locationController;

    @BeforeEach
    void setUp() {
        locationRepository = Mockito.mock(LocationRepository.class);
        locationController = new LocationController(locationRepository);
    }

    @Test
    void getAllLocations_Success() {
        // setup
        List<Location> locations = Arrays.asList(
                new Location(1, 10, 20)
        );
        when(locationRepository.findAll()).thenReturn(locations);

        // execute
        ResponseEntity<List<Location>> response = locationController.getAllLocations();

        // verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(locations, response.getBody());
        verify(locationRepository).findAll();
    }

    @Test
    void getAllLocations_Exception() {
        // setup
        when(locationRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // execute
        ResponseEntity<List<Location>> response = locationController.getAllLocations();

        // verify
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
        verify(locationRepository).findAll();
    }

    @Test
    void getLocationById_Success() {
        // setup
        Location location = new Location(1, 10, 20);
        when(locationRepository.findById(1)).thenReturn(Optional.of(location));

        // execute
        ResponseEntity<Location> response = locationController.getLocationById(1);

        // verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(location, response.getBody());
        verify(locationRepository).findById(1);
    }

    @Test
    void getLocationById_NotFound() {
        // setup
        when(locationRepository.findById(1)).thenReturn(Optional.empty());

        // execute
        ResponseEntity<Location> response = locationController.getLocationById(1);

        // verify
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(locationRepository).findById(1);
    }

    @Test
    void getLocationsInEnvironment_Success() {
        // setup
        List<Location> locations = Arrays.asList(new Location(1, 10, 20));
        when(locationRepository.findByEnvironmentId(1)).thenReturn(locations);

        // execute
        ResponseEntity<List<Location>> response = locationController.getLocationsInEnvironment(1);

        // verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(locations, response.getBody());
        verify(locationRepository).findByEnvironmentId(1);
    }

    @Test
    void getLocationsInEnvironment_Exception() {
        // setup
        when(locationRepository.findByEnvironmentId(1)).thenThrow(new RuntimeException());

        // execute
        ResponseEntity<List<Location>> response = locationController.getLocationsInEnvironment(1);

        // verify
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
        verify(locationRepository).findByEnvironmentId(1);
    }

    @Test
    void getLocationsInGrid_Success() {
        // setup
        List<Location> locations = Arrays.asList(new Location(1, 10, 20));
        when(locationRepository.findByGridId(1)).thenReturn(locations);

        // execute
        ResponseEntity<List<Location>> response = locationController.getLocationsInGrid(1);

        // verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(locations, response.getBody());
        verify(locationRepository).findByGridId(1);
    }

    @Test
    void getLocationsInGrid_Exception() {
        // setup
        when(locationRepository.findByGridId(1)).thenThrow(new RuntimeException());

        // execute
        ResponseEntity<List<Location>> response = locationController.getLocationsInGrid(1);

        // verify
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
        verify(locationRepository).findByGridId(1);
    }

    @Test
    void getLocationOfEntity_Success() {
        // setup
        Location location = new Location(1, 10, 20);
        when(locationRepository.findByEntityId(1)).thenReturn(Optional.of(location));

        // execute
        ResponseEntity<Location> response = locationController.getLocationOfEntity(1);

        // verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(location, response.getBody());
        verify(locationRepository).findByEntityId(1);
    }

    @Test
    void getLocationOfEntity_NotFound() {
        // setup
        when(locationRepository.findByEntityId(1)).thenReturn(Optional.empty());

        // execute
        ResponseEntity<Location> response = locationController.getLocationOfEntity(1);

        // verify
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(locationRepository).findByEntityId(1);
    }

    @Test
    void addEntityToLocation_Success() {
        // setup
        when(locationRepository.addEntityToLocation(1, 1)).thenReturn(true);

        // execute
        ResponseEntity<Void> response = locationController.addEntityToLocation(1, 1);

        // verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(locationRepository).addEntityToLocation(1, 1);
    }

    @Test
    void addEntityToLocation_NotFound() {
        // setup
        when(locationRepository.addEntityToLocation(1, 1)).thenReturn(false);

        // execute
        ResponseEntity<Void> response = locationController.addEntityToLocation(1, 1);

        // verify
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(locationRepository).addEntityToLocation(1, 1);
    }

    @Test
    void addEntityToLocation_Exception() {
        // setup
        when(locationRepository.addEntityToLocation(1, 1)).thenThrow(new RuntimeException());

        // execute
        ResponseEntity<Void> response = locationController.addEntityToLocation(1, 1);

        // verify
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(locationRepository).addEntityToLocation(1, 1);
    }

    @Test
    void removeEntityFromLocation_Success() {
        // setup
        when(locationRepository.removeEntityFromLocation(1, 1)).thenReturn(true);

        // execute
        ResponseEntity<Void> response = locationController.removeEntityFromLocation(1, 1);

        // verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(locationRepository).removeEntityFromLocation(1, 1);
    }

    @Test
    void removeEntityFromLocation_NotFound() {
        // setup
        when(locationRepository.removeEntityFromLocation(1, 1)).thenReturn(false);

        // execute
        ResponseEntity<Void> response = locationController.removeEntityFromLocation(1, 1);

        // verify
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(locationRepository).removeEntityFromLocation(1, 1);
    }

    @Test
    void removeEntityFromCurrentLocation_Success() {
        // setup
        when(locationRepository.removeEntityFromCurrentLocation(1)).thenReturn(true);

        // execute
        ResponseEntity<Void> response = locationController.removeEntityFromCurrentLocation(1);

        // verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(locationRepository).removeEntityFromCurrentLocation(1);
    }

    @Test
    void removeEntityFromCurrentLocation_NotFound() {
        // setup
        when(locationRepository.removeEntityFromCurrentLocation(1)).thenReturn(false);

        // execute
        ResponseEntity<Void> response = locationController.removeEntityFromCurrentLocation(1);

        // verify
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(locationRepository).removeEntityFromCurrentLocation(1);
    }
}