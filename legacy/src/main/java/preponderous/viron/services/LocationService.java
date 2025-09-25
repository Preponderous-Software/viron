package preponderous.viron.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import preponderous.viron.config.ServiceConfig;
import preponderous.viron.exceptions.NotFoundException;
import preponderous.viron.exceptions.ServiceException;
import preponderous.viron.models.Location;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class LocationService {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    @Autowired
    public LocationService(RestTemplateBuilder restTemplateBuilder, ServiceConfig serviceConfig) {
        this.restTemplate = restTemplateBuilder.build();
        this.baseUrl = serviceConfig.getVironHost() + ":" + serviceConfig.getVironPort() + "/api/v1/locations";
    }

    public List<Location> getAllLocations() {
        try {
            ResponseEntity<Location[]> response = restTemplate.getForEntity(baseUrl, Location[].class);
            return response.getStatusCode() == HttpStatus.OK && response.getBody() != null
                    ? Arrays.asList(response.getBody())
                    : Collections.emptyList();
        } catch (Exception e) {
            log.error("Error getting locations: {}", e.getMessage());
            throw new ServiceException("Error getting locations", e);
        }
    }

    public Location getLocationById(int id) {
        try {
            ResponseEntity<Location> response = restTemplate.getForEntity(baseUrl + "/{id}", Location.class, id);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
            throw new NotFoundException("Location not found with id: " + id);
        } catch (HttpClientErrorException.NotFound e) {
            throw new NotFoundException("Location not found with id: " + id);
        } catch (Exception e) {
            log.error("Error getting location by id {}: {}", id, e.getMessage());
            throw new ServiceException("Error getting location by id: " + id, e);
        }
    }

    public List<Location> getLocationsInEnvironment(int environmentId) {
        try {
            ResponseEntity<Location[]> response = restTemplate.getForEntity(
                    baseUrl + "/environment/{environmentId}",
                    Location[].class,
                    environmentId
            );
            return response.getStatusCode() == HttpStatus.OK && response.getBody() != null
                    ? Arrays.asList(response.getBody())
                    : Collections.emptyList();
        } catch (Exception e) {
            log.error("Error getting locations in environment {}: {}", environmentId, e.getMessage());
            throw new ServiceException("Error getting locations in environment: " + environmentId, e);
        }
    }

    public List<Location> getLocationsInGrid(int gridId) {
        try {
            ResponseEntity<Location[]> response = restTemplate.getForEntity(
                    baseUrl + "/grid/{gridId}",
                    Location[].class,
                    gridId
            );
            return response.getStatusCode() == HttpStatus.OK && response.getBody() != null
                    ? Arrays.asList(response.getBody())
                    : Collections.emptyList();
        } catch (Exception e) {
            log.error("Error getting locations in grid {}: {}", gridId, e.getMessage());
            throw new ServiceException("Error getting locations in grid: " + gridId, e);
        }
    }

    public Location getLocationOfEntity(int entityId) {
        try {
            ResponseEntity<Location> response = restTemplate.getForEntity(
                    baseUrl + "/entity/{entityId}",
                    Location.class,
                    entityId
            );
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
            throw new NotFoundException("Location not found for entity: " + entityId);
        } catch (HttpClientErrorException.NotFound e) {
            throw new NotFoundException("Location not found for entity: " + entityId);
        } catch (Exception e) {
            log.error("Error getting location of entity {}: {}", entityId, e.getMessage());
            throw new ServiceException("Error getting location of entity: " + entityId, e);
        }
    }

    public void addEntityToLocation(int entityId, int locationId) {
        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    baseUrl + "/{locationId}/entity/{entityId}",
                    HttpMethod.PUT,
                    null,
                    Void.class,
                    locationId,
                    entityId
            );
            if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new NotFoundException("Location or entity not found");
            }
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new ServiceException("Failed to add entity to location");
            }
        } catch (HttpClientErrorException.NotFound e) {
            throw new NotFoundException("Location or entity not found");
        } catch (Exception e) {
            log.error("Error adding entity {} to location {}: {}", entityId, locationId, e.getMessage());
            throw new ServiceException("Error adding entity to location", e);
        }
    }

    public void removeEntityFromLocation(int entityId, int locationId) {
        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    baseUrl + "/{locationId}/entity/{entityId}",
                    HttpMethod.DELETE,
                    null,
                    Void.class,
                    locationId,
                    entityId
            );
            if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new NotFoundException("Location or entity not found");
            }
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new ServiceException("Failed to remove entity from location");
            }
        } catch (HttpClientErrorException.NotFound e) {
            throw new NotFoundException("Location or entity not found");
        } catch (Exception e) {
            log.error("Error removing entity {} from location {}: {}", entityId, locationId, e.getMessage());
            throw new ServiceException("Error removing entity from location", e);
        }
    }

    public void removeEntityFromCurrentLocation(int entityId) {
        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    baseUrl + "/entity/{entityId}",
                    HttpMethod.DELETE,
                    null,
                    Void.class,
                    entityId
            );
            if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new NotFoundException("Entity not found");
            }
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new ServiceException("Failed to remove entity from current location");
            }
        } catch (HttpClientErrorException.NotFound e) {
            throw new NotFoundException("Entity not found");
        } catch (Exception e) {
            log.error("Error removing entity {} from current location: {}", entityId, e.getMessage());
            throw new ServiceException("Error removing entity from current location", e);
        }
    }
}