package preponderous.viron.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import preponderous.viron.config.DbConfig;
import preponderous.viron.database.DbInteractions;
import preponderous.viron.dto.EntityDto;
import preponderous.viron.exceptions.EntityCreationException;
import preponderous.viron.factories.EntityFactory;
import preponderous.viron.mappers.EntityMapper;
import preponderous.viron.models.Entity;
import preponderous.viron.repositories.EntityRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
@DirtiesContext
class EntityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EntityRepository entityRepository;

    @MockBean
    private EntityFactory entityFactory;

    @MockBean
    private EntityMapper entityMapper;

    @MockBean
    private DbInteractions dbInteractions;

    @MockBean
    private DbConfig dbConfig;

    // --- GET /api/v1/entities ---

    @Test
    void getAllEntities_Success() throws Exception {
        List<Entity> entities = List.of(
                new Entity(1, "Entity1", "2024-01-01"),
                new Entity(2, "Entity2", "2024-01-01")
        );
        List<EntityDto> dtos = List.of(
                new EntityDto(1, "Entity1", "2024-01-01"),
                new EntityDto(2, "Entity2", "2024-01-01")
        );
        when(entityRepository.findAll()).thenReturn(entities);
        when(entityMapper.toDtoList(entities)).thenReturn(dtos);

        mockMvc.perform(get("/api/v1/entities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].entityId").value(1))
                .andExpect(jsonPath("$[0].name").value("Entity1"))
                .andExpect(jsonPath("$[0].creationDate").value("2024-01-01"))
                .andExpect(jsonPath("$[1].entityId").value(2))
                .andExpect(jsonPath("$[1].name").value("Entity2"));

        verify(entityRepository).findAll();
        verify(entityMapper).toDtoList(entities);
    }

    @Test
    void getAllEntities_EmptyList() throws Exception {
        when(entityRepository.findAll()).thenReturn(Collections.emptyList());
        when(entityMapper.toDtoList(Collections.emptyList())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/entities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getAllEntities_RepositoryThrowsException() throws Exception {
        when(entityRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/v1/entities"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").isString());
    }

    // --- GET /api/v1/entities/{id} ---

    @Test
    void getEntityById_Success() throws Exception {
        Entity entity = new Entity(1, "Entity1", "2024-01-01");
        EntityDto dto = new EntityDto(1, "Entity1", "2024-01-01");
        when(entityRepository.findById(1)).thenReturn(Optional.of(entity));
        when(entityMapper.toDto(entity)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/entities/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entityId").value(1))
                .andExpect(jsonPath("$.name").value("Entity1"))
                .andExpect(jsonPath("$.creationDate").value("2024-01-01"));
    }

    @Test
    void getEntityById_NotFound() throws Exception {
        when(entityRepository.findById(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/entities/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Entity not found with id: 999"));
    }

    @Test
    void getEntityById_RepositoryThrowsException() throws Exception {
        when(entityRepository.findById(1)).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/v1/entities/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").isString());
    }

    // --- GET /api/v1/entities/environment/{environmentId} ---

    @Test
    void getEntitiesInEnvironment_Success() throws Exception {
        List<Entity> entities = List.of(new Entity(1, "Entity1", "2024-01-01"));
        List<EntityDto> dtos = List.of(new EntityDto(1, "Entity1", "2024-01-01"));
        when(entityRepository.findByEnvironmentId(1)).thenReturn(entities);
        when(entityMapper.toDtoList(entities)).thenReturn(dtos);

        mockMvc.perform(get("/api/v1/entities/environment/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].entityId").value(1));
    }

    @Test
    void getEntitiesInEnvironment_EmptyList() throws Exception {
        when(entityRepository.findByEnvironmentId(1)).thenReturn(Collections.emptyList());
        when(entityMapper.toDtoList(Collections.emptyList())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/entities/environment/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // --- GET /api/v1/entities/grid/{gridId} ---

    @Test
    void getEntitiesInGrid_Success() throws Exception {
        List<Entity> entities = List.of(new Entity(2, "GridEntity", "2024-02-01"));
        List<EntityDto> dtos = List.of(new EntityDto(2, "GridEntity", "2024-02-01"));
        when(entityRepository.findByGridId(5)).thenReturn(entities);
        when(entityMapper.toDtoList(entities)).thenReturn(dtos);

        mockMvc.perform(get("/api/v1/entities/grid/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].entityId").value(2))
                .andExpect(jsonPath("$[0].name").value("GridEntity"));
    }

    // --- GET /api/v1/entities/location/{locationId} ---

    @Test
    void getEntitiesInLocation_Success() throws Exception {
        List<Entity> entities = List.of(new Entity(3, "LocEntity", "2024-03-01"));
        List<EntityDto> dtos = List.of(new EntityDto(3, "LocEntity", "2024-03-01"));
        when(entityRepository.findByLocationId(10)).thenReturn(entities);
        when(entityMapper.toDtoList(entities)).thenReturn(dtos);

        mockMvc.perform(get("/api/v1/entities/location/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].entityId").value(3))
                .andExpect(jsonPath("$[0].name").value("LocEntity"));
    }

    // --- GET /api/v1/entities/unassigned ---

    @Test
    void getEntitiesNotInAnyLocation_Success() throws Exception {
        List<Entity> entities = List.of(new Entity(4, "Unassigned", "2024-04-01"));
        List<EntityDto> dtos = List.of(new EntityDto(4, "Unassigned", "2024-04-01"));
        when(entityRepository.findEntitiesNotInAnyLocation()).thenReturn(entities);
        when(entityMapper.toDtoList(entities)).thenReturn(dtos);

        mockMvc.perform(get("/api/v1/entities/unassigned"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].entityId").value(4))
                .andExpect(jsonPath("$[0].name").value("Unassigned"));
    }

    @Test
    void getEntitiesNotInAnyLocation_EmptyList() throws Exception {
        when(entityRepository.findEntitiesNotInAnyLocation()).thenReturn(Collections.emptyList());
        when(entityMapper.toDtoList(Collections.emptyList())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/entities/unassigned"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // --- POST /api/v1/entities ---

    @Test
    void createEntity_ReturnsCreated() throws Exception {
        Entity entity = new Entity(1, "NewEntity", "2024-01-01");
        EntityDto dto = new EntityDto(1, "NewEntity", "2024-01-01");
        when(entityFactory.createEntity("NewEntity")).thenReturn(entity);
        when(entityMapper.toDto(entity)).thenReturn(dto);

        mockMvc.perform(post("/api/v1/entities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"NewEntity\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.entityId").value(1))
                .andExpect(jsonPath("$.name").value("NewEntity"))
                .andExpect(jsonPath("$.creationDate").value("2024-01-01"));
    }

    @Test
    void createEntity_BlankName_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/entities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(entityFactory, never()).createEntity(anyString());
    }

    @Test
    void createEntity_FactoryThrowsEntityCreationException() throws Exception {
        when(entityFactory.createEntity("BadEntity"))
                .thenThrow(new EntityCreationException("Creation failed"));

        mockMvc.perform(post("/api/v1/entities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"BadEntity\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Creation failed"));
    }

    @Test
    void createEntity_FactoryThrowsRuntimeException() throws Exception {
        when(entityFactory.createEntity("ErrorEntity"))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(post("/api/v1/entities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"ErrorEntity\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").isString());
    }

    // --- DELETE /api/v1/entities/{id} ---

    @Test
    void deleteEntity_ReturnsNoContent() throws Exception {
        when(entityRepository.findById(1)).thenReturn(Optional.of(new Entity(1, "Entity1", "2024-01-01")));
        when(entityRepository.deleteById(1)).thenReturn(true);

        mockMvc.perform(delete("/api/v1/entities/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteEntity_NotFound() throws Exception {
        when(entityRepository.findById(999)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/v1/entities/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Entity not found with id: 999"));
    }

    @Test
    void deleteEntity_RepositoryThrowsException() throws Exception {
        when(entityRepository.findById(1)).thenReturn(Optional.of(new Entity(1, "Entity1", "2024-01-01")));
        when(entityRepository.deleteById(1)).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(delete("/api/v1/entities/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").isString());
    }

    // --- PATCH /api/v1/entities/{id}/name ---

    @Test
    void updateEntityName_Success() throws Exception {
        when(entityRepository.findById(1)).thenReturn(Optional.of(new Entity(1, "OldName", "2024-01-01")));
        when(entityRepository.updateName(1, "UpdatedName")).thenReturn(true);

        mockMvc.perform(patch("/api/v1/entities/1/name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"UpdatedName\"}"))
                .andExpect(status().isOk());

        verify(entityRepository).updateName(1, "UpdatedName");
    }

    @Test
    void updateEntityName_BlankName_ReturnsBadRequest() throws Exception {
        mockMvc.perform(patch("/api/v1/entities/1/name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(entityRepository, never()).updateName(anyInt(), anyString());
    }

    @Test
    void updateEntityName_NotFound() throws Exception {
        when(entityRepository.findById(999)).thenReturn(Optional.empty());

        mockMvc.perform(patch("/api/v1/entities/999/name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"UpdatedName\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Entity not found with id: 999"));
    }

    @Test
    void updateEntityName_RepositoryThrowsException() throws Exception {
        when(entityRepository.findById(1)).thenReturn(Optional.of(new Entity(1, "OldName", "2024-01-01")));
        when(entityRepository.updateName(1, "Name")).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(patch("/api/v1/entities/1/name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Name\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").isString());
    }
}
