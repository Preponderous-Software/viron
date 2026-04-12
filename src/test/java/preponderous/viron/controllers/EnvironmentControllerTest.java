package preponderous.viron.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import preponderous.viron.config.DbConfig;
import preponderous.viron.database.DbInteractions;
import preponderous.viron.dto.EnvironmentDto;
import preponderous.viron.exceptions.EnvironmentCreationException;
import preponderous.viron.factories.EnvironmentFactory;
import preponderous.viron.mappers.EnvironmentMapper;
import preponderous.viron.models.Environment;
import preponderous.viron.repositories.EnvironmentRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext
class EnvironmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EnvironmentRepository environmentRepository;

    @MockBean
    private EnvironmentFactory environmentFactory;

    @MockBean
    private EnvironmentMapper environmentMapper;

    @MockBean
    private DbInteractions dbInteractions;

    @MockBean
    private DbConfig dbConfig;

    @Test
    void getAllEnvironments_Success() throws Exception {
        List<Environment> environments = Arrays.asList(
                new Environment(1, "Env1", "2023-01-01"),
                new Environment(2, "Env2", "2023-01-02")
        );
        List<EnvironmentDto> dtos = Arrays.asList(
                new EnvironmentDto(1, "Env1", "2023-01-01"),
                new EnvironmentDto(2, "Env2", "2023-01-02")
        );
        when(environmentRepository.findAll()).thenReturn(environments);
        when(environmentMapper.toDtoList(environments)).thenReturn(dtos);

        mockMvc.perform(get("/api/v1/environments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].environmentId", is(1)))
                .andExpect(jsonPath("$[0].name", is("Env1")))
                .andExpect(jsonPath("$[0].creationDate", is("2023-01-01")))
                .andExpect(jsonPath("$[1].environmentId", is(2)))
                .andExpect(jsonPath("$[1].name", is("Env2")));

        verify(environmentRepository).findAll();
        verify(environmentMapper).toDtoList(environments);
    }

    @Test
    void getAllEnvironments_EmptyList() throws Exception {
        when(environmentRepository.findAll()).thenReturn(Collections.emptyList());
        when(environmentMapper.toDtoList(Collections.emptyList())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/environments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(environmentRepository).findAll();
    }

    @Test
    void getAllEnvironments_ThrowsException() throws Exception {
        when(environmentRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/v1/environments"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status", is(500)))
                .andExpect(jsonPath("$.message", is("An unexpected error occurred")));
    }

    @Test
    void getEnvironmentById_Success() throws Exception {
        Environment environment = new Environment(1, "Env1", "2023-01-01");
        EnvironmentDto dto = new EnvironmentDto(1, "Env1", "2023-01-01");
        when(environmentRepository.findById(1)).thenReturn(Optional.of(environment));
        when(environmentMapper.toDto(environment)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/environments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.environmentId", is(1)))
                .andExpect(jsonPath("$.name", is("Env1")))
                .andExpect(jsonPath("$.creationDate", is("2023-01-01")));

        verify(environmentRepository).findById(1);
        verify(environmentMapper).toDto(environment);
    }

    @Test
    void getEnvironmentById_NotFound() throws Exception {
        when(environmentRepository.findById(1)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/environments/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Environment not found with id: 1")));
    }

    @Test
    void getEnvironmentById_ThrowsException() throws Exception {
        when(environmentRepository.findById(1)).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/v1/environments/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status", is(500)))
                .andExpect(jsonPath("$.message", is("An unexpected error occurred")));
    }

    @Test
    void getEnvironmentByName_Success() throws Exception {
        Environment environment = new Environment(1, "Env1", "2023-01-01");
        EnvironmentDto dto = new EnvironmentDto(1, "Env1", "2023-01-01");
        when(environmentRepository.findByName("Env1")).thenReturn(Optional.of(environment));
        when(environmentMapper.toDto(environment)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/environments/name/Env1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.environmentId", is(1)))
                .andExpect(jsonPath("$.name", is("Env1")))
                .andExpect(jsonPath("$.creationDate", is("2023-01-01")));

        verify(environmentRepository).findByName("Env1");
        verify(environmentMapper).toDto(environment);
    }

    @Test
    void getEnvironmentByName_NotFound() throws Exception {
        when(environmentRepository.findByName("NonExistent")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/environments/name/NonExistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Environment not found with name: NonExistent")));
    }

    @Test
    void getEnvironmentOfEntity_Success() throws Exception {
        Environment environment = new Environment(1, "Env1", "2023-01-01");
        EnvironmentDto dto = new EnvironmentDto(1, "Env1", "2023-01-01");
        when(environmentRepository.findByEntityId(1)).thenReturn(Optional.of(environment));
        when(environmentMapper.toDto(environment)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/environments/entity/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.environmentId", is(1)))
                .andExpect(jsonPath("$.name", is("Env1")))
                .andExpect(jsonPath("$.creationDate", is("2023-01-01")));

        verify(environmentRepository).findByEntityId(1);
        verify(environmentMapper).toDto(environment);
    }

    @Test
    void getEnvironmentOfEntity_NotFound() throws Exception {
        when(environmentRepository.findByEntityId(1)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/environments/entity/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Environment not found for entity: 1")));
    }

    @Test
    void createEnvironment_Success() throws Exception {
        Environment environment = new Environment(1, "NewEnv", "2023-01-01");
        EnvironmentDto dto = new EnvironmentDto(1, "NewEnv", "2023-01-01");
        when(environmentFactory.createEnvironment("NewEnv", 5, 10)).thenReturn(environment);
        when(environmentMapper.toDto(environment)).thenReturn(dto);

        mockMvc.perform(post("/api/v1/environments/NewEnv/5/10"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.environmentId", is(1)))
                .andExpect(jsonPath("$.name", is("NewEnv")))
                .andExpect(jsonPath("$.creationDate", is("2023-01-01")));

        verify(environmentFactory).createEnvironment("NewEnv", 5, 10);
        verify(environmentMapper).toDto(environment);
    }

    @Test
    void createEnvironment_CreationException() throws Exception {
        when(environmentFactory.createEnvironment("NewEnv", 5, 10))
                .thenThrow(new EnvironmentCreationException("Creation failed"));

        mockMvc.perform(post("/api/v1/environments/NewEnv/5/10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("Creation failed")));
    }

    @Test
    void createEnvironment_ThrowsException() throws Exception {
        when(environmentFactory.createEnvironment("NewEnv", 5, 10))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(post("/api/v1/environments/NewEnv/5/10"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status", is(500)))
                .andExpect(jsonPath("$.message", is("An unexpected error occurred")));
    }

    @Test
    void deleteEnvironment_Success() throws Exception {
        Environment env = new Environment(1, "Env1", "2023-01-01");
        when(environmentRepository.findById(1)).thenReturn(Optional.of(env));
        when(environmentRepository.findEntityIdsByEnvironmentId(1)).thenReturn(List.of(1, 2));
        when(environmentRepository.findLocationIdsByEnvironmentId(1)).thenReturn(List.of(3, 4));
        when(environmentRepository.findGridIdsByEnvironmentId(1)).thenReturn(List.of(5));
        when(environmentRepository.deleteEntityLocation(anyInt())).thenReturn(true);
        when(environmentRepository.deleteLocationGrid(anyInt())).thenReturn(true);
        when(environmentRepository.deleteGridEnvironment(anyInt())).thenReturn(true);
        when(environmentRepository.deleteEntity(anyInt())).thenReturn(true);
        when(environmentRepository.deleteLocation(anyInt())).thenReturn(true);
        when(environmentRepository.deleteGrid(anyInt())).thenReturn(true);
        when(environmentRepository.deleteById(1)).thenReturn(true);

        mockMvc.perform(delete("/api/v1/environments/1"))
                .andExpect(status().isNoContent());

        verify(environmentRepository).deleteById(1);
    }

    @Test
    void deleteEnvironment_NotFound() throws Exception {
        when(environmentRepository.findById(1)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/v1/environments/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Environment not found with id: 1")));

        verify(environmentRepository, never()).deleteById(anyInt());
    }

    @Test
    void updateEnvironmentName_Success() throws Exception {
        when(environmentRepository.findById(1)).thenReturn(Optional.of(new Environment(1, "OldName", "2023-01-01")));
        when(environmentRepository.updateName(1, "NewName")).thenReturn(true);

        mockMvc.perform(patch("/api/v1/environments/1/name/NewName"))
                .andExpect(status().isOk());

        verify(environmentRepository).updateName(1, "NewName");
    }

    @Test
    void updateEnvironmentName_NotFound() throws Exception {
        when(environmentRepository.findById(1)).thenReturn(Optional.empty());

        mockMvc.perform(patch("/api/v1/environments/1/name/NewName"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Environment not found with id: 1")));

        verify(environmentRepository, never()).updateName(anyInt(), anyString());
    }

    @Test
    void updateEnvironmentName_ThrowsException() throws Exception {
        when(environmentRepository.findById(1)).thenReturn(Optional.of(new Environment(1, "OldName", "2023-01-01")));
        when(environmentRepository.updateName(1, "NewName")).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(patch("/api/v1/environments/1/name/NewName"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status", is(500)))
                .andExpect(jsonPath("$.message", is("An unexpected error occurred")));
    }
}