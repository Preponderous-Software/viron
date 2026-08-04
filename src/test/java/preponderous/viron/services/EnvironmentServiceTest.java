package preponderous.viron.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import preponderous.viron.config.ServiceConfig;
import preponderous.viron.dto.CreateEnvironmentRequest;
import preponderous.viron.dto.UpdateEnvironmentNameRequest;
import preponderous.viron.exceptions.NotFoundException;
import preponderous.viron.exceptions.ServiceException;
import preponderous.viron.models.Environment;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

public class EnvironmentServiceTest {

    private static final String BASE_URL = "http://localhost:8080/api/v1/environments";
    private static final String BY_ID_URL = BASE_URL + "/{id}";
    private static final String BY_NAME_URL = BASE_URL + "/name/{name}";
    private static final String OF_ENTITY_URL = BASE_URL + "/entity/{entityId}";
    private static final String NAME_URL = BASE_URL + "/{id}/name";

    private RestTemplate restTemplate;
    private EnvironmentService environmentService;

    @BeforeEach
    void setUp() {
        RestTemplateBuilder restTemplateBuilder = Mockito.mock(RestTemplateBuilder.class);
        restTemplate = Mockito.mock(RestTemplate.class);
        Mockito.when(restTemplateBuilder.additionalInterceptors(any(ClientHttpRequestInterceptor.class)))
                .thenReturn(restTemplateBuilder);
        Mockito.when(restTemplateBuilder.build()).thenReturn(restTemplate);

        ServiceConfig serviceConfig = new ServiceConfig();
        serviceConfig.setVironHost("http://localhost");
        serviceConfig.setVironPort(8080);

        environmentService = new EnvironmentService(restTemplateBuilder, serviceConfig);
    }

    private static HttpEntity<Void> nullRequestEntity() {
        return ArgumentMatchers.isNull();
    }

    @SuppressWarnings("rawtypes")
    private static ArgumentCaptor<HttpEntity> requestEntityCaptor() {
        return ArgumentCaptor.forClass(HttpEntity.class);
    }

    // getAllEnvironments

    @Test
    void testGetAllEnvironments_Success_ReturnsEnvironments() {
        Environment[] environments = {
                new Environment(1, "Env1", "2024-01-01"),
                new Environment(2, "Env2", "2024-01-02")
        };
        Mockito.when(restTemplate.exchange(
                        eq(BASE_URL), eq(HttpMethod.GET), nullRequestEntity(), eq(Environment[].class)))
                .thenReturn(ResponseEntity.ok(environments));

        List<Environment> result = environmentService.getAllEnvironments();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Env1");
    }

    @Test
    void testGetAllEnvironments_NullBody_ReturnsEmptyList() {
        Mockito.when(restTemplate.exchange(
                        eq(BASE_URL), eq(HttpMethod.GET), nullRequestEntity(), eq(Environment[].class)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        assertThat(environmentService.getAllEnvironments()).isEmpty();
    }

    @Test
    void testGetAllEnvironments_NonOkStatus_ReturnsEmptyList() {
        Environment[] environments = {new Environment(1, "Env1", "2024-01-01")};
        Mockito.when(restTemplate.exchange(
                        eq(BASE_URL), eq(HttpMethod.GET), nullRequestEntity(), eq(Environment[].class)))
                .thenReturn(new ResponseEntity<>(environments, HttpStatus.NO_CONTENT));

        assertThat(environmentService.getAllEnvironments()).isEmpty();
    }

    @Test
    void testGetAllEnvironments_RestTemplateThrows_WrapsInServiceException() {
        Mockito.when(restTemplate.exchange(
                        eq(BASE_URL), eq(HttpMethod.GET), nullRequestEntity(), eq(Environment[].class)))
                .thenThrow(new RestClientException("boom"));

        assertThatThrownBy(() -> environmentService.getAllEnvironments())
                .isExactlyInstanceOf(ServiceException.class)
                .hasMessage("Error getting environments");
    }

    // getEnvironmentById

    @Test
    void testGetEnvironmentById_Success_ReturnsEnvironment() {
        Environment environment = new Environment(1, "Env1", "2024-01-01");
        Mockito.when(restTemplate.exchange(
                        eq(BY_ID_URL), eq(HttpMethod.GET), nullRequestEntity(), eq(Environment.class), eq(1)))
                .thenReturn(ResponseEntity.ok(environment));

        assertThat(environmentService.getEnvironmentById(1)).isEqualTo(environment);
    }

    @Test
    void testGetEnvironmentById_NullBody_ThrowsNotFoundException() {
        Mockito.when(restTemplate.exchange(
                        eq(BY_ID_URL), eq(HttpMethod.GET), nullRequestEntity(), eq(Environment.class), eq(1)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        assertThatThrownBy(() -> environmentService.getEnvironmentById(1))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Environment not found with id: 1");
    }

    @Test
    void testGetEnvironmentById_NotFoundResponse_ThrowsNotFoundException() {
        Mockito.when(restTemplate.exchange(
                        eq(BY_ID_URL), eq(HttpMethod.GET), nullRequestEntity(), eq(Environment.class), eq(1)))
                .thenThrow(HttpClientErrorException.NotFound.class);

        assertThatThrownBy(() -> environmentService.getEnvironmentById(1))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Environment not found with id: 1");
    }

    @Test
    void testGetEnvironmentById_RestTemplateThrows_WrapsInServiceException() {
        Mockito.when(restTemplate.exchange(
                        eq(BY_ID_URL), eq(HttpMethod.GET), nullRequestEntity(), eq(Environment.class), eq(1)))
                .thenThrow(new RestClientException("boom"));

        assertThatThrownBy(() -> environmentService.getEnvironmentById(1))
                .isExactlyInstanceOf(ServiceException.class)
                .hasMessage("Error getting environment by id: 1");
    }

    // getEnvironmentByName

    @Test
    void testGetEnvironmentByName_Success_ReturnsEnvironment() {
        Environment environment = new Environment(1, "Env1", "2024-01-01");
        Mockito.when(restTemplate.exchange(
                        eq(BY_NAME_URL), eq(HttpMethod.GET), nullRequestEntity(), eq(Environment.class), eq("Env1")))
                .thenReturn(ResponseEntity.ok(environment));

        assertThat(environmentService.getEnvironmentByName("Env1")).isEqualTo(environment);
    }

    @Test
    void testGetEnvironmentByName_NotFoundResponse_ThrowsNotFoundException() {
        Mockito.when(restTemplate.exchange(
                        eq(BY_NAME_URL), eq(HttpMethod.GET), nullRequestEntity(), eq(Environment.class), eq("Missing")))
                .thenThrow(HttpClientErrorException.NotFound.class);

        assertThatThrownBy(() -> environmentService.getEnvironmentByName("Missing"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Environment not found with name: Missing");
    }

    @Test
    void testGetEnvironmentByName_RestTemplateThrows_WrapsInServiceException() {
        Mockito.when(restTemplate.exchange(
                        eq(BY_NAME_URL), eq(HttpMethod.GET), nullRequestEntity(), eq(Environment.class), eq("Env1")))
                .thenThrow(new RestClientException("boom"));

        assertThatThrownBy(() -> environmentService.getEnvironmentByName("Env1"))
                .isExactlyInstanceOf(ServiceException.class)
                .hasMessage("Error getting environment by name: Env1");
    }

    // getEnvironmentOfEntity

    @Test
    void testGetEnvironmentOfEntity_Success_ReturnsEnvironment() {
        Environment environment = new Environment(1, "Env1", "2024-01-01");
        Mockito.when(restTemplate.exchange(
                        eq(OF_ENTITY_URL), eq(HttpMethod.GET), nullRequestEntity(), eq(Environment.class), eq(7)))
                .thenReturn(ResponseEntity.ok(environment));

        assertThat(environmentService.getEnvironmentOfEntity(7)).isEqualTo(environment);
    }

    @Test
    void testGetEnvironmentOfEntity_NotFoundResponse_ThrowsNotFoundException() {
        Mockito.when(restTemplate.exchange(
                        eq(OF_ENTITY_URL), eq(HttpMethod.GET), nullRequestEntity(), eq(Environment.class), eq(7)))
                .thenThrow(HttpClientErrorException.NotFound.class);

        assertThatThrownBy(() -> environmentService.getEnvironmentOfEntity(7))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Environment not found for entity: 7");
    }

    @Test
    void testGetEnvironmentOfEntity_RestTemplateThrows_WrapsInServiceException() {
        Mockito.when(restTemplate.exchange(
                        eq(OF_ENTITY_URL), eq(HttpMethod.GET), nullRequestEntity(), eq(Environment.class), eq(7)))
                .thenThrow(new RestClientException("boom"));

        assertThatThrownBy(() -> environmentService.getEnvironmentOfEntity(7))
                .isExactlyInstanceOf(ServiceException.class)
                .hasMessage("Error getting environment of entity: 7");
    }

    // createEnvironment - square-grid overload

    @Test
    void testCreateEnvironment_SquareOverload_SendsGridSizeOnly() {
        Environment created = new Environment(1, "Env1", "2024-01-01");
        Mockito.when(restTemplate.exchange(
                        eq(BASE_URL), eq(HttpMethod.POST), any(HttpEntity.class), eq(Environment.class)))
                .thenReturn(ResponseEntity.ok(created));

        Environment result = environmentService.createEnvironment("Env1", 2, 5);

        assertThat(result).isEqualTo(created);
        ArgumentCaptor<HttpEntity> captor = requestEntityCaptor();
        Mockito.verify(restTemplate).exchange(
                eq(BASE_URL), eq(HttpMethod.POST), captor.capture(), eq(Environment.class));
        CreateEnvironmentRequest request = (CreateEnvironmentRequest) captor.getValue().getBody();
        assertThat(request).isNotNull();
        assertThat(request.getName()).isEqualTo("Env1");
        assertThat(request.getNumGrids()).isEqualTo(2);
        assertThat(request.getGridSize()).isEqualTo(5);
        assertThat(request.getNumRows()).isNull();
        assertThat(request.getNumColumns()).isNull();
    }

    // createEnvironment - rows/columns overload

    @Test
    void testCreateEnvironment_DimensionsOverload_SendsRowsAndColumnsWithoutGridSize() {
        Environment created = new Environment(1, "Env1", "2024-01-01");
        Mockito.when(restTemplate.exchange(
                        eq(BASE_URL), eq(HttpMethod.POST), any(HttpEntity.class), eq(Environment.class)))
                .thenReturn(ResponseEntity.ok(created));

        Environment result = environmentService.createEnvironment("Env1", 2, 3, 4);

        assertThat(result).isEqualTo(created);
        ArgumentCaptor<HttpEntity> captor = requestEntityCaptor();
        Mockito.verify(restTemplate).exchange(
                eq(BASE_URL), eq(HttpMethod.POST), captor.capture(), eq(Environment.class));
        CreateEnvironmentRequest request = (CreateEnvironmentRequest) captor.getValue().getBody();
        assertThat(request).isNotNull();
        assertThat(request.getName()).isEqualTo("Env1");
        assertThat(request.getNumGrids()).isEqualTo(2);
        assertThat(request.getGridSize()).isNull();
        assertThat(request.getNumRows()).isEqualTo(3);
        assertThat(request.getNumColumns()).isEqualTo(4);
    }

    @Test
    void testCreateEnvironment_NullBody_ThrowsServiceException() {
        Mockito.when(restTemplate.exchange(
                        eq(BASE_URL), eq(HttpMethod.POST), any(HttpEntity.class), eq(Environment.class)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        assertThatThrownBy(() -> environmentService.createEnvironment("Env1", 2, 5))
                .isExactlyInstanceOf(ServiceException.class)
                .hasMessage("Error creating environment");
    }

    @Test
    void testCreateEnvironment_NonSuccessStatus_ThrowsServiceException() {
        Environment created = new Environment(1, "Env1", "2024-01-01");
        Mockito.when(restTemplate.exchange(
                        eq(BASE_URL), eq(HttpMethod.POST), any(HttpEntity.class), eq(Environment.class)))
                .thenReturn(new ResponseEntity<>(created, HttpStatus.BAD_REQUEST));

        assertThatThrownBy(() -> environmentService.createEnvironment("Env1", 2, 5))
                .isExactlyInstanceOf(ServiceException.class)
                .hasMessage("Error creating environment");
    }

    @Test
    void testCreateEnvironment_RestTemplateThrows_WrapsInServiceException() {
        Mockito.when(restTemplate.exchange(
                        eq(BASE_URL), eq(HttpMethod.POST), any(HttpEntity.class), eq(Environment.class)))
                .thenThrow(new RestClientException("boom"));

        assertThatThrownBy(() -> environmentService.createEnvironment("Env1", 2, 5))
                .isExactlyInstanceOf(ServiceException.class)
                .hasMessage("Error creating environment");
    }

    // deleteEnvironment

    @Test
    void testDeleteEnvironment_Success_ReturnsTrue() {
        Mockito.when(restTemplate.exchange(
                        eq(BY_ID_URL), eq(HttpMethod.DELETE), nullRequestEntity(), eq(Void.class), eq(1)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        assertThat(environmentService.deleteEnvironment(1)).isTrue();
    }

    @Test
    void testDeleteEnvironment_NotFoundStatus_ThrowsNotFoundException() {
        Mockito.when(restTemplate.exchange(
                        eq(BY_ID_URL), eq(HttpMethod.DELETE), nullRequestEntity(), eq(Void.class), eq(1)))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> environmentService.deleteEnvironment(1))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Environment not found with id: 1");
    }

    @Test
    void testDeleteEnvironment_NotFoundResponse_ThrowsNotFoundException() {
        Mockito.when(restTemplate.exchange(
                        eq(BY_ID_URL), eq(HttpMethod.DELETE), nullRequestEntity(), eq(Void.class), eq(1)))
                .thenThrow(HttpClientErrorException.NotFound.class);

        assertThatThrownBy(() -> environmentService.deleteEnvironment(1))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Environment not found with id: 1");
    }

    @Test
    void testDeleteEnvironment_NonSuccessStatus_ThrowsServiceException() {
        Mockito.when(restTemplate.exchange(
                        eq(BY_ID_URL), eq(HttpMethod.DELETE), nullRequestEntity(), eq(Void.class), eq(1)))
                .thenReturn(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> environmentService.deleteEnvironment(1))
                .isExactlyInstanceOf(ServiceException.class)
                .hasMessage("Error deleting environment");
    }

    @Test
    void testDeleteEnvironment_RestTemplateThrows_WrapsInServiceException() {
        Mockito.when(restTemplate.exchange(
                        eq(BY_ID_URL), eq(HttpMethod.DELETE), nullRequestEntity(), eq(Void.class), eq(1)))
                .thenThrow(new RestClientException("boom"));

        assertThatThrownBy(() -> environmentService.deleteEnvironment(1))
                .isExactlyInstanceOf(ServiceException.class)
                .hasMessage("Error deleting environment");
    }

    // updateEnvironmentName

    @Test
    void testUpdateEnvironmentName_Success_PatchesWithNameBody() {
        Mockito.when(restTemplate.exchange(
                        eq(NAME_URL), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(Void.class), eq(1)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        assertThat(environmentService.updateEnvironmentName(1, "Renamed")).isTrue();

        ArgumentCaptor<HttpEntity> captor = requestEntityCaptor();
        Mockito.verify(restTemplate).exchange(
                eq(NAME_URL), eq(HttpMethod.PATCH), captor.capture(), eq(Void.class), eq(1));
        UpdateEnvironmentNameRequest request = (UpdateEnvironmentNameRequest) captor.getValue().getBody();
        assertThat(request).isNotNull();
        assertThat(request.getName()).isEqualTo("Renamed");
    }

    @Test
    void testUpdateEnvironmentName_NotFoundStatus_ThrowsNotFoundException() {
        Mockito.when(restTemplate.exchange(
                        eq(NAME_URL), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(Void.class), eq(1)))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> environmentService.updateEnvironmentName(1, "Renamed"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Environment not found with id: 1");
    }

    @Test
    void testUpdateEnvironmentName_NotFoundResponse_ThrowsNotFoundException() {
        Mockito.when(restTemplate.exchange(
                        eq(NAME_URL), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(Void.class), eq(1)))
                .thenThrow(HttpClientErrorException.NotFound.class);

        assertThatThrownBy(() -> environmentService.updateEnvironmentName(1, "Renamed"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Environment not found with id: 1");
    }

    @Test
    void testUpdateEnvironmentName_NonSuccessStatus_ThrowsServiceException() {
        Mockito.when(restTemplate.exchange(
                        eq(NAME_URL), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(Void.class), eq(1)))
                .thenReturn(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> environmentService.updateEnvironmentName(1, "Renamed"))
                .isExactlyInstanceOf(ServiceException.class)
                .hasMessage("Error updating environment name");
    }

    @Test
    void testUpdateEnvironmentName_RestTemplateThrows_WrapsInServiceException() {
        Mockito.when(restTemplate.exchange(
                        eq(NAME_URL), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(Void.class), eq(1)))
                .thenThrow(new RestClientException("boom"));

        assertThatThrownBy(() -> environmentService.updateEnvironmentName(1, "Renamed"))
                .isExactlyInstanceOf(ServiceException.class)
                .hasMessage("Error updating environment name");
    }
}
