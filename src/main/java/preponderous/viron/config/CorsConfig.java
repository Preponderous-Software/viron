// Copyright (c) 2024 Preponderous Software
// MIT License

package preponderous.viron.config;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * A CORS allow-list for read-only browser clients (issue #244).
 *
 * <p>{@code viron.cors.allowed-origins} ({@code VIRON_CORS_ALLOWED_ORIGINS}) is a
 * comma-separated list of exact origins, such as {@code https://arcade.example.com}. It is
 * empty by default, and then no CORS configuration is registered at all: a cross-origin page
 * cannot read Viron, and a preflight is answered {@code 401} exactly as before (no CORS filter
 * is installed; see {@link SecurityConfig}).
 *
 * <p>When it is set, pages on those origins may send {@code GET} and {@code HEAD} to
 * {@code /api/v1/**} and {@code /actuator/health} with an {@code Authorization} header. The
 * preflight is answered by Spring Security's CORS filter, before authentication, so it needs
 * no token. Write methods are never allowed cross-origin, so a browser page can only read,
 * whatever token it holds; pair it with a read-only token ({@code scope: viron:read}, #245) so
 * the credential cannot write from anywhere else either. No cookies are involved
 * ({@code allowCredentials} stays false), and a wildcard origin is refused at start-up.
 */
public final class CorsConfig {

    private CorsConfig() {
    }

    /** The methods a cross-origin page may use. Reads only. */
    static final List<String> ALLOWED_METHODS = List.of("GET", "HEAD");

    /**
     * The CORS source for a non-empty allow-list. Deliberately not a bean: Spring Security
     * installs a CORS filter whenever a {@code corsConfigurationSource} bean exists, and with an
     * empty list none should be installed at all.
     */
    static CorsConfigurationSource source(List<String> origins) {
        if (origins.isEmpty()) {
            throw new IllegalArgumentException("an empty allow-list means CORS stays off");
        }
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(origins);
        config.setAllowedMethods(ALLOWED_METHODS);
        config.setAllowedHeaders(List.of("Authorization", "Accept"));
        config.setExposedHeaders(List.of("Retry-After"));
        config.setAllowCredentials(false);
        config.setMaxAge(Duration.ofMinutes(10));
        source.registerCorsConfiguration("/api/v1/**", config);
        source.registerCorsConfiguration("/actuator/health", config);
        return source;
    }

    /**
     * Splits and checks the configured list. Each entry must be a bare origin
     * ({@code scheme://host[:port]}, no path); {@code *} is refused because the list is meant
     * to name the pages that may read, not to open Viron to every site.
     */
    static List<String> parseOrigins(String raw) {
        List<String> origins = new ArrayList<>();
        if (raw == null) {
            return origins;
        }
        for (String part : raw.split(",")) {
            String origin = part.trim();
            if (origin.isEmpty()) {
                continue;
            }
            if (origin.contains("*")) {
                throw new IllegalStateException("viron.cors.allowed-origins must list exact origins; '"
                        + origin + "' contains a wildcard.");
            }
            URI uri;
            try {
                uri = URI.create(origin);
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException("viron.cors.allowed-origins: '" + origin
                        + "' is not a valid origin.", e);
            }
            boolean bare = uri.getScheme() != null && uri.getHost() != null
                    && (uri.getPath() == null || uri.getPath().isEmpty())
                    && uri.getQuery() == null && uri.getFragment() == null && uri.getUserInfo() == null;
            if (!bare || !(uri.getScheme().equals("http") || uri.getScheme().equals("https"))) {
                throw new IllegalStateException("viron.cors.allowed-origins: '" + origin
                        + "' must be scheme://host[:port], with no path, such as https://example.com.");
            }
            origins.add(origin);
        }
        return origins;
    }
}
