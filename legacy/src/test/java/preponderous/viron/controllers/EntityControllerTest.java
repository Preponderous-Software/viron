package preponderous.viron.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import preponderous.viron.exceptions.EntityCreationException;
import preponderous.viron.factories.EntityFactory;
import preponderous.viron.models.Entity;
import preponderous.viron.repositories.EntityRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
class EntityControllerTest {
    private EntityRepository entityRepository;
    private EntityFactory entityFactory;
    private EntityController entityController;

    @BeforeEach
    void setUp() {
        entityRepository = Mockito.mock(EntityRepository.class);
        entityFactory = Mockito.mock(EntityFactory.class);
        entityController = new EntityController(entityRepository, entityFactory);
    }

    @Test
    void getAllEntities_Success() {
        // prepare
        List<Entity> entities = List.of(
                new Entity(1, "Entity1", "2024-01-01"),
                new Entity(2, "Entity2", "2024-01-01")
        );
        when(entityRepository.findAll()).thenReturn(entities);

        // execute
        ResponseEntity<List<Entity>> response = entityController.getAllEntities();

        // verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entities, response.getBody());
        verify(entityRepository).findAll();
    }

    @Test
    void getAllEntities_Exception() {
        // prepare
        when(entityRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // execute
        ResponseEntity<List<Entity>> response = entityController.getAllEntities();

        // verify
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getEntityById_Success() {
        // prepare
        Entity entity = new Entity(1, "Entity1", "2024-01-01");
        when(entityRepository.findById(1)).thenReturn(Optional.of(entity));

        // execute
        ResponseEntity<Entity> response = entityController.getEntityById(1);

        // verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
    }

    @Test
    void getEntityById_NotFound() {
        // prepare
        when(entityRepository.findById(anyInt())).thenReturn(Optional.empty());

        // execute
        ResponseEntity<Entity> response = entityController.getEntityById(1);

        // verify
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getEntitiesInEnvironment_Success() {
        // prepare
        List<Entity> entities = new ArrayList<>();
        when(entityRepository.findByEnvironmentId(anyInt())).thenReturn(entities);

        // execute
        ResponseEntity<List<Entity>> response = entityController.getEntitiesInEnvironment(1);

        // verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entities, response.getBody());
    }

    @Test
    void getEntitiesInGrid_Success() {
        // prepare
        List<Entity> entities = new ArrayList<>();
        when(entityRepository.findByGridId(anyInt())).thenReturn(entities);

        // execute
        ResponseEntity<List<Entity>> response = entityController.getEntitiesInGrid(1);

        // verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entities, response.getBody());
    }

    @Test
    void getEntitiesInLocation_Success() {
        // prepare
        List<Entity> entities = new ArrayList<>();
        when(entityRepository.findByLocationId(anyInt())).thenReturn(entities);

        // execute
        ResponseEntity<List<Entity>> response = entityController.getEntitiesInLocation(1);

        // verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entities, response.getBody());
    }

    @Test
    void getEntitiesNotInAnyLocation_Success() {
        // prepare
        List<Entity> entities = new ArrayList<>();
        when(entityRepository.findEntitiesNotInAnyLocation()).thenReturn(entities);

        // execute
        ResponseEntity<List<Entity>> response = entityController.getEntitiesNotInAnyLocation();

        // verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entities, response.getBody());
    }

    @Test
    void createEntity_Success() throws EntityCreationException {
        // prepare
        Entity entity = new Entity(1, "New Entity", "2024-01-01");
        when(entityFactory.createEntity(anyString())).thenReturn(entity);

        // execute
        ResponseEntity<Entity> response = entityController.createEntity("New Entity");

        // verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
    }

    @Test
    void createEntity_EntityCreationException() throws EntityCreationException {
        // prepare
        when(entityFactory.createEntity(anyString()))
                .thenThrow(new EntityCreationException("Creation failed"));

        // execute
        ResponseEntity<Entity> response = entityController.createEntity("New Entity");

        // verify
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void deleteEntity_Success() {
        // prepare
        when(entityRepository.deleteById(anyInt())).thenReturn(true);

        // execute
        ResponseEntity<Void> response = entityController.deleteEntity(1);

        // verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void deleteEntity_NotFound() {
        // prepare
        when(entityRepository.deleteById(anyInt())).thenReturn(false);

        // execute
        ResponseEntity<Void> response = entityController.deleteEntity(1);

        // verify
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void updateEntityName_Success() {
        // prepare
        when(entityRepository.updateName(anyInt(), anyString())).thenReturn(true);

        // execute
        ResponseEntity<Void> response = entityController.updateEntityName(1, "New Name");

        // verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void updateEntityName_NotFound() {
        // prepare
        when(entityRepository.updateName(anyInt(), anyString())).thenReturn(false);

        // execute
        ResponseEntity<Void> response = entityController.updateEntityName(1, "New Name");

        // verify
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testExceptionHandling_InternalServerError() {
        // prepare
        when(entityRepository.findAll()).thenThrow(new RuntimeException("Unexpected error"));

        // execute
        ResponseEntity<List<Entity>> response = entityController.getAllEntities();

        // verify
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }
}