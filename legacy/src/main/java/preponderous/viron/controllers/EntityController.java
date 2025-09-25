package preponderous.viron.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import preponderous.viron.exceptions.EntityCreationException;
import preponderous.viron.factories.EntityFactory;
import preponderous.viron.models.Entity;
import preponderous.viron.repositories.EntityRepository;

import java.util.List;

@RestController
@RequestMapping("/api/v1/entities")
@Slf4j
@RequiredArgsConstructor
public class EntityController {
    private final EntityRepository entityRepository;
    private final EntityFactory entityFactory;

    @GetMapping
    public ResponseEntity<List<Entity>> getAllEntities() {
        try {
            return ResponseEntity.ok(entityRepository.findAll());
        } catch (Exception e) {
            log.error("Error fetching all entities: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Entity> getEntityById(@PathVariable int id) {
        try {
            return entityRepository.findById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error fetching entity by id {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/environment/{environmentId}")
    public ResponseEntity<List<Entity>> getEntitiesInEnvironment(@PathVariable int environmentId) {
        try {
            return ResponseEntity.ok(entityRepository.findByEnvironmentId(environmentId));
        } catch (Exception e) {
            log.error("Error fetching entities in environment {}: {}", environmentId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/grid/{gridId}")
    public ResponseEntity<List<Entity>> getEntitiesInGrid(@PathVariable int gridId) {
        try {
            return ResponseEntity.ok(entityRepository.findByGridId(gridId));
        } catch (Exception e) {
            log.error("Error fetching entities in grid {}: {}", gridId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/location/{locationId}")
    public ResponseEntity<List<Entity>> getEntitiesInLocation(@PathVariable int locationId) {
        try {
            return ResponseEntity.ok(entityRepository.findByLocationId(locationId));
        } catch (Exception e) {
            log.error("Error fetching entities in location {}: {}", locationId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/unassigned")
    public ResponseEntity<List<Entity>> getEntitiesNotInAnyLocation() {
        try {
            return ResponseEntity.ok(entityRepository.findEntitiesNotInAnyLocation());
        } catch (Exception e) {
            log.error("Error fetching unassigned entities: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{name}")
    public ResponseEntity<Entity> createEntity(@PathVariable String name) {
        try {
            Entity newEntity = entityFactory.createEntity(name);
            return ResponseEntity.ok(newEntity);
        } catch (EntityCreationException e) {
            log.error("Error creating entity with name {}: {}", name, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Unexpected error creating entity: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntity(@PathVariable int id) {
        try {
            return entityRepository.deleteById(id)
                    ? ResponseEntity.ok().build()
                    : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error deleting entity {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/{id}/name/{name}")
    public ResponseEntity<Void> updateEntityName(@PathVariable int id, @PathVariable String name) {
        try {
            return entityRepository.updateName(id, name)
                    ? ResponseEntity.ok().build()
                    : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error updating name for entity {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}