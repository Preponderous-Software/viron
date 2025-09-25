package preponderous.viron.factories;

import java.sql.ResultSet;
import java.sql.SQLException;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        String creationDate = new java.util.Date().toString();
        String query = "INSERT INTO viron.entity (entity_id, name, creation_date) VALUES (" + id + ", '" + name + "', '" + creationDate + "')";
        boolean success = dbInteractions.update(query);
        if (success) {
            log.info("Successfully created entity with name: {} and id: {} and creation date: {}", name, id, creationDate);
            return new Entity(id, name, creationDate);
        }
        throw new EntityCreationException("Error creating entity with name: " + name);
    }

    private int getNextEntityId() {
        ResultSet rs = dbInteractions.query("SELECT nextval('viron.entity_id_seq')");
        try {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            log.error("Error getting next entity id: {}", e.getMessage());
        }
        return -1;
    }
}
