package preponderous.viron.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import preponderous.viron.models.Location;
import preponderous.viron.repositories.LocationRepository;

import java.util.List;

@RestController
@RequestMapping("/api/v1/locations")
@Slf4j
@RequiredArgsConstructor
public class LocationController {
    private final LocationRepository locationRepository;

    @GetMapping
    public ResponseEntity<List<Location>> getAllLocations() {
        try {
            return ResponseEntity.ok(locationRepository.findAll());
        } catch (Exception e) {
            log.error("Error fetching all locations: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Location> getLocationById(@PathVariable int id) {
        try {
            return locationRepository.findById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error fetching location by id {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/environment/{environmentId}")
    public ResponseEntity<List<Location>> getLocationsInEnvironment(@PathVariable int environmentId) {
        try {
            return ResponseEntity.ok(locationRepository.findByEnvironmentId(environmentId));
        } catch (Exception e) {
            log.error("Error fetching locations in environment {}: {}", environmentId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/grid/{gridId}")
    public ResponseEntity<List<Location>> getLocationsInGrid(@PathVariable int gridId) {
        try {
            return ResponseEntity.ok(locationRepository.findByGridId(gridId));
        } catch (Exception e) {
            log.error("Error fetching locations in grid {}: {}", gridId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/entity/{entityId}")
    public ResponseEntity<Location> getLocationOfEntity(@PathVariable int entityId) {
        try {
            return locationRepository.findByEntityId(entityId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error fetching location of entity {}: {}", entityId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{locationId}/entity/{entityId}")
    public ResponseEntity<Void> addEntityToLocation(@PathVariable int entityId, @PathVariable int locationId) {
        try {
            return locationRepository.addEntityToLocation(entityId, locationId)
                    ? ResponseEntity.ok().build()
                    : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error adding entity {} to location {}: {}", entityId, locationId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{locationId}/entity/{entityId}")
    public ResponseEntity<Void> removeEntityFromLocation(@PathVariable int entityId, @PathVariable int locationId) {
        try {
            return locationRepository.removeEntityFromLocation(entityId, locationId)
                    ? ResponseEntity.ok().build()
                    : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error removing entity {} from location {}: {}", entityId, locationId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/entity/{entityId}")
    public ResponseEntity<Void> removeEntityFromCurrentLocation(@PathVariable int entityId) {
        try {
            return locationRepository.removeEntityFromCurrentLocation(entityId)
                    ? ResponseEntity.ok().build()
                    : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error removing entity {} from current location: {}", entityId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}