package preponderous.viron.repositories;

import preponderous.viron.models.Environment;
import java.util.List;
import java.util.Optional;

public interface EnvironmentRepository {
    List<Environment> findAll();
    Optional<Environment> findById(int id);
    Optional<Environment> findByName(String name);
    Optional<Environment> findByEntityId(int entityId);
    Environment save(Environment environment);
    boolean deleteById(int id);
    boolean updateName(int id, String name);
    List<Integer> findEntityIdsByEnvironmentId(int environmentId);
    List<Integer> findLocationIdsByEnvironmentId(int environmentId);
    List<Integer> findGridIdsByEnvironmentId(int environmentId);
    boolean deleteEntityLocation(int entityId);
    boolean deleteLocationGrid(int locationId);
    boolean deleteGridEnvironment(int gridId);
    boolean deleteEntity(int entityId);
    boolean deleteLocation(int locationId);
    boolean deleteGrid(int gridId);
}