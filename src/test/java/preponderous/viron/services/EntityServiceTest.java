package preponderous.viron.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import preponderous.viron.config.ServiceConfig;
import preponderous.viron.dto.CreateEntityRequest;
import preponderous.viron.dto.UpdateEntityNameRequest;
import preponderous.viron.exceptions.EntityServiceException;
import preponderous.viron.models.Entity;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class EntityServiceTest {

    private static final String BASE_URL = "http://localhost:8080/api/v1/entities";

    @MockBean
    private RestTemplate restTemplate;

    private ServiceConfig serviceConfig;
    private EntityService entityService;

    @BeforeEach
    void setUp() {
        serviceConfig = new ServiceConfig();
        serviceConfig.setVironHost("http://localhost");
        serviceConfig.setVironPort(8080);
        entityService = new EntityService(restTemplate, serviceConfig);
    }

    @Test
    void testGetAllEntities_Success_ReturnsEntities() {
        Entity[] entities = {new Entity(1, "Entity1", "2023-01-01"), new Entity(2, "Entity2", "2023-02-01")};
        Mockito.when(restTemplate.getForEntity(eq(BASE_URL), eq(Entity[].class)))
                .thenReturn(ResponseEntity.ok(entities));

        List<Entity> result = entityService.getAllEntities();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Entity1");
    }

    @Test
    void testGetAllEntities_NullBody_ReturnsEmptyList() {
        Mockito.when(restTemplate.getForEntity(eq(BASE_URL), eq(Entity[].class)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        List<Entity> result = entityService.getAllEntities();

        assertThat(result).isEmpty();
    }

    @Test
    void testGetAllEntities_RestTemplateThrows_WrapsInEntityServiceException() {
        Mockito.when(restTemplate.getForEntity(eq(BASE_URL), eq(Entity[].class)))
                .thenThrow(new RestClientException("boom"));

        assertThatThrownBy(() -> entityService.getAllEntities())
                .isInstanceOf(EntityServiceException.class)
                .hasMessage("Error retrieving entities");
    }

    @Test
    void testGetEntityById_Found_ReturnsOptionalOfEntity() {
        Entity entity = new Entity(1, "Entity1", "2023-01-01");
        Mockito.when(restTemplate.getForEntity(eq(BASE_URL + "/{id}"), eq(Entity.class), eq(1)))
                .thenReturn(ResponseEntity.ok(entity));

        Optional<Entity> result = entityService.getEntityById(1);

        assertThat(result).contains(entity);
    }

    @Test
    void testGetEntityById_RestTemplateThrows_WrapsInEntityServiceException() {
        Mockito.when(restTemplate.getForEntity(eq(BASE_URL + "/{id}"), eq(Entity.class), eq(1)))
                .thenThrow(new RestClientException("boom"));

        assertThatThrownBy(() -> entityService.getEntityById(1))
                .isInstanceOf(EntityServiceException.class)
                .hasMessage("Error retrieving entity");
    }

    @Test
    void testGetEntitiesInEnvironment_Success_ReturnsEntities() {
        Entity[] entities = {new Entity(1, "Entity1", "2023-01-01")};
        Mockito.when(restTemplate.getForEntity(eq(BASE_URL + "/environment/{environmentId}"), eq(Entity[].class), eq(5)))
                .thenReturn(ResponseEntity.ok(entities));

        List<Entity> result = entityService.getEntitiesInEnvironment(5);

        assertThat(result).hasSize(1);
    }

    @Test
    void testGetEntitiesInEnvironment_NullBody_ReturnsEmptyList() {
        Mockito.when(restTemplate.getForEntity(eq(BASE_URL + "/environment/{environmentId}"), eq(Entity[].class), eq(5)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        List<Entity> result = entityService.getEntitiesInEnvironment(5);

        assertThat(result).isEmpty();
    }

    @Test
    void testGetEntitiesInEnvironment_RestTemplateThrows_WrapsInEntityServiceException() {
        Mockito.when(restTemplate.getForEntity(eq(BASE_URL + "/environment/{environmentId}"), eq(Entity[].class), eq(5)))
                .thenThrow(new RestClientException("boom"));

        assertThatThrownBy(() -> entityService.getEntitiesInEnvironment(5))
                .isInstanceOf(EntityServiceException.class)
                .hasMessage("Error retrieving entities in environment");
    }

    @Test
    void testGetEntitiesInGrid_Success_ReturnsEntities() {
        Entity[] entities = {new Entity(1, "Entity1", "2023-01-01")};
        Mockito.when(restTemplate.getForEntity(eq(BASE_URL + "/grid/{gridId}"), eq(Entity[].class), eq(7)))
                .thenReturn(ResponseEntity.ok(entities));

        List<Entity> result = entityService.getEntitiesInGrid(7);

        assertThat(result).hasSize(1);
    }

    @Test
    void testGetEntitiesInGrid_RestTemplateThrows_WrapsInEntityServiceException() {
        Mockito.when(restTemplate.getForEntity(eq(BASE_URL + "/grid/{gridId}"), eq(Entity[].class), eq(7)))
                .thenThrow(new RestClientException("boom"));

        assertThatThrownBy(() -> entityService.getEntitiesInGrid(7))
                .isInstanceOf(EntityServiceException.class)
                .hasMessage("Error retrieving entities in grid");
    }

    @Test
    void testGetEntitiesInLocation_Success_ReturnsEntities() {
        Entity[] entities = {new Entity(1, "Entity1", "2023-01-01")};
        Mockito.when(restTemplate.getForEntity(eq(BASE_URL + "/location/{locationId}"), eq(Entity[].class), eq(9)))
                .thenReturn(ResponseEntity.ok(entities));

        List<Entity> result = entityService.getEntitiesInLocation(9);

        assertThat(result).hasSize(1);
    }

    @Test
    void testGetEntitiesInLocation_RestTemplateThrows_WrapsInEntityServiceException() {
        Mockito.when(restTemplate.getForEntity(eq(BASE_URL + "/location/{locationId}"), eq(Entity[].class), eq(9)))
                .thenThrow(new RestClientException("boom"));

        assertThatThrownBy(() -> entityService.getEntitiesInLocation(9))
                .isInstanceOf(EntityServiceException.class)
                .hasMessage("Error retrieving entities in location");
    }

    @Test
    void testGetEntitiesNotInAnyLocation_Success_ReturnsEntities() {
        Entity[] entities = {new Entity(1, "Entity1", "2023-01-01")};
        Mockito.when(restTemplate.getForEntity(eq(BASE_URL + "/unassigned"), eq(Entity[].class)))
                .thenReturn(ResponseEntity.ok(entities));

        List<Entity> result = entityService.getEntitiesNotInAnyLocation();

        assertThat(result).hasSize(1);
    }

    @Test
    void testGetEntitiesNotInAnyLocation_RestTemplateThrows_WrapsInEntityServiceException() {
        Mockito.when(restTemplate.getForEntity(eq(BASE_URL + "/unassigned"), eq(Entity[].class)))
                .thenThrow(new RestClientException("boom"));

        assertThatThrownBy(() -> entityService.getEntitiesNotInAnyLocation())
                .isInstanceOf(EntityServiceException.class)
                .hasMessage("Error retrieving unassigned entities");
    }

    @Test
    void testCreateEntity_Success_ReturnsEntity() {
        Entity entity = new Entity(1, "Bob", "2023-01-01");
        Mockito.when(restTemplate.postForEntity(eq(BASE_URL), eq(new CreateEntityRequest("Bob")), eq(Entity.class)))
                .thenReturn(ResponseEntity.ok(entity));

        Entity result = entityService.createEntity("Bob");

        assertThat(result).isEqualTo(entity);
    }

    @Test
    void testCreateEntity_NullBody_ThrowsEntityServiceException() {
        Mockito.when(restTemplate.postForEntity(eq(BASE_URL), eq(new CreateEntityRequest("Bob")), eq(Entity.class)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        assertThatThrownBy(() -> entityService.createEntity("Bob"))
                .isInstanceOf(EntityServiceException.class)
                .hasMessage("Error creating entity");
    }

    @Test
    void testCreateEntity_RestTemplateThrows_WrapsInEntityServiceException() {
        Mockito.when(restTemplate.postForEntity(eq(BASE_URL), eq(new CreateEntityRequest("Bob")), eq(Entity.class)))
                .thenThrow(new RestClientException("boom"));

        assertThatThrownBy(() -> entityService.createEntity("Bob"))
                .isInstanceOf(EntityServiceException.class)
                .hasMessage("Error creating entity");
    }

    @Test
    void testDeleteEntity_Success_ReturnsTrue() {
        boolean result = entityService.deleteEntity(1);

        assertThat(result).isTrue();
        verify(restTemplate).delete(eq(BASE_URL + "/{id}"), eq(1));
    }

    @Test
    void testDeleteEntity_RestTemplateThrows_WrapsInEntityServiceException() {
        Mockito.doThrow(new RestClientException("boom")).when(restTemplate).delete(eq(BASE_URL + "/{id}"), eq(1));

        assertThatThrownBy(() -> entityService.deleteEntity(1))
                .isInstanceOf(EntityServiceException.class)
                .hasMessage("Error deleting entity");
    }

    @Test
    void testUpdateEntityName_Success_ReturnsTrue() {
        boolean result = entityService.updateEntityName(1, "NewName");

        assertThat(result).isTrue();
        verify(restTemplate).patchForObject(eq(BASE_URL + "/{id}/name"), eq(new UpdateEntityNameRequest("NewName")), eq(Void.class), eq(1));
    }

    @Test
    void testUpdateEntityName_RestTemplateThrows_WrapsInEntityServiceException() {
        Mockito.when(restTemplate.patchForObject(eq(BASE_URL + "/{id}/name"), eq(new UpdateEntityNameRequest("NewName")), eq(Void.class), eq(1)))
                .thenThrow(new RestClientException("boom"));

        assertThatThrownBy(() -> entityService.updateEntityName(1, "NewName"))
                .isInstanceOf(EntityServiceException.class)
                .hasMessage("Error updating entity name");
    }
}
