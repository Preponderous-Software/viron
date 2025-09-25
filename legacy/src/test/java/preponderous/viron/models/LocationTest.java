// Copyright (c) 2024 Preponderous Software
// MIT License

package preponderous.viron.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class LocationTest {
    
    @Test
    void testInitialization() {
        Location location = new Location(0, 0, 0);
    }

    @Test
    void testGetId() {
        Location location = new Location(0, 0, 0);
        location.setLocationId(0);
        assertEquals(0, location.getLocationId());
    }

    @Test
    void testGetX() {
        Location location = new Location(0, 0, 0);
        location.setX(0);
        assertEquals(0, location.getX());
    }

    @Test
    void testGetY() {
        Location location = new Location(0, 0, 0);
        location.setY(0);
        assertEquals(0, location.getY());
    }

    @Test
    void testToString() {
        Location location = new Location(0, 0, 0);
        assertEquals("Location{locationId=0, x=0, y=0}", location.toString());
    }
}
