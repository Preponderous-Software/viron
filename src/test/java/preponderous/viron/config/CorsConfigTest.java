// Copyright (c) 2024 Preponderous Software
// MIT License

package preponderous.viron.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/** Parsing and checking of {@code viron.cors.allowed-origins} (issue #244). */
class CorsConfigTest {

    @Test
    void emptyOrBlankMeansClosed() {
        assertTrue(CorsConfig.parseOrigins("").isEmpty());
        assertTrue(CorsConfig.parseOrigins(" , ").isEmpty());
        assertTrue(CorsConfig.parseOrigins(null).isEmpty());
    }

    @Test
    void entriesAreTrimmed() {
        assertEquals(List.of("https://a.example", "http://localhost:8000"),
                CorsConfig.parseOrigins(" https://a.example ,http://localhost:8000"));
    }

    @Test
    void wildcardIsRefused() {
        assertThrows(IllegalStateException.class, () -> CorsConfig.parseOrigins("*"));
        assertThrows(IllegalStateException.class, () -> CorsConfig.parseOrigins("https://*.example"));
    }

    @Test
    void anythingButABareOriginIsRefused() {
        assertThrows(IllegalStateException.class, () -> CorsConfig.parseOrigins("https://a.example/play"));
        assertThrows(IllegalStateException.class, () -> CorsConfig.parseOrigins("a.example"));
        assertThrows(IllegalStateException.class, () -> CorsConfig.parseOrigins("ftp://a.example"));
        assertThrows(IllegalStateException.class, () -> CorsConfig.parseOrigins("https://a.example?x=1"));
    }

    @Test
    void sourceRefusesAnEmptyList() {
        assertThrows(IllegalArgumentException.class, () -> CorsConfig.source(List.of()));
    }
}
