package preponderous.viron.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import preponderous.viron.dto.CreateEntityRequest;
import preponderous.viron.dto.EntityDto;
import preponderous.viron.dto.UpdateEntityNameRequest;
import preponderous.viron.exceptions.NotFoundException;
import preponderous.viron.exceptions.ServiceException;
import preponderous.viron.factories.EntityFactory;
import preponderous.viron.mappers.EntityMapper;
import preponderous.viron.models.Entity;
import preponderous.viron.repositories.EntityRepository;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
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
    public List<EntityDto> getAllEntities() {
        List<Entity> entities = entityRepository.findAll();
        return entityMapper.toDtoList(entities);
    }

    @GetMapping("/{id}")
    public EntityDto getEntityById(@PathVariable @Min(1) int id) {
        Entity entity = entityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Entity not found with id: " + id));
        return entityMapper.toDto(entity);
    }

    @GetMapping("/environment/{environmentId}")
    public List<EntityDto> getEntitiesInEnvironment(@PathVariable @Min(1) int environmentId) {
        List<Entity> entities = entityRepository.findByEnvironmentId(environmentId);
        return entityMapper.toDtoList(entities);
    }

    @GetMapping("/grid/{gridId}")
    public List<EntityDto> getEntitiesInGrid(@PathVariable @Min(1) int gridId) {
        List<Entity> entities = entityRepository.findByGridId(gridId);
        return entityMapper.toDtoList(entities);
    }

    @GetMapping("/location/{locationId}")
    public List<EntityDto> getEntitiesInLocation(@PathVariable @Min(1) int locationId) {
        List<Entity> entities = entityRepository.findByLocationId(locationId);
        return entityMapper.toDtoList(entities);
    }

    @GetMapping("/unassigned")
    public List<EntityDto> getEntitiesNotInAnyLocation() {
        List<Entity> entities = entityRepository.findEntitiesNotInAnyLocation();
        return entityMapper.toDtoList(entities);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EntityDto createEntity(@Valid @RequestBody CreateEntityRequest request) {
        Entity newEntity = entityFactory.createEntity(request.getName());
        return entityMapper.toDto(newEntity);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEntity(@PathVariable @Min(1) int id) {
        if (entityRepository.findById(id).isEmpty()) {
            throw new NotFoundException("Entity not found with id: " + id);
        }
        if (!entityRepository.deleteById(id)) {
            throw new ServiceException("Failed to delete entity with id: " + id);
        }
    }

    @PatchMapping("/{id}/name")
    public void updateEntityName(@PathVariable @Min(1) int id, @Valid @RequestBody UpdateEntityNameRequest request) {
        String name = request.getName();
        if (entityRepository.findById(id).isEmpty()) {
            throw new NotFoundException("Entity not found with id: " + id);
        }
        if (!entityRepository.updateName(id, name)) {
            throw new ServiceException("Failed to update name for entity " + id);
        }
    }
}
