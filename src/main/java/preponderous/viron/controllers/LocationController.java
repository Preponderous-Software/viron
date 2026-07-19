package preponderous.viron.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import preponderous.viron.dto.LocationDto;
import preponderous.viron.exceptions.ConflictException;
import preponderous.viron.exceptions.InvalidRequestException;
import preponderous.viron.exceptions.NotFoundException;
import preponderous.viron.exceptions.ServiceException;
import preponderous.viron.mappers.LocationMapper;
import preponderous.viron.models.Location;
import preponderous.viron.repositories.LocationRepository;

import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/locations")
@Slf4j
@RequiredArgsConstructor
@Validated
public class LocationController {
    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;

    @GetMapping
    public List<LocationDto> getAllLocations() {
        List<Location> locations = locationRepository.findAll();
        return locationMapper.toDtoList(locations);
    }

    @GetMapping("/{id}")
    public LocationDto getLocationById(@PathVariable @Min(1) int id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Location not found with id: " + id));
        return locationMapper.toDto(location);
    }

    @GetMapping("/environment/{environmentId}")
    public List<LocationDto> getLocationsInEnvironment(@PathVariable @Min(1) int environmentId) {
        List<Location> locations = locationRepository.findByEnvironmentId(environmentId);
        return locationMapper.toDtoList(locations);
    }

    @GetMapping("/grid/{gridId}")
    public List<LocationDto> getLocationsInGrid(@PathVariable @Min(1) int gridId) {
        List<Location> locations = locationRepository.findByGridId(gridId);
        return locationMapper.toDtoList(locations);
    }

    @GetMapping("/grid/{gridId}/unoccupied")
    public List<LocationDto> getUnoccupiedLocationsInGrid(@PathVariable @Min(1) int gridId) {
        List<Location> locations = locationRepository.findUnoccupiedByGridId(gridId);
        return locationMapper.toDtoList(locations);
    }

    @GetMapping("/entity/{entityId}")
    public LocationDto getLocationOfEntity(@PathVariable @Min(1) int entityId) {
        Location location = locationRepository.findByEntityId(entityId)
                .orElseThrow(() -> new NotFoundException("Location not found for entity: " + entityId));
        return locationMapper.toDto(location);
    }

    @PutMapping("/{locationId}/entity/{entityId}")
    public void addEntityToLocation(@PathVariable("entityId") @Min(1) int entityId, @PathVariable("locationId") @Min(1) int locationId) {
        if (locationRepository.findById(locationId).isEmpty()) {
            throw new NotFoundException("Location not found with id: " + locationId);
        }
        if (!locationRepository.addEntityToLocation(entityId, locationId)) {
            throw new ServiceException("Failed to add entity " + entityId + " to location " + locationId);
        }
    }

    @DeleteMapping("/{locationId}/entity/{entityId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeEntityFromLocation(@PathVariable("entityId") @Min(1) int entityId, @PathVariable("locationId") @Min(1) int locationId) {
        if (locationRepository.findById(locationId).isEmpty()) {
            throw new NotFoundException("Location not found with id: " + locationId);
        }
        if (!locationRepository.removeEntityFromLocation(entityId, locationId)) {
            throw new ServiceException("Failed to remove entity " + entityId + " from location " + locationId);
        }
    }

    @DeleteMapping("/entity/{entityId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeEntityFromCurrentLocation(@PathVariable @Min(1) int entityId) {
        if (locationRepository.findByEntityId(entityId).isEmpty()) {
            throw new NotFoundException("Location not found for entity: " + entityId);
        }
        if (!locationRepository.removeEntityFromCurrentLocation(entityId)) {
            throw new ServiceException("Failed to remove entity " + entityId + " from current location");
        }
    }

    @GetMapping("/{locationId}/entities")
    public List<Integer> getEntityIdsAtLocation(@PathVariable @Min(1) int locationId) {
        if (locationRepository.findById(locationId).isEmpty()) {
            throw new NotFoundException("Location not found with id: " + locationId);
        }
        return locationRepository.getEntityIdsAtLocation(locationId);
    }

    @GetMapping("/{locationId}/occupied")
    public boolean isLocationOccupied(@PathVariable @Min(1) int locationId) {
        if (locationRepository.findById(locationId).isEmpty()) {
            throw new NotFoundException("Location not found with id: " + locationId);
        }
        return !locationRepository.getEntityIdsAtLocation(locationId).isEmpty();
    }

    @GetMapping("/{locationId}/neighbors")
    public List<LocationDto> getNeighbors(@PathVariable @Min(1) int locationId) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new NotFoundException("Location not found with id: " + locationId));
        int gridId = locationRepository.getGridIdOfLocation(locationId)
                .orElseThrow(() -> new NotFoundException("Location " + locationId + " is not in any grid"));
        List<Location> neighbors = locationRepository.findByGridId(gridId).stream()
                .filter(candidate -> candidate.getLocationId() != locationId)
                .filter(candidate -> isAdjacent(location, candidate))
                .collect(Collectors.toList());
        return locationMapper.toDtoList(neighbors);
    }

    /**
     * Moves an entity from its current location to {@code locationId}, validating that
     * the entity is placed, the target exists and is in the same grid, is adjacent to the
     * entity's current location, and is not already occupied (collision). The transition
     * itself is a single atomic update.
     */
    @PutMapping("/{locationId}/entity/{entityId}/move")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void moveEntityToLocation(@PathVariable("entityId") @Min(1) int entityId,
                                     @PathVariable("locationId") @Min(1) int locationId) {
        Location current = locationRepository.findByEntityId(entityId)
                .orElseThrow(() -> new NotFoundException("Entity " + entityId + " is not placed at any location"));
        Location target = locationRepository.findById(locationId)
                .orElseThrow(() -> new NotFoundException("Location not found with id: " + locationId));
        Optional<Integer> currentGrid = locationRepository.getGridIdOfLocation(current.getLocationId());
        Optional<Integer> targetGrid = locationRepository.getGridIdOfLocation(locationId);
        if (currentGrid.isEmpty() || targetGrid.isEmpty() || !currentGrid.get().equals(targetGrid.get())) {
            throw new InvalidRequestException(
                    "Target location " + locationId + " is not in the same grid as entity " + entityId);
        }
        if (!isAdjacent(current, target)) {
            throw new InvalidRequestException(
                    "Target location " + locationId + " is not adjacent to entity " + entityId + "'s current location");
        }
        if (!locationRepository.getEntityIdsAtLocation(locationId).isEmpty()) {
            throw new ConflictException("Target location " + locationId + " is already occupied");
        }
        if (!locationRepository.moveEntityToLocation(entityId, locationId)) {
            throw new ServiceException("Failed to move entity " + entityId + " to location " + locationId);
        }
    }

    /** True if {@code a} and {@code b} are within one grid cell of each other (Chebyshev distance 1), including diagonals. */
    private static boolean isAdjacent(Location a, Location b) {
        int dx = Math.abs(a.getX() - b.getX());
        int dy = Math.abs(a.getY() - b.getY());
        return Math.max(dx, dy) == 1;
    }
}
