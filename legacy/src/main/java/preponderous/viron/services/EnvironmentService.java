// Copyright (c) 2024 Preponderous Software
// MIT License

package preponderous.viron.services;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import preponderous.viron.config.ServiceConfig;
import preponderous.viron.exceptions.ServiceException;
import preponderous.viron.models.Environment;

@Service
public class EnvironmentService {
    private final RestTemplateBuilder restTemplateBuilder;
    private final ServiceConfig serviceConfig;
    private final String baseUrl;

    @Autowired
    public EnvironmentService(RestTemplateBuilder restTemplateBuilder, ServiceConfig serviceConfig) {
        this.restTemplateBuilder = restTemplateBuilder;
        this.serviceConfig = serviceConfig;
        this.baseUrl = this.serviceConfig.getVironHost() + ":" + serviceConfig.getVironPort() + "/api/v1/environments";
    }

    public List<Environment> getAllEnvironments() {
        RestTemplate restTemplate = restTemplateBuilder.build();
        ResponseEntity<Environment[]> response = restTemplate.exchange(baseUrl, HttpMethod.GET, null, Environment[].class);
        if (response.getStatusCode().isError()) {
            throw new ServiceException("Error getting environments");
        }
        return Arrays.asList(response.getBody());
    }

    public Environment getEnvironmentById(int id) {
        RestTemplate restTemplate = restTemplateBuilder.build();
        ResponseEntity<Environment> response = restTemplate.exchange(baseUrl + "/{id}", HttpMethod.GET, null, Environment.class, id);
        if (response.getStatusCode().isError()) {
            throw new ServiceException("Error getting environment by id");
        }
        return response.getBody();
    }

    public Environment getEnvironmentByName(String name) {
        RestTemplate restTemplate = restTemplateBuilder.build();
        ResponseEntity<Environment> response = restTemplate.exchange(baseUrl + "/name/{name}", HttpMethod.GET, null, Environment.class, name);
        if (response.getStatusCode().isError()) {
            throw new ServiceException("Error getting environment by name");
        }
        return response.getBody();
    }

    public Environment getEnvironmentOfEntity(int entityId) {
        RestTemplate restTemplate = restTemplateBuilder.build();
        ResponseEntity<Environment> response = restTemplate.exchange(baseUrl + "/entity/{entityId}", HttpMethod.GET, null, Environment.class, entityId);
        if (response.getStatusCode().isError()) {
            throw new ServiceException("Error getting environment of entity");
        }
        return response.getBody();
    }

    public Environment createEnvironment(String name, int numGrids, int gridSize) {
        RestTemplate restTemplate = restTemplateBuilder.build();
        ResponseEntity<Environment> response = restTemplate.exchange(
                baseUrl + "/{name}/{numGrids}/{gridSize}",
                HttpMethod.POST,
                null,
                Environment.class,
                name,
                numGrids,
                gridSize
        );
        if (response.getStatusCode().isError()) {
            throw new ServiceException("Error creating environment");
        }
        return response.getBody();
    }

    public boolean deleteEnvironment(int id) {
        RestTemplate restTemplate = restTemplateBuilder.build();
        ResponseEntity<Void> response = restTemplate.exchange(baseUrl + "/{id}", HttpMethod.DELETE, null, Void.class, id);
        if (response.getStatusCode().isError()) {
            throw new ServiceException("Error deleting environment");
        }
        return response.getStatusCode().is2xxSuccessful();
    }

    public boolean updateEnvironmentName(int id, String name) {
        RestTemplate restTemplate = restTemplateBuilder.build();
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/{id}/name/{name}",
                HttpMethod.PATCH,
                null,
                Void.class,
                id,
                name
        );
        if (response.getStatusCode().isError()) {
            throw new ServiceException("Error updating environment name");
        }
        return response.getStatusCode().is2xxSuccessful();
    }
}