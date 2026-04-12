package preponderous.viron.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import preponderous.viron.dto.LocationDto;
import preponderous.viron.exceptions.NotFoundException;
import preponderous.viron.exceptions.ServiceException;
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

    @GetMapping("/entity/{entityId}")
    public LocationDto getLocationOfEntity(@PathVariable @Min(1) int entityId) {
        Location location = locationRepository.findByEntityId(entityId)
                .orElseThrow(() -> new NotFoundException("Location not found for entity: " + entityId));
        return locationMapper.toDto(location);
    }

    @PutMapping("/{locationId}/entity/{entityId}")
    public void addEntityToLocation(@PathVariable @Min(1) int entityId, @PathVariable @Min(1) int locationId) {
        if (locationRepository.findById(locationId).isEmpty()) {
            throw new NotFoundException("Location not found with id: " + locationId);
        }
        if (!locationRepository.addEntityToLocation(entityId, locationId)) {
            throw new ServiceException("Failed to add entity " + entityId + " to location " + locationId);
        }
    }

    @DeleteMapping("/{locationId}/entity/{entityId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeEntityFromLocation(@PathVariable @Min(1) int entityId, @PathVariable @Min(1) int locationId) {
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
            throw new NotFoundException("Entity not found with id: " + entityId);
        }
        if (!locationRepository.removeEntityFromCurrentLocation(entityId)) {
            throw new ServiceException("Failed to remove entity " + entityId + " from current location");
        }
    }
}
