// Copyright (c) 2024 Preponderous Software
// MIT License

package preponderous.viron;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Spec-drift guard (issue #130): fails the build if the controllers' live routes
 * (as reported by springdoc at {@code /v3/api-docs}) diverge from the checked-in
 * contract at {@code docs/openapi/viron-api.json}. Enables the debug endpoints so
 * {@link preponderous.viron.controllers.DebugController}'s routes are included, since
 * they are part of the static spec too.
 */
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
@DirtiesContext
@TestPropertySource(properties = "viron.debug.enabled=true")
class OpenApiSpecDriftTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void liveRoutesMatchStaticSpec() throws Exception {
        String liveJson = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode liveSpec = mapper.readTree(liveJson);
        JsonNode staticSpec = mapper.readTree(new File("docs/openapi/viron-api.json"));

        Set<String> liveRoutes = extractRoutes(liveSpec);
        Set<String> staticRoutes = extractRoutes(staticSpec);

        assertEquals(staticRoutes, liveRoutes,
                "docs/openapi/viron-api.json has drifted from the controllers' actual routes. "
                        + "Update the spec (or the controller) so both agree.");
    }

    private Set<String> extractRoutes(JsonNode spec) {
        Set<String> routes = new TreeSet<>();
        Iterator<Map.Entry<String, JsonNode>> paths = spec.get("paths").fields();
        while (paths.hasNext()) {
            Map.Entry<String, JsonNode> pathEntry = paths.next();
            String path = pathEntry.getKey();
            pathEntry.getValue().fieldNames()
                    .forEachRemaining(verb -> routes.add(verb.toUpperCase() + " " + path));
        }
        return routes;
    }
}
