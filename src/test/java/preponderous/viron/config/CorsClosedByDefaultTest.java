// Copyright (c) 2024 Preponderous Software
// MIT License

package preponderous.viron.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import preponderous.viron.database.DbInteractions;

/**
 * With {@code viron.cors.allowed-origins} unset (the default), behaviour is what it was before
 * issue #244: a preflight is answered 401 with no {@code Access-Control-*} headers, and a
 * cross-origin read gets no allow-origin header.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CorsClosedByDefaultTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DbInteractions dbInteractions;

    @Test
    void preflightIsUnauthorizedWithNoCorsHeaders() throws Exception {
        mockMvc.perform(options("/api/v1/environments")
                        .header("Origin", "http://example.test")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Methods"));
    }

    @Test
    @WithMockUser
    void crossOriginReadGetsNoAllowOriginHeader() throws Exception {
        mockMvc.perform(get("/api/v1/environments").header("Origin", "http://example.test"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }
}
