// Copyright (c) 2024 Preponderous Software
// MIT License

package preponderous.viron.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import preponderous.viron.database.DbInteractions;
import preponderous.viron.factories.EntityFactory;
import preponderous.viron.factories.EnvironmentFactory;
import preponderous.viron.models.Entity;
import preponderous.viron.models.Environment;
import preponderous.viron.repositories.EntityRepository;
import preponderous.viron.repositories.EnvironmentRepository;
import preponderous.viron.repositories.GridRepository;
import preponderous.viron.repositories.LocationRepository;

/**
 * Read-only tokens (issue #245), exercised with real HS256 tokens signed with the test secret
 * so the whole JWT path runs, not a mocked principal.
 *
 * <p>A token whose {@code scope} grants {@code viron:read} and not {@code viron:write} gets
 * {@code 200} on the read routes a viewer polls and {@code 403} on every write route; a token
 * without a {@code scope} claim (what UserAuth issues today) is not affected.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext
@TestPropertySource(properties = "viron.debug.enabled=true")
class ReadOnlyTokenTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping handlerMapping;

    @Value("${app.jwt.secret}")
    private String secret;

    @MockBean
    private EnvironmentRepository environmentRepository;

    @MockBean
    private GridRepository gridRepository;

    @MockBean
    private LocationRepository locationRepository;

    @MockBean
    private EntityRepository entityRepository;

    @MockBean
    private EnvironmentFactory environmentFactory;

    @MockBean
    private EntityFactory entityFactory;

    @MockBean
    private DbInteractions dbInteractions;

    private String token(Object scope, String scopeClaim) throws Exception {
        JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder()
                .subject("viewer")
                .issuer("userauth")
                .issueTime(new Date())
                .expirationTime(Date.from(Instant.now().plusSeconds(600)));
        if (scope != null) {
            claims.claim(scopeClaim, scope);
        }
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims.build());
        jwt.sign(new MACSigner(secret.getBytes(StandardCharsets.UTF_8)));
        return "Bearer " + jwt.serialize();
    }

    private String readOnly() throws Exception {
        return token("viron:read", "scope");
    }

    private String legacy() throws Exception {
        return token(null, null);
    }

    /** The five read routes viron-strata polls, which the issue's acceptance names. */
    @Test
    void readOnlyTokenReadsTheViewerRoutes() throws Exception {
        when(environmentRepository.findAll()).thenReturn(List.of(new Environment(1, "world", "2026-10-24")));
        when(environmentRepository.findById(1)).thenReturn(Optional.of(new Environment(1, "world", "2026-10-24")));
        when(environmentRepository.findByName("world")).thenReturn(Optional.of(new Environment(1, "world", "2026-10-24")));
        when(entityRepository.findById(7)).thenReturn(Optional.of(new Entity(7, "walker", "2026-10-24")));

        String auth = readOnly();
        for (String path : List.of(
                "/api/v1/environments",
                "/api/v1/environments/1",
                "/api/v1/environments/name/world",
                "/api/v1/grids/environment/1",
                "/api/v1/locations/placements?environmentId=1",
                "/api/v1/entities/environment/1",
                "/api/v1/entities/7")) {
            mockMvc.perform(get(path).header("Authorization", auth))
                    .andExpect(status().isOk());
        }
    }

    /**
     * Every route that is not a GET is refused to a read-only token. The list comes from the
     * live handler mapping (debug routes included), so a write route added later is covered
     * without touching this test.
     */
    @Test
    void readOnlyTokenIsForbiddenOnEveryWriteRoute() throws Exception {
        List<String[]> writes = writeRoutes();
        assertTrue(writes.size() >= 10, "expected the write routes to be discovered, got " + writes.size());

        String auth = readOnly();
        for (String[] route : writes) {
            mockMvc.perform(request(HttpMethod.valueOf(route[0]), route[1])
                            .header("Authorization", auth)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"x\",\"numGrids\":1,\"gridSize\":1}"))
                    .andExpect(status().isForbidden())
                    .andExpect(header().string("WWW-Authenticate",
                            Matchers.startsWith("Bearer error=\"insufficient_scope\"")));
        }
        // Refused before any controller ran: nothing was created, moved or deleted.
        verifyNoInteractions(environmentFactory, entityFactory, dbInteractions);
    }

    /** Tokens without a scope claim keep full access, exactly as before #245. */
    @Test
    void tokenWithoutScopeClaimCanStillWrite() throws Exception {
        when(environmentFactory.createEnvironment(anyString(), anyInt(), anyInt(), anyInt()))
                .thenReturn(new Environment(1, "made", "2026-10-24"));

        mockMvc.perform(post("/api/v1/environments")
                        .header("Authorization", legacy())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"made\",\"numGrids\":1,\"gridSize\":2}"))
                .andExpect(status().isCreated());
    }

    /** A legacy token is not refused on any write route either (it may still 4xx on input). */
    @Test
    void tokenWithoutScopeClaimIsNotForbiddenOnAnyWriteRoute() throws Exception {
        String auth = legacy();
        for (String[] route : writeRoutes()) {
            int code = mockMvc.perform(request(HttpMethod.valueOf(route[0]), route[1])
                            .header("Authorization", auth)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"x\",\"numGrids\":1,\"gridSize\":1}"))
                    .andReturn().getResponse().getStatus();
            assertNotEquals(403, code, route[0] + " " + route[1]);
            assertNotEquals(401, code, route[0] + " " + route[1]);
        }
    }

    @Test
    void readAndWriteScopesTogetherCanWrite() throws Exception {
        when(environmentFactory.createEnvironment(anyString(), anyInt(), anyInt(), anyInt()))
                .thenReturn(new Environment(1, "made", "2026-10-24"));

        mockMvc.perform(post("/api/v1/environments")
                        .header("Authorization", token("viron:read viron:write", "scope"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"made\",\"numGrids\":1,\"gridSize\":2}"))
                .andExpect(status().isCreated());
    }

    /** {@code scp} as a JSON list is read the same way as a space-separated {@code scope}. */
    @Test
    void scpListClaimIsHonoured() throws Exception {
        String auth = token(List.of("viron:read"), "scp");
        mockMvc.perform(get("/api/v1/environments").header("Authorization", auth))
                .andExpect(status().isOk());
        mockMvc.perform(request(HttpMethod.DELETE, "/api/v1/environments/1").header("Authorization", auth))
                .andExpect(status().isForbidden());
    }

    /** An unrelated scope does not make a token read-only. */
    @Test
    void unrelatedScopeKeepsFullAccess() throws Exception {
        when(environmentFactory.createEnvironment(anyString(), anyInt(), anyInt(), anyInt()))
                .thenReturn(new Environment(1, "made", "2026-10-24"));

        mockMvc.perform(post("/api/v1/environments")
                        .header("Authorization", token("profile", "scope"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"made\",\"numGrids\":1,\"gridSize\":2}"))
                .andExpect(status().isCreated());
    }

    /** No token at all is still 401 on every write route: the scope rule never admits anonymous callers. */
    @Test
    void writeWithoutTokenIsStillUnauthorized() throws Exception {
        for (String[] route : writeRoutes()) {
            mockMvc.perform(request(HttpMethod.valueOf(route[0]), route[1])
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"x\",\"numGrids\":1,\"gridSize\":1}"))
                    .andExpect(status().isUnauthorized());
        }
        verifyNoInteractions(environmentFactory, entityFactory, dbInteractions);
    }

    private List<String[]> writeRoutes() {
        List<String[]> routes = new ArrayList<>();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> e : handlerMapping.getHandlerMethods().entrySet()) {
            RequestMappingInfo info = e.getKey();
            if (info.getPathPatternsCondition() == null) {
                continue;
            }
            for (String pattern : info.getPathPatternsCondition().getPatternValues()) {
                if (!pattern.startsWith("/api/v1/")) {
                    continue;
                }
                String path = pattern.replaceAll("\\{[^}]+}", "1");
                for (RequestMethod method : info.getMethodsCondition().getMethods()) {
                    if (method != RequestMethod.GET && method != RequestMethod.HEAD) {
                        routes.add(new String[] {method.name(), path});
                    }
                }
            }
        }
        return routes;
    }

    @Test
    void isReadOnlyNeedsTheReadScopeAndNoWriteScope() {
        assertTrue(SecurityConfig.isReadOnly(auth("SCOPE_viron:read")));
        assertFalse(SecurityConfig.isReadOnly(auth("SCOPE_viron:read", "SCOPE_viron:write")));
        assertFalse(SecurityConfig.isReadOnly(auth("SCOPE_viron:write")));
        assertFalse(SecurityConfig.isReadOnly(auth()));
        assertEquals("viron:read", SecurityConfig.READ_SCOPE);
    }

    private static org.springframework.security.core.Authentication auth(String... authorities) {
        return new org.springframework.security.authentication.TestingAuthenticationToken(
                "viewer", "n/a", authorities);
    }
}
