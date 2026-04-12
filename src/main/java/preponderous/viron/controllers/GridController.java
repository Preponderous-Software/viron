package preponderous.viron.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import preponderous.viron.dto.GridDto;
import preponderous.viron.exceptions.NotFoundException;
import preponderous.viron.mappers.GridMapper;
import preponderous.viron.models.Grid;
import preponderous.viron.repositories.GridRepository;

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
    public ResponseEntity<List<GridDto>> getAllGrids() {
        List<Grid> grids = gridRepository.findAll();
        return ResponseEntity.ok(gridMapper.toDtoList(grids));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GridDto> getGridById(@PathVariable @Min(1) int id) {
        Grid grid = gridRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Grid not found with id: " + id));
        return ResponseEntity.ok(gridMapper.toDto(grid));
    }

    @GetMapping("/environment/{environmentId}")
    public ResponseEntity<List<GridDto>> getGridsInEnvironment(@PathVariable @Min(1) int environmentId) {
        List<Grid> grids = gridRepository.findByEnvironmentId(environmentId);
        return ResponseEntity.ok(gridMapper.toDtoList(grids));
    }

    @GetMapping("/entity/{entityId}")
    public ResponseEntity<GridDto> getGridOfEntity(@PathVariable @Min(1) int entityId) {
        Grid grid = gridRepository.findByEntityId(entityId)
                .orElseThrow(() -> new NotFoundException("Grid not found for entity: " + entityId));
        return ResponseEntity.ok(gridMapper.toDto(grid));
    }
}
