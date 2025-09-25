package preponderous.viron.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import preponderous.viron.models.Grid;
import preponderous.viron.repositories.GridRepository;

import java.util.List;

@RestController
@RequestMapping("/api/v1/grids")
@Slf4j
@RequiredArgsConstructor
public class GridController {
    private final GridRepository gridRepository;

    @GetMapping
    public ResponseEntity<List<Grid>> getAllGrids() {
        try {
            return ResponseEntity.ok(gridRepository.findAll());
        } catch (Exception e) {
            log.error("Error fetching all grids: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Grid> getGridById(@PathVariable int id) {
        try {
            return gridRepository.findById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error fetching grid by id {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/environment/{environmentId}")
    public ResponseEntity<List<Grid>> getGridsInEnvironment(@PathVariable int environmentId) {
        try {
            return ResponseEntity.ok(gridRepository.findByEnvironmentId(environmentId));
        } catch (Exception e) {
            log.error("Error fetching grids in environment {}: {}", environmentId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/entity/{entityId}")
    public ResponseEntity<Grid> getGridOfEntity(@PathVariable int entityId) {
        try {
            return gridRepository.findByEntityId(entityId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error fetching grid for entity {}: {}", entityId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}