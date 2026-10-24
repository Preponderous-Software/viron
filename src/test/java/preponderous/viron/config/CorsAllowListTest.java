// Copyright (c) 2024 Preponderous Software
// MIT License

package preponderous.viron.config;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import preponderous.viron.database.DbInteractions;
import preponderous.viron.factories.EnvironmentFactory;
import preponderous.viron.repositories.EnvironmentRepository;

/**
 * The CORS allow-list (issue #244) when it is set: listed origins may read, preflights need no
 * token, and no write method is ever allowed cross-origin, whatever the token.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext
@TestPropertySource(properties =
        "viron.cors.allowed-origins= https://arcade.example.test , http://localhost:8000 ")
class CorsAllowListTest {

    private static final String ALLOWED = "https://arcade.example.test";
    private static final String OTHER = "https://evil.example.test";

    @Autowired
    private MockMvc mockMvc;

    @Value("${app.jwt.secret}")
    private String secret;

    @MockBean
    private EnvironmentRepository environmentRepository;

    @MockBean
    private EnvironmentFactory environmentFactory;

    @MockBean
    private DbInteractions dbInteractions;

    private String fullToken() throws Exception {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject("driver").issuer("userauth")
                .expirationTime(Date.from(Instant.now().plusSeconds(600))).build();
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        jwt.sign(new MACSigner(secret.getBytes(StandardCharsets.UTF_8)));
        return "Bearer " + jwt.serialize();
    }

    @Test
    void preflightFromAllowedOriginNeedsNoTokenAndAllowsReadsOnly() throws Exception {
        mockMvc.perform(options("/api/v1/locations/placements?environmentId=1")
                        .header("Origin", ALLOWED)
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", ALLOWED))
                .andExpect(header().string("Access-Control-Allow-Methods", "GET,HEAD"))
                .andExpect(header().string("Access-Control-Allow-Headers", containsString("authorization")))
                .andExpect(header().string("Access-Control-Max-Age", "600"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Credentials"));
    }

    @Test
    void secondListedOriginIsAllowedToo() throws Exception {
        mockMvc.perform(options("/api/v1/environments")
                        .header("Origin", "http://localhost:8000")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:8000"));
    }

    @Test
    void preflightForAWriteIsRefused() throws Exception {
        for (String method : new String[] {"POST", "PUT", "PATCH", "DELETE"}) {
            mockMvc.perform(options("/api/v1/environments/1")
                            .header("Origin", ALLOWED)
                            .header("Access-Control-Request-Method", method))
                    .andExpect(status().isForbidden())
                    .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
        }
    }

    @Test
    void preflightFromAnUnlistedOriginIsRefused() throws Exception {
        mockMvc.perform(options("/api/v1/environments")
                        .header("Origin", OTHER)
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    void crossOriginReadCarriesTheAllowOriginHeader() throws Exception {
        mockMvc.perform(get("/api/v1/environments")
                        .header("Origin", ALLOWED)
                        .header("Authorization", fullToken()))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", ALLOWED))
                .andExpect(header().string("Access-Control-Expose-Headers", containsString("Retry-After")));
    }

    @Test
    void crossOriginReadStillNeedsAToken() throws Exception {
        mockMvc.perform(get("/api/v1/environments").header("Origin", ALLOWED))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void crossOriginWriteIsRefusedEvenWithAFullToken() throws Exception {
        mockMvc.perform(post("/api/v1/environments")
                        .header("Origin", ALLOWED)
                        .header("Authorization", fullToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"x\",\"numGrids\":1,\"gridSize\":1}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/v1/environments/1")
                        .header("Origin", ALLOWED)
                        .header("Authorization", fullToken()))
                .andExpect(status().isForbidden());
        verifyNoInteractions(environmentFactory, dbInteractions);
    }

    @Test
    void sameOriginOrNonBrowserWritesAreUnaffected() throws Exception {
        // No Origin header (curl, the Python client, a driver): CORS does not apply.
        mockMvc.perform(delete("/api/v1/environments/1").header("Authorization", fullToken()))
                .andExpect(status().is(not(403)));
    }

    @Test
    void healthPreflightIsAllowedForLivenessChecks() throws Exception {
        mockMvc.perform(options("/actuator/health")
                        .header("Origin", ALLOWED)
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", ALLOWED));
    }
}
