package preponderous.viron.repositories;

import preponderous.viron.models.Entity;
import java.util.List;
import java.util.Optional;

public interface EntityRepository {
    List<Entity> findAll();
    Optional<Entity> findById(int id);
    List<Entity> findByEnvironmentId(int environmentId);
    List<Entity> findByGridId(int gridId);
    List<Entity> findByLocationId(int locationId);
    List<Entity> findEntitiesNotInAnyLocation();
    Entity save(Entity entity);
    boolean deleteById(int id);
    boolean updateName(int id, String name);
}