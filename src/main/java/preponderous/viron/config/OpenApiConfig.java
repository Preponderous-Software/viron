package preponderous.viron.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for OpenAPI documentation.
 *
 * <p>This configuration sets up Swagger/OpenAPI documentation for the Viron API, providing
 * interactive API documentation accessible via the Swagger UI.
 */
@Configuration
public class OpenApiConfig {

  /**
   * Creates the OpenAPI configuration bean.
   *
   * @return configured OpenAPI instance with application metadata
   */
  @Bean
  public OpenAPI vironOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Viron API")
                .description(
                    "Foundational spatial simulation service - the bedrock on which worlds are"
                        + " built. Manages environments, grids, locations, and entities through a"
                        + " clean REST API.")
                .version("0.6.0-SNAPSHOT")
                .contact(
                    new Contact()
                        .name("Preponderous Software")
                        .url("https://github.com/Preponderous-Software/Viron")
                        .email("support@preponderoussoftware.com"))
                .license(
                    new License().name("MIT License").url("https://opensource.org/licenses/MIT")));
  }
}
