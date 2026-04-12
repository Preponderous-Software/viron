package preponderous.viron.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import preponderous.viron.dto.EntityDto;
import preponderous.viron.exceptions.NotFoundException;
import preponderous.viron.factories.EntityFactory;
import preponderous.viron.mappers.EntityMapper;
import preponderous.viron.models.Entity;
import preponderous.viron.repositories.EntityRepository;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

@RestController
@RequestMapping("/api/v1/entities")
@Slf4j
@RequiredArgsConstructor
@Validated
public class EntityController {
    private final EntityRepository entityRepository;
    private final EntityFactory entityFactory;
    private final EntityMapper entityMapper;

    @GetMapping
    public ResponseEntity<List<EntityDto>> getAllEntities() {
        List<Entity> entities = entityRepository.findAll();
        return ResponseEntity.ok(entityMapper.toDtoList(entities));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityDto> getEntityById(@PathVariable @Min(1) int id) {
        Entity entity = entityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Entity not found with id: " + id));
        return ResponseEntity.ok(entityMapper.toDto(entity));
    }

    @GetMapping("/environment/{environmentId}")
    public ResponseEntity<List<EntityDto>> getEntitiesInEnvironment(@PathVariable @Min(1) int environmentId) {
        List<Entity> entities = entityRepository.findByEnvironmentId(environmentId);
        return ResponseEntity.ok(entityMapper.toDtoList(entities));
    }

    @GetMapping("/grid/{gridId}")
    public ResponseEntity<List<EntityDto>> getEntitiesInGrid(@PathVariable @Min(1) int gridId) {
        List<Entity> entities = entityRepository.findByGridId(gridId);
        return ResponseEntity.ok(entityMapper.toDtoList(entities));
    }

    @GetMapping("/location/{locationId}")
    public ResponseEntity<List<EntityDto>> getEntitiesInLocation(@PathVariable @Min(1) int locationId) {
        List<Entity> entities = entityRepository.findByLocationId(locationId);
        return ResponseEntity.ok(entityMapper.toDtoList(entities));
    }

    @GetMapping("/unassigned")
    public ResponseEntity<List<EntityDto>> getEntitiesNotInAnyLocation() {
        List<Entity> entities = entityRepository.findEntitiesNotInAnyLocation();
        return ResponseEntity.ok(entityMapper.toDtoList(entities));
    }

    @PostMapping("/{name}")
    public ResponseEntity<EntityDto> createEntity(@PathVariable @NotBlank String name) {
        Entity newEntity = entityFactory.createEntity(name);
        return ResponseEntity.status(HttpStatus.CREATED).body(entityMapper.toDto(newEntity));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteEntity(@PathVariable @Min(1) int id) {
        if (!entityRepository.deleteById(id)) {
            throw new NotFoundException("Entity not found with id: " + id);
        }
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/name/{name}")
    public ResponseEntity<Void> updateEntityName(@PathVariable @Min(1) int id, @PathVariable @NotBlank String name) {
        if (!entityRepository.updateName(id, name)) {
            throw new NotFoundException("Entity not found with id: " + id);
        }
        return ResponseEntity.ok().build();
    }
}
