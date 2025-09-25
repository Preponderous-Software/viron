// Copyright (c) 2024 Preponderous Software
// MIT License

package preponderous.viron.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EnvironmentTest {

    @Test
    void testInitialization() {
        Environment environment = new Environment(0, "Test", "10/12/2024");
    }

    @Test
    void testGetId() {
        Environment environment = new Environment(0, "Test", "10/12/2024");
        environment.setEnvironmentId(0);
        assertEquals(0, environment.getEnvironmentId());
    }
    
    @Test
    void testGetName() {
        Environment environment = new Environment(0, "Test", "10/12/2024");
        environment.setName("Test");
        assertEquals("Test", environment.getName());
    }

    @Test
    void testGetCreationDate() {
        Environment environment = new Environment(0, "Test", "10/12/2024");
        environment.setCreationDate("10/12/2024");
        assertEquals("10/12/2024", environment.getCreationDate());
    }

    @Test
    void testToString() {
        Environment environment = new Environment(0, "Test", "10/12/2024");
        assertEquals("Environment{environmentId=0, name='Test', creationDate='10/12/2024'}", environment.toString());
    }
}
