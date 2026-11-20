// Copyright (c) 2024 Preponderous Software
// MIT License

package preponderous.viron;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Drift guard for {@code postman/Viron.postman_collection.json}. The collection describes itself as
 * generated from {@code docs/openapi/viron-api.json}, but nothing regenerates it, so it can only stay
 * true by being checked. {@link OpenApiSpecDriftTest} already holds the spec to the controllers; this
 * holds the collection to the spec, so a request that Postman sends is one the API documents.
 */
class PostmanCollectionDriftTest {

    private static final String SPEC = "docs/openapi/viron-api.json";
    private static final String COLLECTION = "postman/Viron.postman_collection.json";

    private static JsonNode spec;
    private static JsonNode collection;
    private static List<JsonNode> requests;

    @BeforeAll
    static void load() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        spec = mapper.readTree(new File(SPEC));
        collection = mapper.readTree(new File(COLLECTION));
        requests = new ArrayList<>();
        collectRequests(collection.path("item"), requests);
    }

    /** Every documented operation has exactly one request, and no request targets an undocumented one. */
    @Test
    void collectionCoversExactlyTheSpecsOperations() {
        Set<String> specRoutes = new TreeSet<>();
        Iterator<Map.Entry<String, JsonNode>> paths = spec.path("paths").fields();
        while (paths.hasNext()) {
            Map.Entry<String, JsonNode> path = paths.next();
            path.getValue().fieldNames().forEachRemaining(verb -> {
                if (!"parameters".equals(verb)) {
                    specRoutes.add(verb.toUpperCase() + " " + path.getKey());
                }
            });
        }

        List<String> collectionRoutes = new ArrayList<>();
        for (JsonNode request : requests) {
            collectionRoutes.add(routeOf(request));
        }

        assertEquals(collectionRoutes.size(), new TreeSet<>(collectionRoutes).size(),
                COLLECTION + " sends some route more than once: " + collectionRoutes);
        Set<String> missing = new TreeSet<>(specRoutes);
        missing.removeAll(collectionRoutes);
        Set<String> undocumented = new TreeSet<>(collectionRoutes);
        undocumented.removeAll(specRoutes);
        assertEquals("missing=[] undocumented=[]", "missing=" + missing + " undocumented=" + undocumented,
                COLLECTION + " has drifted from " + SPEC + ". Add, remove or correct requests so both "
                        + "list the same operations.");
    }

    /** A request's name and its {@code url.raw} are what a reader sees; both must say what it sends. */
    @Test
    void requestNamesAndRawUrlsAgreeWithTheRequestSent() {
        Set<String> mismatched = new TreeSet<>();
        for (JsonNode request : requests) {
            String route = routeOf(request);
            if (!route.equals(request.path("name").asText())) {
                mismatched.add(route + " is named \"" + request.path("name").asText() + "\"");
            }
            JsonNode url = request.path("request").path("url");
            String expectedRaw = String.join(".", textsOf(url.path("host")))
                    + (url.has("port") ? ":" + url.path("port").asText() : "")
                    + "/" + String.join("/", textsOf(url.path("path")))
                    + queryStringOf(url);
            if (!expectedRaw.equals(url.path("raw").asText())) {
                mismatched.add(route + " has url.raw \"" + url.path("raw").asText() + "\", expected \""
                        + expectedRaw + "\"");
            }
        }
        assertEquals(Set.of(), mismatched);
    }

    /** Query parameters sent are ones the operation declares, and every required one is sent. */
    @Test
    void queryParametersMatchTheOperation() {
        Set<String> problems = new TreeSet<>();
        for (JsonNode request : requests) {
            JsonNode operation = operationOf(request);
            Set<String> declared = new TreeSet<>();
            Set<String> required = new TreeSet<>();
            for (JsonNode parameter : operation.path("parameters")) {
                JsonNode resolved = resolve(parameter);
                if ("query".equals(resolved.path("in").asText())) {
                    declared.add(resolved.path("name").asText());
                    if (resolved.path("required").asBoolean(false)) {
                        required.add(resolved.path("name").asText());
                    }
                }
            }
            Set<String> sent = new TreeSet<>();
            for (JsonNode query : request.path("request").path("url").path("query")) {
                sent.add(query.path("key").asText());
            }
            for (String name : sent) {
                if (!declared.contains(name)) {
                    problems.add(routeOf(request) + " sends undeclared query parameter " + name);
                }
            }
            for (String name : required) {
                if (!sent.contains(name)) {
                    problems.add(routeOf(request) + " omits required query parameter " + name);
                }
            }
        }
        assertEquals(Set.of(), problems);
    }

    /**
     * A request carries a JSON body exactly when the operation declares one, and that body names only
     * properties of the schema and every required one, so it is not answered 400 for its own shape.
     */
    @Test
    void requestBodiesMatchTheOperationsSchema() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Set<String> problems = new TreeSet<>();
        for (JsonNode request : requests) {
            String route = routeOf(request);
            JsonNode requestBody = operationOf(request).path("requestBody");
            JsonNode body = request.path("request").path("body");
            if (requestBody.isMissingNode()) {
                if (!body.isMissingNode()) {
                    problems.add(route + " sends a body the operation does not declare");
                }
                continue;
            }
            if (body.isMissingNode()) {
                problems.add(route + " sends no body, but the operation requires one");
                continue;
            }
            JsonNode schema = resolve(requestBody.path("content").path("application/json").path("schema"));
            JsonNode sent = mapper.readTree(body.path("raw").asText());
            sent.fieldNames().forEachRemaining(field -> {
                if (!schema.path("properties").has(field)) {
                    problems.add(route + " sends property " + field + ", which the schema does not declare");
                }
            });
            for (JsonNode field : schema.path("required")) {
                if (!sent.has(field.asText())) {
                    problems.add(route + " omits required property " + field.asText());
                }
            }
            if (!hasJsonContentType(request)) {
                problems.add(route + " sends its body without Content-Type: application/json");
            }
        }
        assertEquals(Set.of(), problems);
    }

    /**
     * Every route requires the bearer token (the spec applies {@code bearerAuth} globally), so the
     * collection sends it from {@code authToken} once, and no request opts out or sends another.
     */
    @Test
    void everyRequestSendsTheBearerTokenFromAuthToken() {
        JsonNode auth = collection.path("auth");
        assertEquals("bearer", auth.path("type").asText(), "the collection must use bearer auth");
        String token = null;
        for (JsonNode entry : auth.path("bearer")) {
            if ("token".equals(entry.path("key").asText())) {
                token = entry.path("value").asText();
            }
        }
        assertEquals("{{authToken}}", token, "the bearer token must come from the authToken variable");

        boolean declaresVariable = false;
        for (JsonNode variable : collection.path("variable")) {
            declaresVariable |= "authToken".equals(variable.path("key").asText());
        }
        assertTrue(declaresVariable, "the collection must declare the authToken variable");

        Set<String> overriding = new TreeSet<>();
        for (JsonNode request : requests) {
            if (request.has("auth") || request.path("request").has("auth")) {
                overriding.add(routeOf(request));
            }
        }
        assertEquals(Set.of(), overriding,
                "These requests override the collection's bearer auth, so they would be answered 401.");
    }

    private static void collectRequests(JsonNode items, List<JsonNode> into) {
        for (JsonNode item : items) {
            if (item.has("item")) {
                collectRequests(item.path("item"), into);
            } else {
                into.add(item);
            }
        }
    }

    /** {@code VERB /path/{var}}: Postman's {@code :var} segments rewritten to the spec's form. */
    private static String routeOf(JsonNode request) {
        StringBuilder path = new StringBuilder();
        for (String segment : textsOf(request.path("request").path("url").path("path"))) {
            path.append('/').append(segment.startsWith(":") ? "{" + segment.substring(1) + "}" : segment);
        }
        return request.path("request").path("method").asText() + " " + path;
    }

    private JsonNode operationOf(JsonNode request) {
        String[] route = routeOf(request).split(" ", 2);
        return spec.path("paths").path(route[1]).path(route[0].toLowerCase());
    }

    private static JsonNode resolve(JsonNode node) {
        return node.has("$ref") ? spec.at(node.get("$ref").asText().substring(1)) : node;
    }

    private static boolean hasJsonContentType(JsonNode request) {
        for (JsonNode header : request.path("request").path("header")) {
            if ("Content-Type".equalsIgnoreCase(header.path("key").asText())
                    && "application/json".equals(header.path("value").asText())) {
                return true;
            }
        }
        return false;
    }

    private static String queryStringOf(JsonNode url) {
        List<String> pairs = new ArrayList<>();
        for (JsonNode query : url.path("query")) {
            pairs.add(query.path("key").asText() + "=" + query.path("value").asText());
        }
        return pairs.isEmpty() ? "" : "?" + String.join("&", pairs);
    }

    private static List<String> textsOf(JsonNode array) {
        List<String> texts = new ArrayList<>();
        array.forEach(node -> texts.add(node.asText()));
        return texts;
    }
}
