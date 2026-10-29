// Copyright (c) 2024 Preponderous Software
// MIT License

package preponderous.viron.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Characterises the answer to a request Spring MVC itself rejects before any handler body runs: a
 * body that is not JSON, a verb the route does not map, a content type it does not accept, and a
 * path no controller maps. Each is the caller's mistake and is answered with its own 4xx and an
 * {@code ErrorResponse} — not the 500 the catch-all handler in
 * {@link preponderous.viron.exceptions.GlobalExceptionHandler} gave them before (#250).
 *
 * <p>The repositories are mocked and left unstubbed so that reaching one at all would be visible.
 */
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
class FrameworkRejectionTest {

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
    void createEntity_MalformedJsonBody_ReturnsBadRequestErrorResponse() throws Exception {
        mockMvc.perform(post("/api/v1/entities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Request body is missing, is not valid JSON, or has a field of the wrong type"));

        verifyNoInteractions(entityRepository);
    }

    @Test
    void createEnvironment_MissingBody_ReturnsBadRequestErrorResponse() throws Exception {
        mockMvc.perform(post("/api/v1/environments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Request body is missing, is not valid JSON, or has a field of the wrong type"));

        verifyNoInteractions(environmentRepository);
    }

    /**
     * Valid JSON whose field cannot be converted to the request's declared type is rejected by the
     * same reader, before bean validation runs.
     */
    @Test
    void createEnvironment_FieldOfTheWrongType_ReturnsBadRequestErrorResponse() throws Exception {
        mockMvc.perform(post("/api/v1/environments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"x\",\"numGrids\":\"abc\",\"gridSize\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(
                        "Request body is missing, is not valid JSON, or has a field of the wrong type"));

        verifyNoInteractions(environmentRepository);
    }

    /**
     * The parser's own message quotes the offending input and names Jackson's internals, so it is
     * not passed on to the caller.
     */
    @Test
    void createEntity_MalformedJsonBody_MessageDoesNotLeakParserDetail() throws Exception {
        mockMvc.perform(post("/api/v1/entities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": nope}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", not(containsString("Jackson"))))
                .andExpect(jsonPath("$.message", not(containsString("nope"))));
    }

    @Test
    void createEntity_PlainTextBody_ReturnsUnsupportedMediaType() throws Exception {
        mockMvc.perform(post("/api/v1/entities")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("name"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415));

        verifyNoInteractions(entityRepository);
    }

    /**
     * {@code /api/v1/grids/{id}} maps GET and nothing that deletes, so a DELETE there is answered
     * 405 with the {@code Allow} header naming what the route does accept.
     */
    @Test
    void deleteGrid_UnmappedVerb_ReturnsMethodNotAllowedWithAllowHeader() throws Exception {
        mockMvc.perform(delete("/api/v1/grids/1"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().string(HttpHeaders.ALLOW, containsString("GET")))
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.message").value("Method 'DELETE' is not supported."));

        verifyNoInteractions(gridRepository);
    }

    @Test
    void unmappedPath_ReturnsNotFoundErrorResponse() throws Exception {
        mockMvc.perform(get("/api/v1/no-such-resource"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
