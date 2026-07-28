// Copyright (c) 2024 Preponderous Software
// MIT License

package preponderous.viron.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class GridTest {

    @Test
    void testInitialization() {
        Grid grid = new Grid(0, 10, 10, null);
    }

    @Test
    void testGetId() {
        Grid grid = new Grid(0, 10, 10, null);
        grid.setGridId(0);
        assertEquals(0, grid.getGridId());
    }

    @Test
    void testGetRows() {
        Grid grid = new Grid(0, 10, 10, null);
        grid.setRows(10);
        assertEquals(10, grid.getRows());
    }

    @Test
    void testGetColumns() {
        Grid grid = new Grid(0, 10, 10, null);
        grid.setColumns(10);
        assertEquals(10, grid.getColumns());
    }

    @Test
    void testGetName_NullWhenConstructedWithoutName() {
        Grid grid = new Grid(0, 10, 10, null);
        assertEquals(null, grid.getName());
    }

    @Test
    void testGetSetName() {
        Grid grid = new Grid(0, 10, 10, "battlefield");
        assertEquals("battlefield", grid.getName());
        grid.setName("arena");
        assertEquals("arena", grid.getName());
    }
}
