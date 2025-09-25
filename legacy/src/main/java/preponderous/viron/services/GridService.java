package preponderous.viron.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import preponderous.viron.config.ServiceConfig;
import preponderous.viron.exceptions.ServiceException;
import preponderous.viron.models.Grid;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class GridService {
    private final RestTemplateBuilder restTemplateBuilder;
    private final String baseUrl;

    @Autowired
    public GridService(RestTemplateBuilder restTemplateBuilder, ServiceConfig serviceConfig) {
        this.restTemplateBuilder = restTemplateBuilder;
        this.baseUrl = serviceConfig.getVironHost() + ":" + serviceConfig.getVironPort() + "/api/v1/grids";
    }

    public List<Grid> getAllGrids() {
        try {
            RestTemplate restTemplate = restTemplateBuilder.build();
            ResponseEntity<Grid[]> response = restTemplate.getForEntity(baseUrl, Grid[].class);
            return Arrays.asList(response.getBody());
        } catch (Exception e) {
            log.error("Error getting all grids: {}", e.getMessage());
            throw new ServiceException("Failed to fetch all grids", e);
        }
    }

    public Optional<Grid> getGridById(int id) {
        try {
            RestTemplate restTemplate = restTemplateBuilder.build();
            ResponseEntity<Grid> response = restTemplate.getForEntity(baseUrl + "/{id}", Grid.class, id);
            return Optional.ofNullable(response.getBody());
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error getting grid by id {}: {}", id, e.getMessage());
            throw new ServiceException("Failed to fetch grid by id: " + id, e);
        }
    }

    public List<Grid> getGridsInEnvironment(int environmentId) {
        try {
            RestTemplate restTemplate = restTemplateBuilder.build();
            ResponseEntity<Grid[]> response = restTemplate.getForEntity(
                    baseUrl + "/environment/{environmentId}",
                    Grid[].class,
                    environmentId
            );
            return Arrays.asList(response.getBody());
        } catch (Exception e) {
            log.error("Error getting grids in environment {}: {}", environmentId, e.getMessage());
            throw new ServiceException("Failed to fetch grids in environment: " + environmentId, e);
        }
    }

    public Optional<Grid> getGridOfEntity(int entityId) {
        try {
            RestTemplate restTemplate = restTemplateBuilder.build();
            ResponseEntity<Grid> response = restTemplate.getForEntity(
                    baseUrl + "/entity/{entityId}",
                    Grid.class,
                    entityId
            );
            return Optional.ofNullable(response.getBody());
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error getting grid for entity {}: {}", entityId, e.getMessage());
            throw new ServiceException("Failed to fetch grid for entity: " + entityId, e);
        }
    }
}