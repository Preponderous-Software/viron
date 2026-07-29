package preponderous.viron.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import preponderous.viron.dto.GridDto;
import preponderous.viron.dto.UpdateGridNameRequest;
import preponderous.viron.exceptions.NotFoundException;
import preponderous.viron.exceptions.ServiceException;
import preponderous.viron.mappers.GridMapper;
import preponderous.viron.models.Grid;
import preponderous.viron.repositories.GridRepository;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.List;

@RestController
@RequestMapping("/api/v1/grids")
@Slf4j
@RequiredArgsConstructor
@Validated
public class GridController {
    private final GridRepository gridRepository;
    private final GridMapper gridMapper;

    @GetMapping
    public List<GridDto> getAllGrids() {
        List<Grid> grids = gridRepository.findAll();
        return gridMapper.toDtoList(grids);
    }

    @GetMapping("/{id}")
    public GridDto getGridById(@PathVariable @Min(1) int id) {
        Grid grid = gridRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Grid not found with id: " + id));
        return gridMapper.toDto(grid);
    }

    @GetMapping("/environment/{environmentId}")
    public List<GridDto> getGridsInEnvironment(@PathVariable @Min(1) int environmentId) {
        List<Grid> grids = gridRepository.findByEnvironmentId(environmentId);
        return gridMapper.toDtoList(grids);
    }

    @GetMapping("/entity/{entityId}")
    public GridDto getGridOfEntity(@PathVariable @Min(1) int entityId) {
        Grid grid = gridRepository.findByEntityId(entityId)
                .orElseThrow(() -> new NotFoundException("Grid not found for entity: " + entityId));
        return gridMapper.toDto(grid);
    }

    @PatchMapping("/{id}/name")
    public void updateGridName(@PathVariable @Min(1) int id, @Valid @RequestBody UpdateGridNameRequest request) {
        String name = request.getName();
        if (gridRepository.findById(id).isEmpty()) {
            throw new NotFoundException("Grid not found with id: " + id);
        }

        if (!gridRepository.updateName(id, name)) {
            throw new ServiceException("Failed to update name for grid " + id);
        }

        log.info("Updated name for grid {} to '{}'", id, name);
    }
}
