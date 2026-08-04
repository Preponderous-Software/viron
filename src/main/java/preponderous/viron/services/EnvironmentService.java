// Copyright (c) 2024 Preponderous Software
// MIT License

package preponderous.viron.services;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;
import preponderous.viron.config.AuthTokenInterceptor;
import preponderous.viron.config.ServiceConfig;
import preponderous.viron.dto.CreateEnvironmentRequest;
import preponderous.viron.dto.UpdateEnvironmentNameRequest;
import preponderous.viron.exceptions.NotFoundException;
import preponderous.viron.exceptions.ServiceException;
import preponderous.viron.models.Environment;

@Service
@Slf4j
public class EnvironmentService {
    private final RestTemplateBuilder restTemplateBuilder;
    private final ServiceConfig serviceConfig;
    private final String baseUrl;

    @Autowired
    public EnvironmentService(RestTemplateBuilder restTemplateBuilder, ServiceConfig serviceConfig) {
        this.restTemplateBuilder = restTemplateBuilder.additionalInterceptors(new AuthTokenInterceptor(serviceConfig));
        this.serviceConfig = serviceConfig;
        this.baseUrl = this.serviceConfig.getVironHost() + ":" + serviceConfig.getVironPort() + "/api/v1/environments";
    }

    public List<Environment> getAllEnvironments() {
        try {
            RestTemplate restTemplate = restTemplateBuilder.build();
            ResponseEntity<Environment[]> response = restTemplate.exchange(baseUrl, HttpMethod.GET, null, Environment[].class);
            return response.getStatusCode() == HttpStatus.OK && response.getBody() != null
                    ? Arrays.asList(response.getBody())
                    : Collections.emptyList();
        } catch (Exception e) {
            log.error("Error getting environments: {}", e.getMessage());
            throw new ServiceException("Error getting environments", e);
        }
    }

    public Environment getEnvironmentById(int id) {
        try {
            RestTemplate restTemplate = restTemplateBuilder.build();
            ResponseEntity<Environment> response = restTemplate.exchange(baseUrl + "/{id}", HttpMethod.GET, null, Environment.class, id);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
            throw new NotFoundException("Environment not found with id: " + id);
        } catch (HttpClientErrorException.NotFound e) {
            throw new NotFoundException("Environment not found with id: " + id);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error getting environment by id {}: {}", id, e.getMessage());
            throw new ServiceException("Error getting environment by id: " + id, e);
        }
    }

    public Environment getEnvironmentByName(String name) {
        try {
            RestTemplate restTemplate = restTemplateBuilder.build();
            ResponseEntity<Environment> response = restTemplate.exchange(baseUrl + "/name/{name}", HttpMethod.GET, null, Environment.class, name);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
            throw new NotFoundException("Environment not found with name: " + name);
        } catch (HttpClientErrorException.NotFound e) {
            throw new NotFoundException("Environment not found with name: " + name);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error getting environment by name {}: {}", name, e.getMessage());
            throw new ServiceException("Error getting environment by name: " + name, e);
        }
    }

    public Environment getEnvironmentOfEntity(int entityId) {
        try {
            RestTemplate restTemplate = restTemplateBuilder.build();
            ResponseEntity<Environment> response = restTemplate.exchange(baseUrl + "/entity/{entityId}", HttpMethod.GET, null, Environment.class, entityId);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
            throw new NotFoundException("Environment not found for entity: " + entityId);
        } catch (HttpClientErrorException.NotFound e) {
            throw new NotFoundException("Environment not found for entity: " + entityId);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error getting environment of entity {}: {}", entityId, e.getMessage());
            throw new ServiceException("Error getting environment of entity: " + entityId, e);
        }
    }

    /**
     * Creates an environment whose grids are square.
     *
     * @param gridSize rows and columns of each grid
     */
    public Environment createEnvironment(String name, int numGrids, int gridSize) {
        return createEnvironment(new CreateEnvironmentRequest(name, numGrids, gridSize));
    }

    /**
     * Creates an environment whose grids have independently sized dimensions.
     *
     * @param numRows    rows in each grid
     * @param numColumns columns in each grid
     */
    public Environment createEnvironment(String name, int numGrids, int numRows, int numColumns) {
        return createEnvironment(new CreateEnvironmentRequest(name, numGrids, null, numRows, numColumns));
    }

    private Environment createEnvironment(CreateEnvironmentRequest request) {
        try {
            RestTemplate restTemplate = restTemplateBuilder.build();
            ResponseEntity<Environment> response = restTemplate.exchange(
                    baseUrl,
                    HttpMethod.POST,
                    new HttpEntity<>(request),
                    Environment.class
            );
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            throw new ServiceException("Error creating environment");
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating environment {}: {}", request.getName(), e.getMessage());
            throw new ServiceException("Error creating environment", e);
        }
    }

    public boolean deleteEnvironment(int id) {
        try {
            RestTemplate restTemplate = restTemplateBuilder.build();
            ResponseEntity<Void> response = restTemplate.exchange(baseUrl + "/{id}", HttpMethod.DELETE, null, Void.class, id);
            if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new NotFoundException("Environment not found with id: " + id);
            }
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new ServiceException("Error deleting environment");
            }
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            throw new NotFoundException("Environment not found with id: " + id);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deleting environment {}: {}", id, e.getMessage());
            throw new ServiceException("Error deleting environment", e);
        }
    }

    public boolean updateEnvironmentName(int id, String name) {
        try {
            RestTemplate restTemplate = restTemplateBuilder.build();
            ResponseEntity<Void> response = restTemplate.exchange(
                    baseUrl + "/{id}/name",
                    HttpMethod.PATCH,
                    new HttpEntity<>(new UpdateEnvironmentNameRequest(name)),
                    Void.class,
                    id
            );
            if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new NotFoundException("Environment not found with id: " + id);
            }
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new ServiceException("Error updating environment name");
            }
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            throw new NotFoundException("Environment not found with id: " + id);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating name for environment {}: {}", id, e.getMessage());
            throw new ServiceException("Error updating environment name", e);
        }
    }
}
