// Copyright (c) 2024 Preponderous Software
// MIT License

package preponderous.viron.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import org.junit.jupiter.api.Test;

// Every field is given a distinct value so that a constructor argument stored in the wrong field
// is observable.
public class PlacementTest {

    @Test
    void testConstructorStoresEveryField() {
        Placement placement = new Placement(1, 7, 11, 110, 5, 6);
        assertEquals(1, placement.getEntityId());
        assertEquals(7, placement.getEnvironmentId());
        assertEquals(11, placement.getGridId());
        assertEquals(110, placement.getLocationId());
        assertEquals(5, placement.getX());
        assertEquals(6, placement.getY());
    }

    @Test
    void testEquality() {
        assertEquals(new Placement(1, 7, 11, 110, 5, 6), new Placement(1, 7, 11, 110, 5, 6));
        assertNotEquals(new Placement(1, 7, 11, 110, 5, 6), new Placement(1, 7, 11, 110, 6, 5));
    }

    @Test
    void testToString() {
        Placement placement = new Placement(1, 7, 11, 110, 5, 6);
        assertEquals("Placement{entityId=1, environmentId=7, gridId=11, locationId=110, x=5, y=6}",
                placement.toString());
    }
}
