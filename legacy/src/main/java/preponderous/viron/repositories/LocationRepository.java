package preponderous.viron.repositories;

import preponderous.viron.models.Location;
import java.util.List;
import java.util.Optional;

public interface LocationRepository {
    List<Location> findAll();
    Optional<Location> findById(int id);
    List<Location> findByEnvironmentId(int environmentId);
    List<Location> findByGridId(int gridId);
    Optional<Location> findByEntityId(int entityId);
    boolean addEntityToLocation(int entityId, int locationId);
    boolean removeEntityFromLocation(int entityId, int locationId);
    boolean removeEntityFromCurrentLocation(int entityId);
}