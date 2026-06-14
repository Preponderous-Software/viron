// Copyright (c) 2024 Preponderous Software
// MIT License

package preponderous.viron.config;

import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

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
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/health/**")
                        .permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                        .permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
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
