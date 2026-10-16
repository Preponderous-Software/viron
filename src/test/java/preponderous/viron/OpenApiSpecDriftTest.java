// Copyright (c) 2024 Preponderous Software
// MIT License

package preponderous.viron;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.junit.jupiter.api.Test;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.PathPatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    // Qualified by name: the actuator contributes a second RequestMappingHandlerMapping, and only
    // the MVC one carries the controllers' routes.
    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping handlerMapping;

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

    /**
     * The route comparison above comes from springdoc, which reports only what it can infer, so it
     * says nothing about the answers a route gives to a rejected input. A handler that validates an
     * input can answer 400, and #214 arrived as a route that could do so with nothing in the spec
     * saying it. This walks the controllers' own annotations instead, so the contract cannot go
     * quiet again about a validation failure some route is able to produce.
     */
    @Test
    void routesWithValidatedInputDocumentTheirValidationFailure() throws Exception {
        JsonNode staticSpec = new ObjectMapper().readTree(new File("docs/openapi/viron-api.json"));

        Set<String> undocumented = new TreeSet<>();
        forEachApiRoute((verb, path, handler) -> {
            if (!validatesAnyInput(handler)) {
                return;
            }
            JsonNode operation = operationAt(staticSpec, path, verb);
            assertNotNull(operation, verb + " " + path + " is missing from the spec entirely");
            if (!operation.get("responses").has("400")) {
                undocumented.add(verb + " " + path);
            }
        });

        assertEquals(Set.of(), undocumented,
                "These routes validate an input and so can answer 400, but docs/openapi/viron-api.json "
                        + "does not document it. Add a 400 response (the shared "
                        + "#/components/responses/ValidationError covers it).");
    }

    /**
     * Checks the bound itself rather than only the answer: an {@code @Min} on a path variable is part
     * of what the endpoint accepts, so a caller reading the spec should be able to see it without
     * having to provoke a 400 to find out.
     */
    @Test
    void boundedPathVariablesDeclareTheirBoundInTheSpec() throws Exception {
        JsonNode staticSpec = new ObjectMapper().readTree(new File("docs/openapi/viron-api.json"));

        Set<String> undeclared = new TreeSet<>();
        forEachApiRoute((verb, path, handler) -> {
            JsonNode operation = operationAt(staticSpec, path, verb);
            assertNotNull(operation, verb + " " + path + " is missing from the spec entirely");
            for (MethodParameter parameter : handler.getMethodParameters()) {
                Min min = parameter.getParameterAnnotation(Min.class);
                if (min == null) {
                    continue;
                }
                String name = pathVariableNameOf(parameter);
                JsonNode declared = parameterOf(staticSpec, operation, name);
                if (declared == null) {
                    undeclared.add(verb + " " + path + " (" + name + " is not in the spec)");
                } else if (declared.path("schema").path("minimum").asLong(Long.MIN_VALUE) != min.value()) {
                    undeclared.add(verb + " " + path + " (" + name + " should declare minimum "
                            + min.value() + ")");
                }
            }
        });

        assertEquals(Set.of(), undeclared,
                "These path variables carry an @Min bound that docs/openapi/viron-api.json does not "
                        + "express in the parameter schema.");
    }

    /**
     * Every API route sits behind SecurityConfig's bearer-token check (#230), so both descriptions of
     * the API have to say so: the live document (which drives Swagger UI's Authorize control) and the
     * checked-in contract must declare the same scheme and apply it globally.
     */
    @Test
    void liveAndStaticSpecDeclareTheSameBearerRequirement() throws Exception {
        String liveJson = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode liveSpec = mapper.readTree(liveJson);
        JsonNode staticSpec = mapper.readTree(new File("docs/openapi/viron-api.json"));

        for (JsonNode spec : new JsonNode[] {liveSpec, staticSpec}) {
            JsonNode scheme = spec.path("components").path("securitySchemes").path("bearerAuth");
            assertEquals("http", scheme.path("type").asText());
            assertEquals("bearer", scheme.path("scheme").asText());
            assertEquals("JWT", scheme.path("bearerFormat").asText());
            assertTrue(spec.path("security").path(0).has("bearerAuth"),
                    "the bearer scheme must be applied globally");
        }
    }

    /** Any route can be answered 401 by the security filter before its handler runs. */
    @Test
    void everyRouteDocumentsTheUnauthorizedAnswer() throws Exception {
        JsonNode staticSpec = new ObjectMapper().readTree(new File("docs/openapi/viron-api.json"));

        Set<String> undocumented = new TreeSet<>();
        forEachApiRoute((verb, path, handler) -> {
            JsonNode operation = operationAt(staticSpec, path, verb);
            assertNotNull(operation, verb + " " + path + " is missing from the spec entirely");
            if (!operation.get("responses").has("401")) {
                undocumented.add(verb + " " + path);
            }
        });

        assertEquals(Set.of(), undocumented,
                "These routes require a bearer token and so can answer 401, but docs/openapi/viron-api.json "
                        + "does not document it. Add #/components/responses/Unauthorized.");
    }

    private interface RouteVisitor {
        void visit(String verb, String path, HandlerMethod handler);
    }

    /** Visits every mapped {@code /api/v1} route once per HTTP method it answers. */
    private void forEachApiRoute(RouteVisitor visitor) {
        handlerMapping.getHandlerMethods().forEach((info, handler) -> {
            PathPatternsRequestCondition patterns = info.getPathPatternsCondition();
            if (patterns == null) {
                return;
            }
            for (String path : patterns.getPatternValues()) {
                if (!path.startsWith("/api/v1")) {
                    continue;
                }
                for (RequestMethod method : info.getMethodsCondition().getMethods()) {
                    visitor.visit(method.name(), path, handler);
                }
            }
        });
    }

    /** True when the handler declares a constraint Spring will enforce before its body runs. */
    private boolean validatesAnyInput(HandlerMethod handler) {
        for (MethodParameter parameter : handler.getMethodParameters()) {
            if (parameter.hasParameterAnnotation(Min.class) || parameter.hasParameterAnnotation(Valid.class)) {
                return true;
            }
        }
        return false;
    }

    private JsonNode operationAt(JsonNode spec, String path, String verb) {
        JsonNode operation = spec.path("paths").path(path).path(verb.toLowerCase());
        return operation.isMissingNode() ? null : operation;
    }

    /** The spec's declaration of {@code name} on this operation, following a {@code $ref} if used. */
    private JsonNode parameterOf(JsonNode spec, JsonNode operation, String name) {
        for (JsonNode parameter : operation.path("parameters")) {
            JsonNode resolved = parameter.has("$ref")
                    ? spec.at(parameter.get("$ref").asText().substring(1))
                    : parameter;
            if (name.equals(resolved.path("name").asText())) {
                return resolved;
            }
        }
        return null;
    }

    /**
     * The name the route binds this parameter to: the {@code @PathVariable} value when one is given,
     * and otherwise the declared parameter name, which needs a discoverer to be readable.
     */
    private String pathVariableNameOf(MethodParameter parameter) {
        PathVariable annotation = parameter.getParameterAnnotation(PathVariable.class);
        if (annotation != null && !annotation.value().isEmpty()) {
            return annotation.value();
        }
        parameter.initParameterNameDiscovery(new DefaultParameterNameDiscoverer());
        return parameter.getParameterName();
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
