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
        Grid grid = new Grid(0, 10, 10);
    }

    @Test
    void testGetId() {
        Grid grid = new Grid(0, 10, 10);
        grid.setGridId(0);
        assertEquals(0, grid.getGridId());
    }

    @Test
    void testGetRows() {
        Grid grid = new Grid(0, 10, 10);
        grid.setRows(10);
        assertEquals(10, grid.getRows());
    }

    @Test
    void testGetColumns() {
        Grid grid = new Grid(0, 10, 10);
        grid.setColumns(10);
        assertEquals(10, grid.getColumns());
    }
}
