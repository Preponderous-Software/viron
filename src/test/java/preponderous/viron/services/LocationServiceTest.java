package preponderous.viron.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import preponderous.viron.config.ServiceConfig;
import preponderous.viron.exceptions.ConflictException;
import preponderous.viron.exceptions.NotFoundException;
import preponderous.viron.exceptions.ServiceException;
import preponderous.viron.models.Location;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

public class LocationServiceTest {

    private static final String BASE_URL = "http://localhost:8080/api/v1/locations";
    private static final String ENTITY_AT_LOCATION_URL = BASE_URL + "/{locationId}/entity/{entityId}";

    private RestTemplate restTemplate;
    private LocationService locationService;

    @BeforeEach
    void setUp() {
        RestTemplateBuilder restTemplateBuilder = Mockito.mock(RestTemplateBuilder.class);
        restTemplate = Mockito.mock(RestTemplate.class);
        Mockito.when(restTemplateBuilder.additionalInterceptors(any(ClientHttpRequestInterceptor.class)))
                .thenReturn(restTemplateBuilder);
        Mockito.when(restTemplateBuilder.build()).thenReturn(restTemplate);

        ServiceConfig serviceConfig = new ServiceConfig();
        serviceConfig.setVironHost("http://localhost");
        serviceConfig.setVironPort(8080);

        locationService = new LocationService(restTemplateBuilder, serviceConfig);
    }

    private static HttpEntity<Void> nullRequestEntity() {
        return ArgumentMatchers.isNull();
    }

    // getAllLocations

    @Test
    void testGetAllLocations_Success_ReturnsLocations() {
        Location[] locations = {new Location(1, 0, 0), new Location(2, 1, 0)};
        Mockito.when(restTemplate.getForEntity(eq(BASE_URL), eq(Location[].class)))
                .thenReturn(ResponseEntity.ok(locations));

        List<Location> result = locationService.getAllLocations();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getLocationId()).isEqualTo(1);
    }

    @Test
    void testGetAllLocations_NullBody_ReturnsEmptyList() {
        Mockito.when(restTemplate.getForEntity(eq(BASE_URL), eq(Location[].class)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        assertThat(locationService.getAllLocations()).isEmpty();
    }

    @Test
    void testGetAllLocations_RestTemplateThrows_WrapsInServiceException() {
        Mockito.when(restTemplate.getForEntity(eq(BASE_URL), eq(Location[].class)))
                .thenThrow(new RestClientException("boom"));

        assertThatThrownBy(() -> locationService.getAllLocations())
                .isExactlyInstanceOf(ServiceException.class)
                .hasMessage("Error getting locations");
    }

    // getLocationById

    @Test
    void testGetLocationById_Success_ReturnsLocation() {
        Location location = new Location(1, 2, 3);
        Mockito.when(restTemplate.getForEntity(eq(BASE_URL + "/{id}"), eq(Location.class), eq(1)))
                .thenReturn(ResponseEntity.ok(location));

        assertThat(locationService.getLocationById(1)).isEqualTo(location);
    }

    @Test
    void testGetLocationById_NullBody_ThrowsNotFoundException() {
        Mockito.when(restTemplate.getForEntity(eq(BASE_URL + "/{id}"), eq(Location.class), eq(1)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        assertThatThrownBy(() -> locationService.getLocationById(1))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Location not found with id: 1");
    }

    @Test
    void testGetLocationById_HttpNotFound_ThrowsNotFoundException() {
        Mockito.when(restTemplate.getForEntity(eq(BASE_URL + "/{id}"), eq(Location.class), eq(1)))
                .thenThrow(HttpClientErrorException.NotFound.class);

        assertThatThrownBy(() -> locationService.getLocationById(1))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Location not found with id: 1");
    }

    @Test
    void testGetLocationById_RestTemplateThrows_WrapsInServiceException() {
        Mockito.when(restTemplate.getForEntity(eq(BASE_URL + "/{id}"), eq(Location.class), eq(1)))
                .thenThrow(new RestClientException("boom"));

        assertThatThrownBy(() -> locationService.getLocationById(1))
                .isExactlyInstanceOf(ServiceException.class)
                .hasMessage("Error getting location by id: 1");
    }

    // getLocationsInEnvironment

    @Test
    void testGetLocationsInEnvironment_Success_ReturnsLocations() {
        Location[] locations = {new Location(1, 0, 0)};
        Mockito.when(restTemplate.getForEntity(
                        eq(BASE_URL + "/environment/{environmentId}"), eq(Location[].class), eq(3)))
                .thenReturn(ResponseEntity.ok(locations));

        assertThat(locationService.getLocationsInEnvironment(3)).hasSize(1);
    }

    @Test
    void testGetLocationsInEnvironment_NullBody_ReturnsEmptyList() {
        Mockito.when(restTemplate.getForEntity(
                        eq(BASE_URL + "/environment/{environmentId}"), eq(Location[].class), eq(3)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        assertThat(locationService.getLocationsInEnvironment(3)).isEmpty();
    }

    @Test
    void testGetLocationsInEnvironment_RestTemplateThrows_WrapsInServiceException() {
        Mockito.when(restTemplate.getForEntity(
                        eq(BASE_URL + "/environment/{environmentId}"), eq(Location[].class), eq(3)))
                .thenThrow(new RestClientException("boom"));

        assertThatThrownBy(() -> locationService.getLocationsInEnvironment(3))
                .isExactlyInstanceOf(ServiceException.class)
                .hasMessage("Error getting locations in environment: 3");
    }

    // getLocationsInGrid

    @Test
    void testGetLocationsInGrid_Success_ReturnsLocations() {
        Location[] locations = {new Location(1, 0, 0), new Location(2, 0, 1)};
        Mockito.when(restTemplate.getForEntity(eq(BASE_URL + "/grid/{gridId}"), eq(Location[].class), eq(7)))
                .thenReturn(ResponseEntity.ok(locations));

        assertThat(locationService.getLocationsInGrid(7)).hasSize(2);
    }

    @Test
    void testGetLocationsInGrid_NullBody_ReturnsEmptyList() {
        Mockito.when(restTemplate.getForEntity(eq(BASE_URL + "/grid/{gridId}"), eq(Location[].class), eq(7)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        assertThat(locationService.getLocationsInGrid(7)).isEmpty();
    }

    @Test
    void testGetLocationsInGrid_RestTemplateThrows_WrapsInServiceException() {
        Mockito.when(restTemplate.getForEntity(eq(BASE_URL + "/grid/{gridId}"), eq(Location[].class), eq(7)))
                .thenThrow(new RestClientException("boom"));

        assertThatThrownBy(() -> locationService.getLocationsInGrid(7))
                .isExactlyInstanceOf(ServiceException.class)
                .hasMessage("Error getting locations in grid: 7");
    }

    // getUnoccupiedLocationsInGrid

    @Test
    void testGetUnoccupiedLocationsInGrid_Success_ReturnsLocations() {
        Location[] locations = {new Location(1, 0, 0), new Location(2, 0, 1)};
        Mockito.when(restTemplate.getForEntity(eq(BASE_URL + "/grid/{gridId}/unoccupied"), eq(Location[].class), eq(7)))
                .thenReturn(ResponseEntity.ok(locations));

        assertThat(locationService.getUnoccupiedLocationsInGrid(7)).hasSize(2);
    }

    @Test
    void testGetUnoccupiedLocationsInGrid_NullBody_ReturnsEmptyList() {
        Mockito.when(restTemplate.getForEntity(eq(BASE_URL + "/grid/{gridId}/unoccupied"), eq(Location[].class), eq(7)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        assertThat(locationService.getUnoccupiedLocationsInGrid(7)).isEmpty();
    }

    @Test
    void testGetUnoccupiedLocationsInGrid_RestTemplateThrows_WrapsInServiceException() {
        Mockito.when(restTemplate.getForEntity(eq(BASE_URL + "/grid/{gridId}/unoccupied"), eq(Location[].class), eq(7)))
                .thenThrow(new RestClientException("boom"));

        assertThatThrownBy(() -> locationService.getUnoccupiedLocationsInGrid(7))
                .isExactlyInstanceOf(ServiceException.class)
                .hasMessage("Error getting unoccupied locations in grid: 7");
    }

    // getLocationOfEntity

    @Test
    void testGetLocationOfEntity_Success_ReturnsLocation() {
        Location location = new Location(4, 1, 1);
        Mockito.when(restTemplate.getForEntity(eq(BASE_URL + "/entity/{entityId}"), eq(Location.class), eq(9)))
                .thenReturn(ResponseEntity.ok(location));

        assertThat(locationService.getLocationOfEntity(9)).isEqualTo(location);
    }

    @Test
    void testGetLocationOfEntity_NullBody_ThrowsNotFoundException() {
        Mockito.when(restTemplate.getForEntity(eq(BASE_URL + "/entity/{entityId}"), eq(Location.class), eq(9)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        assertThatThrownBy(() -> locationService.getLocationOfEntity(9))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Location not found for entity: 9");
    }

    @Test
    void testGetLocationOfEntity_HttpNotFound_ThrowsNotFoundException() {
        Mockito.when(restTemplate.getForEntity(eq(BASE_URL + "/entity/{entityId}"), eq(Location.class), eq(9)))
                .thenThrow(HttpClientErrorException.NotFound.class);

        assertThatThrownBy(() -> locationService.getLocationOfEntity(9))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Location not found for entity: 9");
    }

    @Test
    void testGetLocationOfEntity_RestTemplateThrows_WrapsInServiceException() {
        Mockito.when(restTemplate.getForEntity(eq(BASE_URL + "/entity/{entityId}"), eq(Location.class), eq(9)))
                .thenThrow(new RestClientException("boom"));

        assertThatThrownBy(() -> locationService.getLocationOfEntity(9))
                .isExactlyInstanceOf(ServiceException.class)
                .hasMessage("Error getting location of entity: 9");
    }

    // addEntityToLocation

    @Test
    void testAddEntityToLocation_Success_DoesNotThrow() {
        Mockito.when(restTemplate.exchange(
                        eq(ENTITY_AT_LOCATION_URL), eq(HttpMethod.PUT), nullRequestEntity(),
                        eq(Void.class), eq(5), eq(2)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        assertThatCode(() -> locationService.addEntityToLocation(2, 5)).doesNotThrowAnyException();
    }

    @Test
    void testAddEntityToLocation_NotFoundStatus_ThrowsNotFoundException() {
        Mockito.when(restTemplate.exchange(
                        eq(ENTITY_AT_LOCATION_URL), eq(HttpMethod.PUT), nullRequestEntity(),
                        eq(Void.class), eq(5), eq(2)))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> locationService.addEntityToLocation(2, 5))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Location or entity not found");
    }

    @Test
    void testAddEntityToLocation_NonSuccessStatus_ThrowsServiceException() {
        Mockito.when(restTemplate.exchange(
                        eq(ENTITY_AT_LOCATION_URL), eq(HttpMethod.PUT), nullRequestEntity(),
                        eq(Void.class), eq(5), eq(2)))
                .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        assertThatThrownBy(() -> locationService.addEntityToLocation(2, 5))
                .isExactlyInstanceOf(ServiceException.class)
                .hasMessage("Failed to add entity to location");
    }

    @Test
    void testAddEntityToLocation_HttpNotFound_ThrowsNotFoundException() {
        Mockito.when(restTemplate.exchange(
                        eq(ENTITY_AT_LOCATION_URL), eq(HttpMethod.PUT), nullRequestEntity(),
                        eq(Void.class), eq(5), eq(2)))
                .thenThrow(HttpClientErrorException.NotFound.class);

        assertThatThrownBy(() -> locationService.addEntityToLocation(2, 5))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Location or entity not found");
    }

    @Test
    void testAddEntityToLocation_RestTemplateThrows_WrapsInServiceException() {
        Mockito.when(restTemplate.exchange(
                        eq(ENTITY_AT_LOCATION_URL), eq(HttpMethod.PUT), nullRequestEntity(),
                        eq(Void.class), eq(5), eq(2)))
                .thenThrow(new RestClientException("boom"));

        assertThatThrownBy(() -> locationService.addEntityToLocation(2, 5))
                .isExactlyInstanceOf(ServiceException.class)
                .hasMessage("Error adding entity to location");
    }

    // removeEntityFromLocation

    @Test
    void testRemoveEntityFromLocation_Success_DoesNotThrow() {
        Mockito.when(restTemplate.exchange(
                        eq(ENTITY_AT_LOCATION_URL), eq(HttpMethod.DELETE), nullRequestEntity(),
                        eq(Void.class), eq(5), eq(2)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        assertThatCode(() -> locationService.removeEntityFromLocation(2, 5)).doesNotThrowAnyException();
    }

    @Test
    void testRemoveEntityFromLocation_NotFoundStatus_ThrowsNotFoundException() {
        Mockito.when(restTemplate.exchange(
                        eq(ENTITY_AT_LOCATION_URL), eq(HttpMethod.DELETE), nullRequestEntity(),
                        eq(Void.class), eq(5), eq(2)))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> locationService.removeEntityFromLocation(2, 5))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Location or entity not found");
    }

    @Test
    void testRemoveEntityFromLocation_NonSuccessStatus_ThrowsServiceException() {
        Mockito.when(restTemplate.exchange(
                        eq(ENTITY_AT_LOCATION_URL), eq(HttpMethod.DELETE), nullRequestEntity(),
                        eq(Void.class), eq(5), eq(2)))
                .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        assertThatThrownBy(() -> locationService.removeEntityFromLocation(2, 5))
                .isExactlyInstanceOf(ServiceException.class)
                .hasMessage("Failed to remove entity from location");
    }

    @Test
    void testRemoveEntityFromLocation_HttpNotFound_ThrowsNotFoundException() {
        Mockito.when(restTemplate.exchange(
                        eq(ENTITY_AT_LOCATION_URL), eq(HttpMethod.DELETE), nullRequestEntity(),
                        eq(Void.class), eq(5), eq(2)))
                .thenThrow(HttpClientErrorException.NotFound.class);

        assertThatThrownBy(() -> locationService.removeEntityFromLocation(2, 5))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Location or entity not found");
    }

    @Test
    void testRemoveEntityFromLocation_RestTemplateThrows_WrapsInServiceException() {
        Mockito.when(restTemplate.exchange(
                        eq(ENTITY_AT_LOCATION_URL), eq(HttpMethod.DELETE), nullRequestEntity(),
                        eq(Void.class), eq(5), eq(2)))
                .thenThrow(new RestClientException("boom"));

        assertThatThrownBy(() -> locationService.removeEntityFromLocation(2, 5))
                .isExactlyInstanceOf(ServiceException.class)
                .hasMessage("Error removing entity from location");
    }

    // removeEntityFromCurrentLocation

    @Test
    void testRemoveEntityFromCurrentLocation_Success_DoesNotThrow() {
        Mockito.when(restTemplate.exchange(
                        eq(BASE_URL + "/entity/{entityId}"), eq(HttpMethod.DELETE), nullRequestEntity(),
                        eq(Void.class), eq(2)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        assertThatCode(() -> locationService.removeEntityFromCurrentLocation(2)).doesNotThrowAnyException();
    }

    @Test
    void testRemoveEntityFromCurrentLocation_NotFoundStatus_ThrowsNotFoundException() {
        Mockito.when(restTemplate.exchange(
                        eq(BASE_URL + "/entity/{entityId}"), eq(HttpMethod.DELETE), nullRequestEntity(),
                        eq(Void.class), eq(2)))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> locationService.removeEntityFromCurrentLocation(2))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Entity not found");
    }

    @Test
    void testRemoveEntityFromCurrentLocation_NonSuccessStatus_ThrowsServiceException() {
        Mockito.when(restTemplate.exchange(
                        eq(BASE_URL + "/entity/{entityId}"), eq(HttpMethod.DELETE), nullRequestEntity(),
                        eq(Void.class), eq(2)))
                .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        assertThatThrownBy(() -> locationService.removeEntityFromCurrentLocation(2))
                .isExactlyInstanceOf(ServiceException.class)
                .hasMessage("Failed to remove entity from current location");
    }

    @Test
    void testRemoveEntityFromCurrentLocation_HttpNotFound_ThrowsNotFoundException() {
        Mockito.when(restTemplate.exchange(
                        eq(BASE_URL + "/entity/{entityId}"), eq(HttpMethod.DELETE), nullRequestEntity(),
                        eq(Void.class), eq(2)))
                .thenThrow(HttpClientErrorException.NotFound.class);

        assertThatThrownBy(() -> locationService.removeEntityFromCurrentLocation(2))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Entity not found");
    }

    @Test
    void testRemoveEntityFromCurrentLocation_RestTemplateThrows_WrapsInServiceException() {
        Mockito.when(restTemplate.exchange(
                        eq(BASE_URL + "/entity/{entityId}"), eq(HttpMethod.DELETE), nullRequestEntity(),
                        eq(Void.class), eq(2)))
                .thenThrow(new RestClientException("boom"));

        assertThatThrownBy(() -> locationService.removeEntityFromCurrentLocation(2))
                .isExactlyInstanceOf(ServiceException.class)
                .hasMessage("Error removing entity from current location");
    }

    // getEntityIdsAtLocation

    @Test
    void testGetEntityIdsAtLocation_Success_ReturnsIds() {
        Integer[] ids = {4, 8};
        Mockito.when(restTemplate.getForEntity(eq(BASE_URL + "/{locationId}/entities"), eq(Integer[].class), eq(5)))
                .thenReturn(ResponseEntity.ok(ids));

        assertThat(locationService.getEntityIdsAtLocation(5)).containsExactly(4, 8);
    }

    @Test
    void testGetEntityIdsAtLocation_NullBody_ReturnsEmptyList() {
        Mockito.when(restTemplate.getForEntity(eq(BASE_URL + "/{locationId}/entities"), eq(Integer[].class), eq(5)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        assertThat(locationService.getEntityIdsAtLocation(5)).isEmpty();
    }

    @Test
    void testGetEntityIdsAtLocation_HttpNotFound_ThrowsNotFoundException() {
        Mockito.when(restTemplate.getForEntity(eq(BASE_URL + "/{locationId}/entities"), eq(Integer[].class), eq(5)))
                .thenThrow(HttpClientErrorException.NotFound.class);

        assertThatThrownBy(() -> locationService.getEntityIdsAtLocation(5))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Location not found with id: 5");
    }

    @Test
    void testGetEntityIdsAtLocation_RestTemplateThrows_WrapsInServiceException() {
        Mockito.when(restTemplate.getForEntity(eq(BASE_URL + "/{locationId}/entities"), eq(Integer[].class), eq(5)))
                .thenThrow(new RestClientException("boom"));

        assertThatThrownBy(() -> locationService.getEntityIdsAtLocation(5))
                .isExactlyInstanceOf(ServiceException.class)
                .hasMessage("Error getting entity ids at location: 5");
    }

    // isLocationOccupied

    @Test
    void testIsLocationOccupied_Success_ReturnsBody() {
        Mockito.when(restTemplate.getForEntity(eq(BASE_URL + "/{locationId}/occupied"), eq(Boolean.class), eq(5)))
                .thenReturn(ResponseEntity.ok(true));

        assertThat(locationService.isLocationOccupied(5)).isTrue();
    }

    @Test
    void testIsLocationOccupied_NullBody_ThrowsServiceException() {
        Mockito.when(restTemplate.getForEntity(eq(BASE_URL + "/{locationId}/occupied"), eq(Boolean.class), eq(5)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        assertThatThrownBy(() -> locationService.isLocationOccupied(5))
                .isExactlyInstanceOf(ServiceException.class)
                .hasMessage("Failed to check occupancy for location: 5");
    }

    @Test
    void testIsLocationOccupied_HttpNotFound_ThrowsNotFoundException() {
        Mockito.when(restTemplate.getForEntity(eq(BASE_URL + "/{locationId}/occupied"), eq(Boolean.class), eq(5)))
                .thenThrow(HttpClientErrorException.NotFound.class);

        assertThatThrownBy(() -> locationService.isLocationOccupied(5))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Location not found with id: 5");
    }

    @Test
    void testIsLocationOccupied_RestTemplateThrows_WrapsInServiceException() {
        Mockito.when(restTemplate.getForEntity(eq(BASE_URL + "/{locationId}/occupied"), eq(Boolean.class), eq(5)))
                .thenThrow(new RestClientException("boom"));

        assertThatThrownBy(() -> locationService.isLocationOccupied(5))
                .isExactlyInstanceOf(ServiceException.class)
                .hasMessage("Error checking occupancy for location: 5");
    }

    // getNeighbors

    @Test
    void testGetNeighbors_Success_ReturnsLocations() {
        Location[] neighbors = {new Location(4, 0, 1), new Location(6, 1, 0)};
        Mockito.when(restTemplate.getForEntity(eq(BASE_URL + "/{locationId}/neighbors"), eq(Location[].class), eq(5)))
                .thenReturn(ResponseEntity.ok(neighbors));

        assertThat(locationService.getNeighbors(5)).hasSize(2);
    }

    @Test
    void testGetNeighbors_NullBody_ReturnsEmptyList() {
        Mockito.when(restTemplate.getForEntity(eq(BASE_URL + "/{locationId}/neighbors"), eq(Location[].class), eq(5)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        assertThat(locationService.getNeighbors(5)).isEmpty();
    }

    @Test
    void testGetNeighbors_HttpNotFound_ThrowsNotFoundException() {
        Mockito.when(restTemplate.getForEntity(eq(BASE_URL + "/{locationId}/neighbors"), eq(Location[].class), eq(5)))
                .thenThrow(HttpClientErrorException.NotFound.class);

        assertThatThrownBy(() -> locationService.getNeighbors(5))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Location not found with id: 5");
    }

    @Test
    void testGetNeighbors_RestTemplateThrows_WrapsInServiceException() {
        Mockito.when(restTemplate.getForEntity(eq(BASE_URL + "/{locationId}/neighbors"), eq(Location[].class), eq(5)))
                .thenThrow(new RestClientException("boom"));

        assertThatThrownBy(() -> locationService.getNeighbors(5))
                .isExactlyInstanceOf(ServiceException.class)
                .hasMessage("Error getting neighbors of location: 5");
    }

    // moveEntityToLocation

    @Test
    void testMoveEntityToLocation_Success_DoesNotThrow() {
        Mockito.when(restTemplate.exchange(
                        eq(ENTITY_AT_LOCATION_URL + "/move"), eq(HttpMethod.PUT), nullRequestEntity(),
                        eq(Void.class), eq(5), eq(2)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        assertThatCode(() -> locationService.moveEntityToLocation(2, 5)).doesNotThrowAnyException();
    }

    @Test
    void testMoveEntityToLocation_NotFoundStatus_ThrowsNotFoundException() {
        Mockito.when(restTemplate.exchange(
                        eq(ENTITY_AT_LOCATION_URL + "/move"), eq(HttpMethod.PUT), nullRequestEntity(),
                        eq(Void.class), eq(5), eq(2)))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> locationService.moveEntityToLocation(2, 5))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Entity or location not found");
    }

    @Test
    void testMoveEntityToLocation_ConflictStatus_ThrowsConflictException() {
        Mockito.when(restTemplate.exchange(
                        eq(ENTITY_AT_LOCATION_URL + "/move"), eq(HttpMethod.PUT), nullRequestEntity(),
                        eq(Void.class), eq(5), eq(2)))
                .thenReturn(new ResponseEntity<>(HttpStatus.CONFLICT));

        assertThatThrownBy(() -> locationService.moveEntityToLocation(2, 5))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Target location 5 is already occupied");
    }

    @Test
    void testMoveEntityToLocation_NonSuccessStatus_ThrowsServiceException() {
        Mockito.when(restTemplate.exchange(
                        eq(ENTITY_AT_LOCATION_URL + "/move"), eq(HttpMethod.PUT), nullRequestEntity(),
                        eq(Void.class), eq(5), eq(2)))
                .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        assertThatThrownBy(() -> locationService.moveEntityToLocation(2, 5))
                .isExactlyInstanceOf(ServiceException.class)
                .hasMessage("Failed to move entity to location");
    }

    @Test
    void testMoveEntityToLocation_HttpNotFound_ThrowsNotFoundException() {
        Mockito.when(restTemplate.exchange(
                        eq(ENTITY_AT_LOCATION_URL + "/move"), eq(HttpMethod.PUT), nullRequestEntity(),
                        eq(Void.class), eq(5), eq(2)))
                .thenThrow(HttpClientErrorException.NotFound.class);

        assertThatThrownBy(() -> locationService.moveEntityToLocation(2, 5))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Entity or location not found");
    }

    @Test
    void testMoveEntityToLocation_HttpConflict_ThrowsConflictException() {
        Mockito.when(restTemplate.exchange(
                        eq(ENTITY_AT_LOCATION_URL + "/move"), eq(HttpMethod.PUT), nullRequestEntity(),
                        eq(Void.class), eq(5), eq(2)))
                .thenThrow(HttpClientErrorException.Conflict.class);

        assertThatThrownBy(() -> locationService.moveEntityToLocation(2, 5))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Target location 5 is already occupied");
    }

    @Test
    void testMoveEntityToLocation_RestTemplateThrows_WrapsInServiceException() {
        Mockito.when(restTemplate.exchange(
                        eq(ENTITY_AT_LOCATION_URL + "/move"), eq(HttpMethod.PUT), nullRequestEntity(),
                        eq(Void.class), eq(5), eq(2)))
                .thenThrow(new RestClientException("boom"));

        assertThatThrownBy(() -> locationService.moveEntityToLocation(2, 5))
                .isExactlyInstanceOf(ServiceException.class)
                .hasMessage("Error moving entity to location");
    }
}
