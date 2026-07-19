package preponderous.viron.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestConfig {

    @Bean
    public RestTemplate restTemplate(ServiceConfig serviceConfig) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(new AuthTokenInterceptor(serviceConfig));
        return restTemplate;
    }
}