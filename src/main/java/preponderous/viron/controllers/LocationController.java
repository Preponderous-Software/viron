package preponderous.viron.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import preponderous.viron.dto.LocationDto;
import preponderous.viron.exceptions.NotFoundException;
import preponderous.viron.mappers.LocationMapper;
import preponderous.viron.models.Location;
import preponderous.viron.repositories.LocationRepository;

import jakarta.validation.constraints.Min;
import java.util.List;

@RestController
@RequestMapping("/api/v1/locations")
@Slf4j
@RequiredArgsConstructor
@Validated
public class LocationController {
    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;

    @GetMapping
    public ResponseEntity<List<LocationDto>> getAllLocations() {
        List<Location> locations = locationRepository.findAll();
        return ResponseEntity.ok(locationMapper.toDtoList(locations));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationDto> getLocationById(@PathVariable @Min(1) int id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Location not found with id: " + id));
        return ResponseEntity.ok(locationMapper.toDto(location));
    }

    @GetMapping("/environment/{environmentId}")
    public ResponseEntity<List<LocationDto>> getLocationsInEnvironment(@PathVariable @Min(1) int environmentId) {
        List<Location> locations = locationRepository.findByEnvironmentId(environmentId);
        return ResponseEntity.ok(locationMapper.toDtoList(locations));
    }

    @GetMapping("/grid/{gridId}")
    public ResponseEntity<List<LocationDto>> getLocationsInGrid(@PathVariable @Min(1) int gridId) {
        List<Location> locations = locationRepository.findByGridId(gridId);
        return ResponseEntity.ok(locationMapper.toDtoList(locations));
    }

    @GetMapping("/entity/{entityId}")
    public ResponseEntity<LocationDto> getLocationOfEntity(@PathVariable @Min(1) int entityId) {
        Location location = locationRepository.findByEntityId(entityId)
                .orElseThrow(() -> new NotFoundException("Location not found for entity: " + entityId));
        return ResponseEntity.ok(locationMapper.toDto(location));
    }

    @PutMapping("/{locationId}/entity/{entityId}")
    public ResponseEntity<Void> addEntityToLocation(@PathVariable @Min(1) int entityId, @PathVariable @Min(1) int locationId) {
        if (!locationRepository.addEntityToLocation(entityId, locationId)) {
            throw new NotFoundException("Location or entity not found");
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{locationId}/entity/{entityId}")
    public ResponseEntity<Void> removeEntityFromLocation(@PathVariable @Min(1) int entityId, @PathVariable @Min(1) int locationId) {
        if (!locationRepository.removeEntityFromLocation(entityId, locationId)) {
            throw new NotFoundException("Location or entity not found");
        }
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/entity/{entityId}")
    public ResponseEntity<Void> removeEntityFromCurrentLocation(@PathVariable @Min(1) int entityId) {
        if (!locationRepository.removeEntityFromCurrentLocation(entityId)) {
            throw new NotFoundException("Entity not found with id: " + entityId);
        }
        return ResponseEntity.noContent().build();
    }
}
