// Copyright (c) 2024 Preponderous Software
// MIT License

package preponderous.viron.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import preponderous.viron.database.DbInteractions;
import preponderous.viron.repositories.EntityRepository;
import preponderous.viron.repositories.EnvironmentRepository;
import preponderous.viron.repositories.GridRepository;
import preponderous.viron.repositories.LocationRepository;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Characterises the answer the controllers give to a path variable that fails the {@code @Min(1)}
 * bound they all declare. The bound is enforced by {@code @Validated} on each controller, so the
 * violation is raised before the handler body runs and is answered by
 * {@link preponderous.viron.exceptions.GlobalExceptionHandler} as a 400 carrying an
 * {@code ErrorResponse} — not as the 404 a caller might expect from an id that names nothing.
 *
 * <p>The repositories are mocked and left unstubbed so that reaching one at all would be visible:
 * a request rejected by validation must not have consulted them.
 */
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
class PathVariableValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EntityRepository entityRepository;

    @MockBean
    private EnvironmentRepository environmentRepository;

    @MockBean
    private GridRepository gridRepository;

    @MockBean
    private LocationRepository locationRepository;

    @MockBean
    private DbInteractions dbInteractions;

    @Test
    void getEntityById_ZeroId_ReturnsBadRequestErrorResponse() throws Exception {
        mockMvc.perform(get("/api/v1/entities/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("id: must be greater than or equal to 1"));

        verifyNoInteractions(entityRepository);
    }

    /**
     * The property path the validator reports is {@code getEntityById.id}, so the message must name
     * the parameter without naming the handler method that received it.
     */
    @Test
    void getEntityById_ZeroId_MessageDoesNotNameTheHandlerMethod() throws Exception {
        mockMvc.perform(get("/api/v1/entities/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", not(containsString("getEntityById"))));
    }

    /**
     * Two violated bounds in one request are reported together, in the manner
     * {@code MethodArgumentNotValidException} field errors are joined.
     */
    @Test
    void addEntityToLocation_BothIdsZero_ReportsBothViolations() throws Exception {
        mockMvc.perform(put("/api/v1/locations/0/entity/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "entityId: must be greater than or equal to 1; "
                                + "locationId: must be greater than or equal to 1"));

        verifyNoInteractions(locationRepository);
    }

    /**
     * A negative id is rejected by the same bound as a zero one; nothing about the answer depends on
     * the id being exactly zero.
     */
    @Test
    void getLocationById_NegativeId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/locations/-3"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("id: must be greater than or equal to 1"));

        verifyNoInteractions(locationRepository);
    }

    @Test
    void getEnvironmentById_ZeroId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/environments/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(environmentRepository);
    }

    @Test
    void getGridById_ZeroId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/grids/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(gridRepository);
    }

    @Test
    void getLocationById_ZeroId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/locations/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(locationRepository);
    }

    @Test
    void updateEntityName_ZeroId_ReturnsBadRequestBeforeReadingTheBody() throws Exception {
        mockMvc.perform(patch("/api/v1/entities/0/name")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Valid\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(entityRepository);
    }

    @Test
    void addEntityToLocation_ZeroEntityId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(put("/api/v1/locations/1/entity/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(locationRepository);
    }

    /**
     * A path variable that cannot be converted to {@code int} is a client error, answered before
     * the handler or the {@code @Min(1)} bound is reached (#236).
     */
    @Test
    void getLocationById_NonIntegerId_ReturnsBadRequestNamingTheParameter() throws Exception {
        mockMvc.perform(get("/api/v1/locations/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("id: must be an integer"));

        verifyNoInteractions(locationRepository);
    }

    @Test
    void addEntityToLocation_NonIntegerEntityId_ReturnsBadRequestNamingTheParameter() throws Exception {
        mockMvc.perform(put("/api/v1/locations/1/entity/x"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("entityId: must be an integer"));

        verifyNoInteractions(locationRepository);
    }

    /**
     * A query parameter takes the same path as a path variable when its value cannot be converted.
     */
    @Test
    void getPlacements_NonIntegerEnvironmentId_ReturnsBadRequestNamingTheParameter() throws Exception {
        mockMvc.perform(get("/api/v1/locations/placements").param("environmentId", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("environmentId: must be an integer"));

        verifyNoInteractions(locationRepository);
    }
}
