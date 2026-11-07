// Copyright (c) 2024 Preponderous Software
// MIT License

package preponderous.viron.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import preponderous.viron.database.DbInteractions;
import preponderous.viron.models.Entity;
import preponderous.viron.models.Environment;
import preponderous.viron.models.Grid;
import preponderous.viron.repositories.EntityRepository;
import preponderous.viron.repositories.EnvironmentRepository;
import preponderous.viron.repositories.GridRepository;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * A name is stored in a {@code VARCHAR(255)} column, so a longer one can never be written. The
 * request bodies bound it to the column, which answers the request as the client error it is
 * instead of attempting the write: a failed {@code UPDATE} is indistinguishable from a server fault
 * and a failed {@code INSERT} from a generic creation failure.
 *
 * <p>The rename tests stub the repository as the failed write would leave it, so a name that got
 * past validation is answered 500 and fails the test on the status as well as on the verification.
 */
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
class NameLengthValidationTest {

    private static final String LONGEST_NAME = "n".repeat(255);
    private static final String OVERLONG_NAME = "n".repeat(256);
    private static final String REJECTION = "name: size must be between 0 and 255";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EntityRepository entityRepository;

    @MockBean
    private EnvironmentRepository environmentRepository;

    @MockBean
    private GridRepository gridRepository;

    @MockBean
    private DbInteractions dbInteractions;

    @Test
    void updateEntityName_OverlongName_ReturnsBadRequest() throws Exception {
        when(entityRepository.findById(1)).thenReturn(Optional.of(new Entity(1, "Old", "2024-01-01")));

        mockMvc.perform(patch("/api/v1/entities/1/name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyNaming(OVERLONG_NAME)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(REJECTION));

        verify(entityRepository, never()).updateName(anyInt(), anyString());
    }

    @Test
    void updateEntityName_NameFillingTheColumn_IsAccepted() throws Exception {
        when(entityRepository.findById(1)).thenReturn(Optional.of(new Entity(1, "Old", "2024-01-01")));
        when(entityRepository.updateName(1, LONGEST_NAME)).thenReturn(true);

        mockMvc.perform(patch("/api/v1/entities/1/name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyNaming(LONGEST_NAME)))
                .andExpect(status().isOk());

        verify(entityRepository).updateName(1, LONGEST_NAME);
    }

    @Test
    void updateEnvironmentName_OverlongName_ReturnsBadRequest() throws Exception {
        when(environmentRepository.findById(1)).thenReturn(Optional.of(new Environment(1, "Old", "2024-01-01")));

        mockMvc.perform(patch("/api/v1/environments/1/name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyNaming(OVERLONG_NAME)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(REJECTION));

        verify(environmentRepository, never()).updateName(anyInt(), anyString());
    }

    @Test
    void updateEnvironmentName_NameFillingTheColumn_IsAccepted() throws Exception {
        when(environmentRepository.findById(1)).thenReturn(Optional.of(new Environment(1, "Old", "2024-01-01")));
        when(environmentRepository.updateName(1, LONGEST_NAME)).thenReturn(true);

        mockMvc.perform(patch("/api/v1/environments/1/name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyNaming(LONGEST_NAME)))
                .andExpect(status().isOk());

        verify(environmentRepository).updateName(1, LONGEST_NAME);
    }

    @Test
    void updateGridName_OverlongName_ReturnsBadRequest() throws Exception {
        when(gridRepository.findById(1)).thenReturn(Optional.of(new Grid(1, 2, 2, null)));

        mockMvc.perform(patch("/api/v1/grids/1/name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyNaming(OVERLONG_NAME)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(REJECTION));

        verify(gridRepository, never()).updateName(anyInt(), anyString());
    }

    @Test
    void updateGridName_NameFillingTheColumn_IsAccepted() throws Exception {
        when(gridRepository.findById(1)).thenReturn(Optional.of(new Grid(1, 2, 2, null)));
        when(gridRepository.updateName(1, LONGEST_NAME)).thenReturn(true);

        mockMvc.perform(patch("/api/v1/grids/1/name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyNaming(LONGEST_NAME)))
                .andExpect(status().isOk());

        verify(gridRepository).updateName(1, LONGEST_NAME);
    }

    /** The real factory is behind this route, so reaching the database at all would be visible. */
    @Test
    void createEntity_OverlongName_IsRejectedBeforeTheDatabase() throws Exception {
        mockMvc.perform(post("/api/v1/entities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyNaming(OVERLONG_NAME)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(REJECTION));

        verifyNoInteractions(dbInteractions);
    }

    @Test
    void createEnvironment_OverlongName_IsRejectedBeforeTheDatabase() throws Exception {
        mockMvc.perform(post("/api/v1/environments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + OVERLONG_NAME + "\",\"numGrids\":1,\"gridSize\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(REJECTION));

        verifyNoInteractions(dbInteractions);
    }

    private static String bodyNaming(String name) {
        return "{\"name\":\"" + name + "\"}";
    }
}
