package preponderous.viron.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import preponderous.viron.dto.LocationDto;
import preponderous.viron.dto.PlacementDto;
import preponderous.viron.exceptions.ConflictException;
import preponderous.viron.exceptions.InvalidRequestException;
import preponderous.viron.exceptions.NotFoundException;
import preponderous.viron.exceptions.ServiceException;
import preponderous.viron.mappers.LocationMapper;
import preponderous.viron.mappers.PlacementMapper;
import preponderous.viron.models.Location;
import preponderous.viron.models.Placement;
import preponderous.viron.repositories.EntityRepository;
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
    private final EntityRepository entityRepository;
    private final LocationMapper locationMapper;
    private final PlacementMapper placementMapper;

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

    /**
     * Where every placed entity stands, optionally limited to one environment, answered from a
     * single query so that the result is one consistent snapshot (#234). An environment with no
     * placed entities — including one that does not exist, as with
     * {@code GET /api/v1/entities/environment/{environmentId}} — is answered with an empty list.
     */
    @GetMapping("/placements")
    public List<PlacementDto> getPlacements(@RequestParam(required = false) @Min(1) Integer environmentId) {
        List<Placement> placements = environmentId == null
                ? locationRepository.findAllPlacements()
                : locationRepository.findPlacementsByEnvironmentId(environmentId);
        return placementMapper.toDtoList(placements);
    }

    @GetMapping("/entity/{entityId}")
    public LocationDto getLocationOfEntity(@PathVariable @Min(1) int entityId) {
        Location location = locationRepository.findByEntityId(entityId)
                .orElseThrow(() -> new NotFoundException("Location not found for entity: " + entityId));
        return locationMapper.toDto(location);
    }

    /**
     * Places an unplaced entity at {@code locationId}. An entity occupies at most one location,
     * so a request for an entity that is already placed elsewhere is a conflict; a request for an
     * entity already at the target is a no-op, keeping the {@code PUT} idempotent.
     *
     * <p>The placement check below cannot decide the outcome on its own: two concurrent requests
     * for the same unplaced entity both read no placement and both go on to insert. The primary
     * key on {@code viron.entity_location.entity_id} is what settles which of them wins, so the
     * loser is recognised by the {@link DuplicateKeyException} its insert raises and answered
     * from the placement the winner committed — the same answer the check above would have given
     * had the two requests arrived in sequence (#200).
     */
    @PutMapping("/{locationId}/entity/{entityId}")
    public void addEntityToLocation(@PathVariable("entityId") @Min(1) int entityId, @PathVariable("locationId") @Min(1) int locationId) {
        if (locationRepository.findById(locationId).isEmpty()) {
            throw new NotFoundException("Location not found with id: " + locationId);
        }
        if (entityRepository.findById(entityId).isEmpty()) {
            throw new NotFoundException("Entity not found with id: " + entityId);
        }
        Optional<Location> currentLocation = locationRepository.findByEntityId(entityId);
        if (currentLocation.isPresent()) {
            reportPlacement(entityId, locationId, currentLocation.get());
            return;
        }
        try {
            if (!locationRepository.addEntityToLocation(entityId, locationId)) {
                throw new ServiceException("Failed to add entity " + entityId + " to location " + locationId);
            }
        } catch (DuplicateKeyException e) {
            log.info("Entity {} was placed concurrently while adding it to location {}", entityId, locationId);
            Optional<Location> winner = locationRepository.findByEntityId(entityId);
            if (winner.isEmpty()) {
                // The winning placement was removed again before it could be read back, so there
                // is no location to name. The request still failed on a conflict, not a fault.
                throw new ConflictException("Entity " + entityId
                        + " was placed by a concurrent request and could not be added to location " + locationId);
            }
            reportPlacement(entityId, locationId, winner.get());
        }
    }

    /**
     * Answers a placement request for an entity that is already placed: silence when it is
     * already where the request wanted it, a conflict naming its actual location otherwise.
     */
    private static void reportPlacement(int entityId, int requestedLocationId, Location placement) {
        if (placement.getLocationId() == requestedLocationId) {
            return;
        }
        throw new ConflictException("Entity " + entityId + " is already placed at location "
                + placement.getLocationId());
    }

    /**
     * Removes the entity placed at {@code locationId}. An entity that is not placed there — whether
     * it is placed elsewhere or not placed at all — is a request naming something that does not
     * exist, answered as such rather than as a server fault (#210); the sibling
     * {@link #removeEntityFromCurrentLocation(int)} already answers the equivalent case the same way.
     *
     * <p>The placement is locked before it is read, and the read and the delete run in one
     * transaction, so that two removals of the same placement at once are resolved in sequence.
     * Without the lock the second of them would find the row already gone by the time it wrote,
     * and be answered with the server fault this endpoint has just stopped reporting.
     */
    @DeleteMapping("/{locationId}/entity/{entityId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void removeEntityFromLocation(@PathVariable("entityId") @Min(1) int entityId, @PathVariable("locationId") @Min(1) int locationId) {
        if (locationRepository.findById(locationId).isEmpty()) {
            throw new NotFoundException("Location not found with id: " + locationId);
        }
        if (!locationRepository.lockPlacementOfEntity(entityId)) {
            throw entityNotAtLocation(entityId, locationId);
        }
        Optional<Location> placement = locationRepository.findByEntityId(entityId);
        if (placement.isEmpty() || placement.get().getLocationId() != locationId) {
            throw entityNotAtLocation(entityId, locationId);
        }
        if (!locationRepository.removeEntityFromLocation(entityId, locationId)) {
            throw new ServiceException("Failed to remove entity " + entityId + " from location " + locationId);
        }
    }

    /**
     * Removes an entity from wherever it is placed.
     *
     * <p>The placement is locked rather than merely read, for the reason given on
     * {@link #removeEntityFromLocation(int, int)}: an unguarded read followed by a delete lets two
     * removals of the same placement both pass the check, and the one that writes second matches
     * no rows and is answered with a server fault for having lost a race. Taking the lock also
     * makes the read redundant, since a statement that locks no row is exactly the unplaced entity
     * the 404 below reports.
     */
    @DeleteMapping("/entity/{entityId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void removeEntityFromCurrentLocation(@PathVariable @Min(1) int entityId) {
        if (!locationRepository.lockPlacementOfEntity(entityId)) {
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
     *
     * <p>The collision check is not something the read alone can decide: two moves into the same
     * empty location would both read it empty and both write, leaving the target holding two
     * entities that the 409 above claims to prevent (#203). Nothing in the schema settles this the
     * way the primary key settles {@link #addEntityToLocation(int, int)} — a location is permitted
     * to hold several entities, and {@code addEntityToLocation} places one without consulting
     * occupancy at all, so "at most one entity per location" is this endpoint's rule rather than
     * an invariant of the data. The target's row is therefore locked before its occupancy is read,
     * and the lock is held to the end of the transaction, so a second move into the same location
     * waits and then reads the placement the first one committed.
     *
     * <p>The entity's own placement is locked first, before anything is read, for two reasons: it
     * keeps the position the checks below are made against from moving underneath them, and it is
     * the order {@code deleteEnvironment} takes the same two locks in, so a move and a cascade
     * delete cannot end up waiting on each other in a cycle.
     */
    @PutMapping("/{locationId}/entity/{entityId}/move")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void moveEntityToLocation(@PathVariable("entityId") @Min(1) int entityId,
                                     @PathVariable("locationId") @Min(1) int locationId) {
        if (!locationRepository.lockPlacementOfEntity(entityId)) {
            throw entityNotPlaced(entityId);
        }
        Location current = locationRepository.findByEntityId(entityId)
                .orElseThrow(() -> entityNotPlaced(entityId));
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
        if (!locationRepository.lockLocation(locationId)) {
            throw new NotFoundException("Location not found with id: " + locationId);
        }
        if (!locationRepository.getEntityIdsAtLocation(locationId).isEmpty()) {
            throw new ConflictException("Target location " + locationId + " is already occupied");
        }
        if (!locationRepository.moveEntityToLocation(entityId, locationId)) {
            throw new ServiceException("Failed to move entity " + entityId + " to location " + locationId);
        }
    }

    private static NotFoundException entityNotPlaced(int entityId) {
        return new NotFoundException("Entity " + entityId + " is not placed at any location");
    }

    private static NotFoundException entityNotAtLocation(int entityId, int locationId) {
        return new NotFoundException("Entity " + entityId + " is not at location " + locationId);
    }

    /** True if {@code a} and {@code b} are within one grid cell of each other (Chebyshev distance 1), including diagonals. */
    private static boolean isAdjacent(Location a, Location b) {
        int dx = Math.abs(a.getX() - b.getX());
        int dy = Math.abs(a.getY() - b.getY());
        return Math.max(dx, dy) == 1;
    }
}
