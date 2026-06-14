// Copyright (c) 2024 Preponderous Software
// MIT License

package preponderous.viron.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import preponderous.viron.database.DbInteractions;

/**
 * Verifies the JWT authentication added for issue #149: API endpoints reject
 * unauthenticated requests, while actuator health stays public. (Authenticated
 * access is exercised by the {@code @WithMockUser} controller tests.)
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DbInteractions dbInteractions;

    @Test
    void apiRequestWithoutTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/environments"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void actuatorHealthIsPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }
}
