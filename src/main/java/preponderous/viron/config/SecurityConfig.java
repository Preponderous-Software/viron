// Copyright (c) 2024 Preponderous Software
// MIT License

package preponderous.viron.config;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthenticatedAuthorizationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

/**
 * Secures the Viron REST API with JWT bearer-token authentication (issue #149).
 *
 * <p>Validates tokens issued by the <strong>UserAuth</strong> service: HMAC-signed
 * (HS256/HS384/HS512) with a shared secret supplied via the {@code JWT_SECRET}
 * environment variable, and an {@code iss} claim matching {@code app.jwt.issuer}
 * (default {@code userauth}). The decoder configuration mirrors UserAuth's own, so a
 * token minted there validates here without a network call.
 *
 * <p>All endpoints require a valid bearer token except actuator health and the
 * OpenAPI/Swagger documentation. Sessions are stateless.
 *
 * <p><strong>Read-only tokens (issue #245).</strong> A token whose {@code scope} (or
 * {@code scp}) claim grants {@value #READ_SCOPE} and does not grant {@value #WRITE_SCOPE}
 * is read-only: it may call {@code GET} and {@code HEAD} routes, and every other method is
 * answered {@code 403 Forbidden}. Every other valid token, including every token without a
 * {@code scope} claim (which is what UserAuth issues today), keeps full access, so existing
 * tokens behave exactly as before.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** The scope that marks a token as a reader. Alone, it makes the token read-only. */
    public static final String READ_SCOPE = "viron:read";

    /** The scope that lets a token carrying {@link #READ_SCOPE} write as well. */
    public static final String WRITE_SCOPE = "viron:write";

    private static final String READ_AUTHORITY = "SCOPE_" + READ_SCOPE;
    private static final String WRITE_AUTHORITY = "SCOPE_" + WRITE_SCOPE;

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            @Value("${viron.cors.allowed-origins:}") String allowedOrigins) throws Exception {
        List<String> corsOrigins = CorsConfig.parseOrigins(allowedOrigins);
        http
                .csrf(csrf -> csrf.disable())
                // CORS (#244): closed unless viron.cors.allowed-origins is set, and then reads
                // only. Preflights are answered by the CORS filter, before authentication, so
                // they need no token.
                .cors(cors -> {
                    if (corsOrigins.isEmpty()) {
                        cors.disable();
                    } else {
                        cors.configurationSource(CorsConfig.source(corsOrigins));
                    }
                })
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/health/**")
                        .permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                        .permitAll()
                        // Reads are open to every valid token, read-only ones included.
                        .requestMatchers(HttpMethod.GET, "/**").authenticated()
                        .requestMatchers(HttpMethod.HEAD, "/**").authenticated()
                        // Everything else writes, and a read-only token is refused (#245).
                        .anyRequest().access(SecurityConfig::notReadOnly))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }

    private static final AuthenticatedAuthorizationManager<RequestAuthorizationContext> AUTHENTICATED =
            AuthenticatedAuthorizationManager.authenticated();

    /**
     * Grants a non-read request to any authenticated caller except a read-only token. The
     * authenticated check is Spring's own, so an anonymous request is still refused (and
     * answered {@code 401}, not {@code 403}).
     */
    static AuthorizationDecision notReadOnly(
            Supplier<Authentication> authentication, RequestAuthorizationContext context) {
        AuthorizationDecision authenticated = AUTHENTICATED.check(authentication, context);
        if (authenticated == null || !authenticated.isGranted()) {
            return new AuthorizationDecision(false);
        }
        return new AuthorizationDecision(!isReadOnly(authentication.get()));
    }

    /**
     * Whether a caller holds a read-only token: one granted {@value #READ_SCOPE} and not
     * {@value #WRITE_SCOPE}. Spring maps a JWT's {@code scope}/{@code scp} claim to
     * {@code SCOPE_}-prefixed authorities, so this reads the authorities only.
     */
    static boolean isReadOnly(Authentication auth) {
        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
        if (authorities == null) {
            return false;
        }
        Set<String> names = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        return names.contains(READ_AUTHORITY) && !names.contains(WRITE_AUTHORITY);
    }

    /**
     * Builds a decoder for UserAuth's HMAC-signed JWTs. Mirrors UserAuth's
     * {@code JwtConfig}: same secret-to-key derivation, the same MAC algorithm, and
     * rejection of any token whose issuer does not match the configured value.
     */
    @Bean
    JwtDecoder jwtDecoder(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.algorithm:HS256}") String algorithm,
            @Value("${app.jwt.issuer:userauth}") String issuer) {
        MacAlgorithm macAlgorithm = MacAlgorithm.from(algorithm);
        if (macAlgorithm == null) {
            throw new IllegalStateException(
                    "Unsupported app.jwt.algorithm '" + algorithm
                            + "'. Supported HMAC algorithms are: HS256, HS384, HS512.");
        }
        // MacAlgorithm "HS256" -> JCA name "HmacSHA256"
        String jcaName = "HmacSHA" + macAlgorithm.getName().substring(2);
        SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), jcaName);
        NimbusJwtDecoder decoder =
                NimbusJwtDecoder.withSecretKey(key).macAlgorithm(macAlgorithm).build();
        // Validate the issuer in addition to the default validators (signature, expiry).
        OAuth2TokenValidator<Jwt> validator = JwtValidators.createDefaultWithIssuer(issuer);
        decoder.setJwtValidator(validator);
        return decoder;
    }
}
