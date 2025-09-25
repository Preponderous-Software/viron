
// Copyright (c) 2024 Preponderous Software
// MIT License

package preponderous.viron.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import preponderous.viron.exceptions.EnvironmentCreationException;
import preponderous.viron.factories.EnvironmentFactory;
import preponderous.viron.models.Environment;
import preponderous.viron.repositories.EnvironmentRepository;

import java.util.List;

@RestController
@RequestMapping("/api/v1/environments")
@Slf4j
@RequiredArgsConstructor
public class EnvironmentController {
    private final EnvironmentRepository environmentRepository;
    private final EnvironmentFactory environmentFactory;

    @GetMapping
    public ResponseEntity<List<Environment>> getAllEnvironments() {
        try {
            return ResponseEntity.ok(environmentRepository.findAll());
        } catch (Exception e) {
            log.error("Error fetching all environments: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Environment> getEnvironmentById(@PathVariable int id) {
        try {
            return environmentRepository.findById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error fetching environment by id {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<Environment> getEnvironmentByName(@PathVariable String name) {
        try {
            return environmentRepository.findByName(name)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error fetching environment by name {}: {}", name, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/entity/{entityId}")
    public ResponseEntity<Environment> getEnvironmentOfEntity(@PathVariable int entityId) {
        try {
            return environmentRepository.findByEntityId(entityId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error fetching environment for entity {}: {}", entityId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{name}/{numGrids}/{gridSize}")
    public ResponseEntity<Environment> createEnvironment(
            @PathVariable String name,
            @PathVariable int numGrids,
            @PathVariable int gridSize) {
        try {
            Environment newEnvironment = environmentFactory.createEnvironment(name, numGrids, gridSize);
            return ResponseEntity.ok(newEnvironment);
        } catch (EnvironmentCreationException e) {
            log.error("Error creating environment with name {}: {}", name, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Unexpected error creating environment: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEnvironment(@PathVariable int id) {
        try {
            if (environmentRepository.findById(id).isEmpty()) {
                log.info("Environment with id {} does not exist, cannot delete", id);
                return ResponseEntity.notFound().build();
            }

            log.info("Attempting to delete environment with id {}", id);

            List<Integer> entityIds = environmentRepository.findEntityIdsByEnvironmentId(id);
            List<Integer> locationIds = environmentRepository.findLocationIdsByEnvironmentId(id);
            List<Integer> gridIds = environmentRepository.findGridIdsByEnvironmentId(id);

            // Delete associations
            for (int entityId : entityIds) {
                if (!environmentRepository.deleteEntityLocation(entityId)) {
                    log.error("Error deleting entity_location with entity id {}", entityId);
                    return ResponseEntity.internalServerError().build();
                }
            }

            for (int locationId : locationIds) {
                if (!environmentRepository.deleteLocationGrid(locationId)) {
                    log.error("Error deleting location_grid with location id {}", locationId);
                    return ResponseEntity.internalServerError().build();
                }
            }

            for (int gridId : gridIds) {
                if (!environmentRepository.deleteGridEnvironment(gridId)) {
                    log.error("Error deleting grid_environment with grid id {}", gridId);
                    return ResponseEntity.internalServerError().build();
                }
            }

            // Delete entities
            for (int entityId : entityIds) {
                if (!environmentRepository.deleteEntity(entityId)) {
                    log.error("Error deleting entity with id {}", entityId);
                    return ResponseEntity.internalServerError().build();
                }
            }
            if (!entityIds.isEmpty()) {
                log.info("Entities deleted: {}", entityIds);
            }

            // Delete locations
            for (int locationId : locationIds) {
                if (!environmentRepository.deleteLocation(locationId)) {
                    log.error("Error deleting location with id {}", locationId);
                    return ResponseEntity.internalServerError().build();
                }
            }
            if (!locationIds.isEmpty()) {
                log.info("Locations deleted: {}", locationIds);
            }

            // Delete grids
            for (int gridId : gridIds) {
                if (!environmentRepository.deleteGrid(gridId)) {
                    log.error("Error deleting grid with id {}", gridId);
                    return ResponseEntity.internalServerError().build();
                }
            }
            if (!gridIds.isEmpty()) {
                log.info("Grids deleted: {}", gridIds);
            }

            // Delete environment
            if (!environmentRepository.deleteById(id)) {
                log.error("Error deleting environment with id {}", id);
                return ResponseEntity.internalServerError().build();
            }

            log.info("Environment with id {} deleted", id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error deleting environment {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/{id}/name/{name}")
    public ResponseEntity<Void> updateEnvironmentName(@PathVariable int id, @PathVariable String name) {
        try {
            // First check if the environment exists
            if (environmentRepository.findById(id).isEmpty()) {
                log.info("Environment with id {} not found", id);
                return ResponseEntity.notFound().build();
            }

            boolean updated = environmentRepository.updateName(id, name);
            if (updated) {
                log.info("Updated name for environment {} to '{}'", id, name);
                return ResponseEntity.ok().build();
            } else {
                log.error("Failed to update name for environment {}", id);
                return ResponseEntity.internalServerError().build();
            }
        } catch (Exception e) {
            log.error("Error updating name for environment {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}