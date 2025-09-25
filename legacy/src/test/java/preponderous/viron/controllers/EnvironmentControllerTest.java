package preponderous.viron.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import preponderous.viron.exceptions.EnvironmentCreationException;
import preponderous.viron.factories.EnvironmentFactory;
import preponderous.viron.models.Environment;
import preponderous.viron.repositories.EnvironmentRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class EnvironmentControllerTest {
    private EnvironmentRepository environmentRepository;
    private EnvironmentFactory environmentFactory;
    private EnvironmentController environmentController;

    @BeforeEach
    void setUp() {
        environmentRepository = Mockito.mock(EnvironmentRepository.class);
        environmentFactory = Mockito.mock(EnvironmentFactory.class);
        environmentController = new EnvironmentController(environmentRepository, environmentFactory);
    }

    @Test
    void getAllEnvironments_Success() {
        // setup
        List<Environment> environments = Arrays.asList(
                new Environment(1, "Env1", "2023-01-01"),
                new Environment(2, "Env2", "2023-01-02")
        );
        when(environmentRepository.findAll()).thenReturn(environments);

        // execute
        ResponseEntity<List<Environment>> response = environmentController.getAllEnvironments();

        // verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(environmentRepository).findAll();
    }

    @Test
    void getAllEnvironments_ThrowsException() {
        // setup
        when(environmentRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // execute
        ResponseEntity<List<Environment>> response = environmentController.getAllEnvironments();

        // verify
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
        verify(environmentRepository).findAll();
    }

    @Test
    void getEnvironmentById_Success() {
        // setup
        Environment environment = new Environment(1, "Env1", "2023-01-01");
        when(environmentRepository.findById(1)).thenReturn(Optional.of(environment));

        // execute
        ResponseEntity<Environment> response = environmentController.getEnvironmentById(1);

        // verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Env1", response.getBody().getName());
        verify(environmentRepository).findById(1);
    }

    @Test
    void getEnvironmentById_NotFound() {
        // setup
        when(environmentRepository.findById(1)).thenReturn(Optional.empty());

        // execute
        ResponseEntity<Environment> response = environmentController.getEnvironmentById(1);

        // verify
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(environmentRepository).findById(1);
    }

    @Test
    void getEnvironmentById_ThrowsException() {
        // setup
        when(environmentRepository.findById(1)).thenThrow(new RuntimeException("Database error"));

        // execute
        ResponseEntity<Environment> response = environmentController.getEnvironmentById(1);

        // verify
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
        verify(environmentRepository).findById(1);
    }

    @Test
    void getEnvironmentByName_Success() {
        // setup
        Environment environment = new Environment(1, "Env1", "2023-01-01");
        when(environmentRepository.findByName("Env1")).thenReturn(Optional.of(environment));

        // execute
        ResponseEntity<Environment> response = environmentController.getEnvironmentByName("Env1");

        // verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Env1", response.getBody().getName());
        verify(environmentRepository).findByName("Env1");
    }

    @Test
    void getEnvironmentByName_NotFound() {
        // setup
        when(environmentRepository.findByName("NonExistent")).thenReturn(Optional.empty());

        // execute
        ResponseEntity<Environment> response = environmentController.getEnvironmentByName("NonExistent");

        // verify
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(environmentRepository).findByName("NonExistent");
    }

    @Test
    void getEnvironmentOfEntity_Success() {
        // setup
        Environment environment = new Environment(1, "Env1", "2023-01-01");
        when(environmentRepository.findByEntityId(1)).thenReturn(Optional.of(environment));

        // execute
        ResponseEntity<Environment> response = environmentController.getEnvironmentOfEntity(1);

        // verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Env1", response.getBody().getName());
        verify(environmentRepository).findByEntityId(1);
    }

    @Test
    void getEnvironmentOfEntity_NotFound() {
        // setup
        when(environmentRepository.findByEntityId(1)).thenReturn(Optional.empty());

        // execute
        ResponseEntity<Environment> response = environmentController.getEnvironmentOfEntity(1);

        // verify
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(environmentRepository).findByEntityId(1);
    }

    @Test
    void createEnvironment_Success() throws EnvironmentCreationException {
        // setup
        Environment environment = new Environment(1, "NewEnv", "2023-01-01");
        when(environmentFactory.createEnvironment("NewEnv", 5, 10)).thenReturn(environment);

        // execute
        ResponseEntity<Environment> response = environmentController.createEnvironment("NewEnv", 5, 10);

        // verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("NewEnv", response.getBody().getName());
        verify(environmentFactory).createEnvironment("NewEnv", 5, 10);
    }

    @Test
    void createEnvironment_CreationException() throws EnvironmentCreationException {
        // setup
        when(environmentFactory.createEnvironment("NewEnv", 5, 10))
                .thenThrow(new EnvironmentCreationException("Creation failed"));

        // execute
        ResponseEntity<Environment> response = environmentController.createEnvironment("NewEnv", 5, 10);

        // verify
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verify(environmentFactory).createEnvironment("NewEnv", 5, 10);
    }

    @Test
    void deleteEnvironment_Success() {
        // setup
        when(environmentRepository.findById(1)).thenReturn(Optional.of(new Environment(1, "Env1", "2023-01-01")));
        when(environmentRepository.findEntityIdsByEnvironmentId(1)).thenReturn(Arrays.asList(1, 2));
        when(environmentRepository.findLocationIdsByEnvironmentId(1)).thenReturn(Arrays.asList(3, 4));
        when(environmentRepository.findGridIdsByEnvironmentId(1)).thenReturn(Arrays.asList(5, 6));
        when(environmentRepository.deleteEntityLocation(anyInt())).thenReturn(true);
        when(environmentRepository.deleteLocationGrid(anyInt())).thenReturn(true);
        when(environmentRepository.deleteGridEnvironment(anyInt())).thenReturn(true);
        when(environmentRepository.deleteEntity(anyInt())).thenReturn(true);
        when(environmentRepository.deleteLocation(anyInt())).thenReturn(true);
        when(environmentRepository.deleteGrid(anyInt())).thenReturn(true);
        when(environmentRepository.deleteById(1)).thenReturn(true);

        // execute
        ResponseEntity<Void> response = environmentController.deleteEnvironment(1);

        // verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(environmentRepository).deleteById(1);
    }

    @Test
    void deleteEnvironment_NotFound() {
        // setup
        when(environmentRepository.findById(1)).thenReturn(Optional.empty());

        // execute
        ResponseEntity<Void> response = environmentController.deleteEnvironment(1);

        // verify
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(environmentRepository, never()).deleteById(anyInt());
    }

    @Test
    void updateEnvironmentName_Success() {
        // setup
        when(environmentRepository.findById(1)).thenReturn(Optional.of(new Environment(1, "OldName", "2023-01-01")));
        when(environmentRepository.updateName(1, "NewName")).thenReturn(true);

        // execute
        ResponseEntity<Void> response = environmentController.updateEnvironmentName(1, "NewName");

        // verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(environmentRepository).updateName(1, "NewName");
    }

    @Test
    void updateEnvironmentName_NotFound() {
        // setup
        when(environmentRepository.findById(1)).thenReturn(Optional.empty());

        // execute
        ResponseEntity<Void> response = environmentController.updateEnvironmentName(1, "NewName");

        // verify
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(environmentRepository, never()).updateName(anyInt(), anyString());
    }

    @Test
    void updateEnvironmentName_ThrowsException() {
        // setup
        when(environmentRepository.findById(1)).thenReturn(Optional.of(new Environment(1, "OldName", "2023-01-01")));
        when(environmentRepository.updateName(1, "NewName")).thenThrow(new RuntimeException("Database error"));

        // execute
        ResponseEntity<Void> response = environmentController.updateEnvironmentName(1, "NewName");

        // verify
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(environmentRepository).updateName(1, "NewName");
    }
}