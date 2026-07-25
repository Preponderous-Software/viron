package preponderous.viron.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateEnvironmentRequestTest {

    @Test
    void gridSizeShorthandResolvesToASquareGrid() {
        CreateEnvironmentRequest request = new CreateEnvironmentRequest("Env", 1, 7);

        assertTrue(request.isGridDimensionsSpecified());
        assertEquals(7, request.getResolvedNumRows());
        assertEquals(7, request.getResolvedNumColumns());
    }

    @Test
    void explicitDimensionsResolveIndependently() {
        CreateEnvironmentRequest request = new CreateEnvironmentRequest("Env", 1, null, 3, 12);

        assertTrue(request.isGridDimensionsSpecified());
        assertEquals(3, request.getResolvedNumRows());
        assertEquals(12, request.getResolvedNumColumns());
    }

    @Test
    void explicitDimensionsTakePrecedenceOverGridSize() {
        CreateEnvironmentRequest request = new CreateEnvironmentRequest("Env", 1, 5, 2, 9);

        assertTrue(request.isGridDimensionsSpecified());
        assertEquals(2, request.getResolvedNumRows());
        assertEquals(9, request.getResolvedNumColumns());
    }

    @Test
    void noDimensionsAtAllIsInvalid() {
        CreateEnvironmentRequest request = new CreateEnvironmentRequest("Env", 1, null, null, null);

        assertFalse(request.isGridDimensionsSpecified());
    }

    @Test
    void halfSpecifiedDimensionsAreInvalidEvenWithGridSize() {
        CreateEnvironmentRequest onlyRows = new CreateEnvironmentRequest("Env", 1, 5, 3, null);
        CreateEnvironmentRequest onlyColumns = new CreateEnvironmentRequest("Env", 1, 5, null, 3);

        assertFalse(onlyRows.isGridDimensionsSpecified());
        assertFalse(onlyColumns.isGridDimensionsSpecified());
    }
}
