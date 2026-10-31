package preponderous.viron.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    /** Name of the bearer scheme; matches {@code components.securitySchemes} in docs/openapi/viron-api.json. */
    public static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // Mirrors info in docs/openapi/viron-api.json, which is what Swagger UI should show.
                .info(new Info()
                        .title("Viron API")
                        .version("1.0.0")
                        .description("API specification for the Viron spatial simulation core using "
                                + "DTO-based request/response objects."))
                // Every API route requires a UserAuth-issued JWT (see SecurityConfig), so declare it
                // once globally; this is also what gives Swagger UI its Authorize control.
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
    }
}
