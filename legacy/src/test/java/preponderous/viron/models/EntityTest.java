// Copyright (c) 2024 Preponderous Software
// MIT License

package preponderous.viron.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EntityTest {
    
    @Test
    void testInitialization() {
        Entity entity = new Entity(0, "Test", "10/12/2024");
    }

    @Test
    void testGetId() {
        Entity entity = new Entity(0, "Test", "10/12/2024");
        entity.setEntityId(0);
        assertEquals(0, entity.getEntityId());
    }

    @Test
    void testGetName() {
        Entity entity = new Entity(0, "Test", "10/12/2024");
        entity.setName("Test");
        assertEquals("Test", entity.getName());
    }

    @Test
    void testGetCreationDate() {
        Entity entity = new Entity(0, "Test", "10/12/2024");
        entity.setCreationDate("10/12/2024");
        assertEquals("10/12/2024", entity.getCreationDate());
    }

    @Test
    void testToString() {
        Entity entity = new Entity(0, "Test", "10/12/2024");
        assertEquals("Entity{entityId=0, name='Test', creationDate='10/12/2024'}", entity.toString());
    }
}
