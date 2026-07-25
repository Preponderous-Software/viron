package preponderous.viron.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import preponderous.viron.config.DbConfig;
import preponderous.viron.database.DbInteractions;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Verifies the default-off gating of {@link DebugController} (issue #156). No
 * {@code viron.debug.enabled} property is set here, so the controller must not be
 * registered and none of its endpoints may be mapped.
 */
@SpringBootTest
@DirtiesContext
class DebugControllerDisabledTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @MockBean
    private DbInteractions dbInteractions;

    @MockBean
    private DbConfig dbConfig;

    @Test
    void debugControllerBeanIsNotRegisteredByDefault() {
        assertFalse(applicationContext.containsBean("debugController"),
                "DebugController must not be registered unless viron.debug.enabled=true");
    }

    @Test
    void noDebugEndpointsAreMappedByDefault() {
        boolean anyDebugMapping = requestMappingHandlerMapping.getHandlerMethods().keySet().stream()
                .flatMap(mapping -> mapping.getPatternValues().stream())
                .anyMatch(pattern -> pattern.startsWith("/api/v1/debug"));

        assertFalse(anyDebugMapping,
                "No /api/v1/debug endpoint may be mapped unless viron.debug.enabled=true");
    }
}
