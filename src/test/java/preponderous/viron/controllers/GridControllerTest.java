package preponderous.viron.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import preponderous.viron.config.DbConfig;
import preponderous.viron.database.DbInteractions;
import preponderous.viron.dto.GridDto;
import preponderous.viron.mappers.GridMapper;
import preponderous.viron.models.Grid;
import preponderous.viron.repositories.GridRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
@DirtiesContext
class GridControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GridRepository gridRepository;

    @MockBean
    private GridMapper gridMapper;

    @MockBean
    private DbInteractions dbInteractions;

    @MockBean
    private DbConfig dbConfig;

    // --- GET /api/v1/grids ---

    @Test
    void getAllGrids_Success() throws Exception {
        List<Grid> grids = List.of(
                new Grid(1, 10, 20),
                new Grid(2, 30, 40)
        );
        List<GridDto> dtos = List.of(
                new GridDto(1, 10, 20),
                new GridDto(2, 30, 40)
        );
        when(gridRepository.findAll()).thenReturn(grids);
        when(gridMapper.toDtoList(grids)).thenReturn(dtos);

        mockMvc.perform(get("/api/v1/grids"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].gridId").value(1))
                .andExpect(jsonPath("$[0].rows").value(10))
                .andExpect(jsonPath("$[0].columns").value(20))
                .andExpect(jsonPath("$[1].gridId").value(2))
                .andExpect(jsonPath("$[1].rows").value(30))
                .andExpect(jsonPath("$[1].columns").value(40));

        verify(gridRepository).findAll();
        verify(gridMapper).toDtoList(grids);
    }

    @Test
    void getAllGrids_RepositoryThrowsException() throws Exception {
        when(gridRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/v1/grids"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").isString());
    }

    // --- GET /api/v1/grids/{id} ---

    @Test
    void getGridById_Success() throws Exception {
        Grid grid = new Grid(1, 10, 20);
        GridDto dto = new GridDto(1, 10, 20);
        when(gridRepository.findById(1)).thenReturn(Optional.of(grid));
        when(gridMapper.toDto(grid)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/grids/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gridId").value(1))
                .andExpect(jsonPath("$.rows").value(10))
                .andExpect(jsonPath("$.columns").value(20));

        verify(gridRepository).findById(1);
        verify(gridMapper).toDto(grid);
    }

    @Test
    void getGridById_NotFound() throws Exception {
        when(gridRepository.findById(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/grids/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Grid not found with id: 999"));
    }

    @Test
    void getGridById_RepositoryThrowsException() throws Exception {
        when(gridRepository.findById(1)).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/v1/grids/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").isString());
    }

    // --- GET /api/v1/grids/environment/{environmentId} ---

    @Test
    void getGridsInEnvironment_Success() throws Exception {
        List<Grid> grids = List.of(new Grid(1, 10, 20));
        List<GridDto> dtos = List.of(new GridDto(1, 10, 20));
        when(gridRepository.findByEnvironmentId(1)).thenReturn(grids);
        when(gridMapper.toDtoList(grids)).thenReturn(dtos);

        mockMvc.perform(get("/api/v1/grids/environment/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].gridId").value(1))
                .andExpect(jsonPath("$[0].rows").value(10))
                .andExpect(jsonPath("$[0].columns").value(20));

        verify(gridRepository).findByEnvironmentId(1);
        verify(gridMapper).toDtoList(grids);
    }

    @Test
    void getGridsInEnvironment_EmptyList() throws Exception {
        when(gridRepository.findByEnvironmentId(1)).thenReturn(Collections.emptyList());
        when(gridMapper.toDtoList(Collections.emptyList())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/grids/environment/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getGridsInEnvironment_RepositoryThrowsException() throws Exception {
        when(gridRepository.findByEnvironmentId(1)).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/v1/grids/environment/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").isString());
    }

    // --- GET /api/v1/grids/entity/{entityId} ---

    @Test
    void getGridOfEntity_Success() throws Exception {
        Grid grid = new Grid(5, 15, 25);
        GridDto dto = new GridDto(5, 15, 25);
        when(gridRepository.findByEntityId(1)).thenReturn(Optional.of(grid));
        when(gridMapper.toDto(grid)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/grids/entity/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gridId").value(5))
                .andExpect(jsonPath("$.rows").value(15))
                .andExpect(jsonPath("$.columns").value(25));

        verify(gridRepository).findByEntityId(1);
        verify(gridMapper).toDto(grid);
    }

    @Test
    void getGridOfEntity_NotFound() throws Exception {
        when(gridRepository.findByEntityId(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/grids/entity/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Grid not found for entity: 999"));
    }

    @Test
    void getGridOfEntity_RepositoryThrowsException() throws Exception {
        when(gridRepository.findByEntityId(1)).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/v1/grids/entity/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").isString());
    }
}
