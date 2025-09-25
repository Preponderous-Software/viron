package preponderous.viron.repositories;

import preponderous.viron.models.Grid;
import java.util.List;
import java.util.Optional;

public interface GridRepository {
    List<Grid> findAll();
    Optional<Grid> findById(int id);
    List<Grid> findByEnvironmentId(int environmentId);
    Optional<Grid> findByEntityId(int entityId);
}