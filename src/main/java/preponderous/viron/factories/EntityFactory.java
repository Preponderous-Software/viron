package preponderous.viron.factories;

import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import preponderous.viron.database.DbInteractions;
import preponderous.viron.exceptions.EntityCreationException;
import preponderous.viron.models.Entity;

@Component
@Slf4j
public class EntityFactory {
    private final DbInteractions dbInteractions;

    @Autowired
    public EntityFactory(DbInteractions dbInteractions) {
        this.dbInteractions = dbInteractions;
    }

    public Entity createEntity(String name) throws EntityCreationException {
        log.info("Attempting to create entity with name: {}", name);
        int id = getNextEntityId();
        if (id == -1) {
            log.error("Failed to get next entity id");
            throw new EntityCreationException("Failed to get next entity id");
        }
        String creationDate = new java.util.Date().toString();
        String query = "INSERT INTO viron.entity (entity_id, name, creation_date) VALUES (?, ?, ?)";
        boolean success = dbInteractions.update(query, id, name, creationDate);
        if (success) {
            log.info("Successfully created entity with name: {} and id: {} and creation date: {}", name, id, creationDate);
            return new Entity(id, name, creationDate);
        }
        throw new EntityCreationException("Error creating entity with name: " + name);
    }

    private int getNextEntityId() {
        Optional<Integer> nextId = dbInteractions.queryOne("SELECT nextval('viron.entity_id_seq')",
                rs -> rs.getInt(1));
        return nextId.orElse(-1);
    }
}
