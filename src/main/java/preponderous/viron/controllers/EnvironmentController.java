
// Copyright (c) 2024 Preponderous Software
// MIT License

package preponderous.viron.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import preponderous.viron.dto.EnvironmentDto;
import preponderous.viron.exceptions.NotFoundException;
import preponderous.viron.exceptions.ServiceException;
import preponderous.viron.factories.EnvironmentFactory;
import preponderous.viron.mappers.EnvironmentMapper;
import preponderous.viron.models.Environment;
import preponderous.viron.repositories.EnvironmentRepository;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

@RestController
@RequestMapping("/api/v1/environments")
@Slf4j
@RequiredArgsConstructor
@Validated
public class EnvironmentController {
    private final EnvironmentRepository environmentRepository;
    private final EnvironmentFactory environmentFactory;
    private final EnvironmentMapper environmentMapper;

    @GetMapping
    public List<EnvironmentDto> getAllEnvironments() {
        List<Environment> environments = environmentRepository.findAll();
        return environmentMapper.toDtoList(environments);
    }

    @GetMapping("/{id}")
    public EnvironmentDto getEnvironmentById(@PathVariable @Min(1) int id) {
        Environment environment = environmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Environment not found with id: " + id));
        return environmentMapper.toDto(environment);
    }

    @GetMapping("/name/{name}")
    public EnvironmentDto getEnvironmentByName(@PathVariable @NotBlank String name) {
        Environment environment = environmentRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException("Environment not found with name: " + name));
        return environmentMapper.toDto(environment);
    }

    @GetMapping("/entity/{entityId}")
    public EnvironmentDto getEnvironmentOfEntity(@PathVariable @Min(1) int entityId) {
        Environment environment = environmentRepository.findByEntityId(entityId)
                .orElseThrow(() -> new NotFoundException("Environment not found for entity: " + entityId));
        return environmentMapper.toDto(environment);
    }

    @PostMapping("/{name}/{numGrids}/{gridSize}")
    @ResponseStatus(HttpStatus.CREATED)
    public EnvironmentDto createEnvironment(
            @PathVariable @NotBlank String name,
            @PathVariable @Min(1) int numGrids,
            @PathVariable @Min(1) int gridSize) {
        Environment newEnvironment = environmentFactory.createEnvironment(name, numGrids, gridSize);
        return environmentMapper.toDto(newEnvironment);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEnvironment(@PathVariable @Min(1) int id) {
        if (environmentRepository.findById(id).isEmpty()) {
            throw new NotFoundException("Environment not found with id: " + id);
        }

        log.info("Attempting to delete environment with id {}", id);

        List<Integer> entityIds = environmentRepository.findEntityIdsByEnvironmentId(id);
        List<Integer> locationIds = environmentRepository.findLocationIdsByEnvironmentId(id);
        List<Integer> gridIds = environmentRepository.findGridIdsByEnvironmentId(id);

        // Delete associations
        for (int entityId : entityIds) {
            if (!environmentRepository.deleteEntityLocation(entityId)) {
                throw new ServiceException("Error deleting entity_location with entity id " + entityId);
            }
        }

        for (int locationId : locationIds) {
            if (!environmentRepository.deleteLocationGrid(locationId)) {
                throw new ServiceException("Error deleting location_grid with location id " + locationId);
            }
        }

        for (int gridId : gridIds) {
            if (!environmentRepository.deleteGridEnvironment(gridId)) {
                throw new ServiceException("Error deleting grid_environment with grid id " + gridId);
            }
        }

        // Delete entities
        for (int entityId : entityIds) {
            if (!environmentRepository.deleteEntity(entityId)) {
                throw new ServiceException("Error deleting entity with id " + entityId);
            }
        }
        if (!entityIds.isEmpty()) {
            log.info("Entities deleted: {}", entityIds);
        }

        // Delete locations
        for (int locationId : locationIds) {
            if (!environmentRepository.deleteLocation(locationId)) {
                throw new ServiceException("Error deleting location with id " + locationId);
            }
        }
        if (!locationIds.isEmpty()) {
            log.info("Locations deleted: {}", locationIds);
        }

        // Delete grids
        for (int gridId : gridIds) {
            if (!environmentRepository.deleteGrid(gridId)) {
                throw new ServiceException("Error deleting grid with id " + gridId);
            }
        }
        if (!gridIds.isEmpty()) {
            log.info("Grids deleted: {}", gridIds);
        }

        // Delete environment
        if (!environmentRepository.deleteById(id)) {
            throw new ServiceException("Error deleting environment with id " + id);
        }

        log.info("Environment with id {} deleted", id);
    }

    @PatchMapping("/{id}/name/{name}")
    public void updateEnvironmentName(@PathVariable @Min(1) int id, @PathVariable @NotBlank String name) {
        if (environmentRepository.findById(id).isEmpty()) {
            throw new NotFoundException("Environment not found with id: " + id);
        }

        if (!environmentRepository.updateName(id, name)) {
            throw new ServiceException("Failed to update name for environment " + id);
        }

        log.info("Updated name for environment {} to '{}'", id, name);
    }
}
