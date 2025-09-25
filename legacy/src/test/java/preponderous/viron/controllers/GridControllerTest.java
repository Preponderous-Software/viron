package preponderous.viron.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import preponderous.viron.models.Grid;
import preponderous.viron.repositories.GridRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
public class GridControllerTest {
    private GridRepository gridRepository;
    private GridController gridController;

    @BeforeEach
    void setUp() {
        gridRepository = Mockito.mock(GridRepository.class);
        gridController = new GridController(gridRepository);
    }

    @Test
    void testGetAllGrids_Success() {
        // setup
        Grid grid1 = new Grid(1, 1, 1);
        Grid grid2 = new Grid(2, 2, 2);
        List<Grid> expectedGrids = Arrays.asList(grid1, grid2);
        when(gridRepository.findAll()).thenReturn(expectedGrids);

        // execute
        ResponseEntity<List<Grid>> response = gridController.getAllGrids();

        // verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedGrids, response.getBody());
    }

    @Test
    void testGetAllGrids_Exception() {
        // setup
        when(gridRepository.findAll()).thenThrow(new RuntimeException("Test Exception"));

        // execute
        ResponseEntity<List<Grid>> response = gridController.getAllGrids();

        // verify
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testGetGridById_Success() {
        // setup
        int gridId = 1;
        Grid expectedGrid = new Grid(gridId, 1, 1);
        when(gridRepository.findById(gridId)).thenReturn(Optional.of(expectedGrid));

        // execute
        ResponseEntity<Grid> response = gridController.getGridById(gridId);

        // verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedGrid, response.getBody());
    }

    @Test
    void testGetGridById_NotFound() {
        // setup
        int gridId = 1;
        when(gridRepository.findById(gridId)).thenReturn(Optional.empty());

        // execute
        ResponseEntity<Grid> response = gridController.getGridById(gridId);

        // verify
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testGetGridById_Exception() {
        // setup
        int gridId = 1;
        when(gridRepository.findById(gridId)).thenThrow(new RuntimeException("Test Exception"));

        // execute
        ResponseEntity<Grid> response = gridController.getGridById(gridId);

        // verify
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testGetGridsInEnvironment_Success() {
        // setup
        int environmentId = 1;
        Grid grid1 = new Grid(1, 1, 1);
        Grid grid2 = new Grid(2, 2, 2);
        List<Grid> expectedGrids = Arrays.asList(grid1, grid2);
        when(gridRepository.findByEnvironmentId(environmentId)).thenReturn(expectedGrids);

        // execute
        ResponseEntity<List<Grid>> response = gridController.getGridsInEnvironment(environmentId);

        // verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedGrids, response.getBody());
    }

    @Test
    void testGetGridsInEnvironment_EmptyList() {
        // setup
        int environmentId = 1;
        when(gridRepository.findByEnvironmentId(environmentId)).thenReturn(Collections.emptyList());

        // execute
        ResponseEntity<List<Grid>> response = gridController.getGridsInEnvironment(environmentId);

        // verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void testGetGridsInEnvironment_Exception() {
        // setup
        int environmentId = 1;
        when(gridRepository.findByEnvironmentId(environmentId)).thenThrow(new RuntimeException("Test Exception"));

        // execute
        ResponseEntity<List<Grid>> response = gridController.getGridsInEnvironment(environmentId);

        // verify
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testGetGridOfEntity_Success() {
        // setup
        int entityId = 1;
        Grid expectedGrid = new Grid(1, 1, 1);
        when(gridRepository.findByEntityId(entityId)).thenReturn(Optional.of(expectedGrid));

        // execute
        ResponseEntity<Grid> response = gridController.getGridOfEntity(entityId);

        // verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedGrid, response.getBody());
    }

    @Test
    void testGetGridOfEntity_NotFound() {
        // setup
        int entityId = 1;
        when(gridRepository.findByEntityId(entityId)).thenReturn(Optional.empty());

        // execute
        ResponseEntity<Grid> response = gridController.getGridOfEntity(entityId);

        // verify
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testGetGridOfEntity_Exception() {
        // setup
        int entityId = 1;
        when(gridRepository.findByEntityId(entityId)).thenThrow(new RuntimeException("Test Exception"));

        // execute
        ResponseEntity<Grid> response = gridController.getGridOfEntity(entityId);

        // verify
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }
}