package preponderous.viron.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import preponderous.viron.config.ServiceConfig;
import preponderous.viron.exceptions.EntityServiceException;
import preponderous.viron.models.Entity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class EntityService {
    private final RestTemplate restTemplate;
    private final ServiceConfig serviceConfig;

    private String getBaseUrl() {
        return String.format("%s:%d/api/v1/entities",
                serviceConfig.getVironHost(),
                serviceConfig.getVironPort());
    }

    public List<Entity> getAllEntities() {
        try {
            ResponseEntity<Entity[]> response = restTemplate.getForEntity(
                    getBaseUrl(),
                    Entity[].class
            );

            return response.getBody() != null
                    ? Arrays.asList(response.getBody())
                    : Collections.emptyList();

        } catch (Exception e) {
            log.error("Failed to fetch all entities: {}", e.getMessage());
            throw new EntityServiceException("Error retrieving entities", e);
        }
    }

    public Optional<Entity> getEntityById(int id) {
        try {
            ResponseEntity<Entity> response = restTemplate.getForEntity(
                    getBaseUrl() + "/{id}",
                    Entity.class,
                    id
            );

            return Optional.ofNullable(response.getBody());

        } catch (Exception e) {
            log.error("Failed to fetch entity with id {}: {}", id, e.getMessage());
            throw new EntityServiceException("Error retrieving entity", e);
        }
    }

    public List<Entity> getEntitiesInEnvironment(int environmentId) {
        try {
            ResponseEntity<Entity[]> response = restTemplate.getForEntity(
                    getBaseUrl() + "/environment/{environmentId}",
                    Entity[].class,
                    environmentId
            );

            return response.getBody() != null
                    ? Arrays.asList(response.getBody())
                    : Collections.emptyList();

        } catch (Exception e) {
            log.error("Failed to fetch entities in environment {}: {}", environmentId, e.getMessage());
            throw new EntityServiceException("Error retrieving entities in environment", e);
        }
    }

    public List<Entity> getEntitiesInGrid(int gridId) {
        try {
            ResponseEntity<Entity[]> response = restTemplate.getForEntity(
                    getBaseUrl() + "/grid/{gridId}",
                    Entity[].class,
                    gridId
            );

            return response.getBody() != null
                    ? Arrays.asList(response.getBody())
                    : Collections.emptyList();

        } catch (Exception e) {
            log.error("Failed to fetch entities in grid {}: {}", gridId, e.getMessage());
            throw new EntityServiceException("Error retrieving entities in grid", e);
        }
    }

    public List<Entity> getEntitiesInLocation(int locationId) {
        try {
            ResponseEntity<Entity[]> response = restTemplate.getForEntity(
                    getBaseUrl() + "/location/{locationId}",
                    Entity[].class,
                    locationId
            );

            return response.getBody() != null
                    ? Arrays.asList(response.getBody())
                    : Collections.emptyList();

        } catch (Exception e) {
            log.error("Failed to fetch entities in location {}: {}", locationId, e.getMessage());
            throw new EntityServiceException("Error retrieving entities in location", e);
        }
    }

    public List<Entity> getEntitiesNotInAnyLocation() {
        try {
            ResponseEntity<Entity[]> response = restTemplate.getForEntity(
                    getBaseUrl() + "/unassigned",
                    Entity[].class
            );

            return response.getBody() != null
                    ? Arrays.asList(response.getBody())
                    : Collections.emptyList();

        } catch (Exception e) {
            log.error("Failed to fetch unassigned entities: {}", e.getMessage());
            throw new EntityServiceException("Error retrieving unassigned entities", e);
        }
    }

    public Entity createEntity(String name) {
        try {
            ResponseEntity<Entity> response = restTemplate.postForEntity(
                    getBaseUrl() + "/{name}",
                    null,
                    Entity.class,
                    name
            );

            if (response.getBody() == null) {
                throw new EntityServiceException("Created entity response was null");
            }

            return response.getBody();

        } catch (Exception e) {
            log.error("Failed to create entity with name {}: {}", name, e.getMessage());
            throw new EntityServiceException("Error creating entity", e);
        }
    }

    public boolean deleteEntity(int id) {
        try {
            restTemplate.delete(getBaseUrl() + "/{id}", id);
            return true;
        } catch (Exception e) {
            log.error("Failed to delete entity {}: {}", id, e.getMessage());
            throw new EntityServiceException("Error deleting entity", e);
        }
    }

    public boolean updateEntityName(int id, String name) {
        try {
            restTemplate.patchForObject(
                    getBaseUrl() + "/{id}/name/{name}",
                    null,
                    Void.class,
                    id,
                    name
            );
            return true;
        } catch (Exception e) {
            log.error("Failed to update name for entity {}: {}", id, e.getMessage());
            throw new EntityServiceException("Error updating entity name", e);
        }
    }
}