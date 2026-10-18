package preponderous.viron.repositories;

import preponderous.viron.models.Location;
import preponderous.viron.models.Placement;
import java.util.List;
import java.util.Optional;

public interface LocationRepository {
    List<Location> findAll();
    Optional<Location> findById(int id);
    List<Location> findByEnvironmentId(int environmentId);
    List<Location> findByGridId(int gridId);
    Optional<Location> findByEntityId(int entityId);
    /**
     * Places an entity at a location.
     *
     * @throws org.springframework.dao.DuplicateKeyException if the entity is already placed —
     *         the database, not the caller's prior read, is what decides this, so a request that
     *         lost a race against a concurrent placement lands here rather than returning
     *         {@code false}
     */
    boolean addEntityToLocation(int entityId, int locationId);
    boolean removeEntityFromLocation(int entityId, int locationId);
    boolean removeEntityFromCurrentLocation(int entityId);

    /** Entity ids currently placed at the given location (occupancy / collision query). */
    List<Integer> getEntityIdsAtLocation(int locationId);

    /** Locations in the given grid that currently have no entity placed at them. */
    List<Location> findUnoccupiedByGridId(int gridId);

    /**
     * Every placed entity with the environment, grid, and location it stands in, ordered by entity
     * id. Read in one statement, so the result describes a single state of the world rather than
     * one assembled from reads taken at different moments (#234). Unplaced entities are absent.
     */
    List<Placement> findAllPlacements();

    /** As {@link #findAllPlacements()}, limited to entities placed in the given environment. */
    List<Placement> findPlacementsByEnvironmentId(int environmentId);

    /** The grid a location belongs to, if any. */
    Optional<Integer> getGridIdOfLocation(int locationId);

    /**
     * Takes an exclusive row lock on an entity's placement, held until the surrounding transaction
     * ends.
     *
     * <p>Callers that lock both a placement and a location take this lock first, because that is
     * the order the environment cascade delete acquires the same two — it clears
     * {@code entity_location} before deleting the locations — and two paths that take the same
     * locks in opposite orders can wait on each other in a cycle.
     *
     * <p>Only meaningful inside a transaction, for the reason given on {@link #lockLocation(int)}.
     *
     * @return {@code true} if the entity is placed and its placement is now locked, {@code false}
     *         if it is not placed anywhere
     * @throws org.springframework.dao.CannotAcquireLockException if the lock could not be taken —
     *         a row that exists but is unavailable is not the same as a row that is absent, so
     *         this is reported rather than returned as {@code false}
     */
    boolean lockPlacementOfEntity(int entityId);

    /**
     * Takes an exclusive row lock on a location, held until the surrounding transaction ends.
     *
     * <p>The lock is on the location itself rather than on whatever happens to occupy it, because
     * a location that is empty has no occupancy rows to lock and emptiness is exactly the state a
     * collision check depends on (#203). Callers that read occupancy and then write on the
     * strength of that read take this lock first, so any other caller doing the same against the
     * same location waits rather than reading the same stale emptiness.
     *
     * <p>Only meaningful inside a transaction: without one the lock is released as soon as the
     * statement ends, which is before the caller can act on what it read.
     *
     * @return {@code true} if the location exists and is now locked, {@code false} if there is no
     *         such location
     * @throws org.springframework.dao.CannotAcquireLockException if the lock could not be taken,
     *         for the reason given on {@link #lockPlacementOfEntity(int)}
     */
    boolean lockLocation(int locationId);

    /** Atomically moves an entity's current placement to the target location. */
    boolean moveEntityToLocation(int entityId, int targetLocationId);
}