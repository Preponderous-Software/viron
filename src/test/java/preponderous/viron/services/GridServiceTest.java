package preponderous.viron.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import preponderous.viron.config.ServiceConfig;
import preponderous.viron.exceptions.ServiceException;
import preponderous.viron.models.Grid;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

public class GridServiceTest {

    private static final String BASE_URL = "http://localhost:8080/api/v1/grids";

    private RestTemplate restTemplate;
    private GridService gridService;

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

        gridService = new GridService(restTemplateBuilder, serviceConfig);
    }

    @Test
    void testGetAllGrids_Success_ReturnsGrids() {
        Grid[] grids = {new Grid(1, 5, 5, "Grid1"), new Grid(2, 3, 3, "Grid2")};
        Mockito.when(restTemplate.getForEntity(eq(BASE_URL), eq(Grid[].class)))
                .thenReturn(ResponseEntity.ok(grids));

        List<Grid> result = gridService.getAllGrids();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Grid1");
    }

    @Test
    void testGetAllGrids_RestTemplateThrows_WrapsInServiceException() {
        Mockito.when(restTemplate.getForEntity(eq(BASE_URL), eq(Grid[].class)))
                .thenThrow(new RestClientException("boom"));

        assertThatThrownBy(() -> gridService.getAllGrids())
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Failed to fetch all grids");
    }

    @Test
    void testGetGridById_Found_ReturnsOptionalOfGrid() {
        Grid grid = new Grid(1, 5, 5, "Grid1");
        Mockito.when(restTemplate.getForEntity(eq(BASE_URL + "/{id}"), eq(Grid.class), eq(1)))
                .thenReturn(ResponseEntity.ok(grid));

        Optional<Grid> result = gridService.getGridById(1);

        assertThat(result).contains(grid);
    }

    @Test
    void testGetGridById_NotFound_ReturnsEmptyOptional() {
        Mockito.when(restTemplate.getForEntity(eq(BASE_URL + "/{id}"), eq(Grid.class), eq(1)))
                .thenThrow(HttpClientErrorException.NotFound.class);

        Optional<Grid> result = gridService.getGridById(1);

        assertThat(result).isEmpty();
    }

    @Test
    void testGetGridById_RestTemplateThrows_WrapsInServiceException() {
        Mockito.when(restTemplate.getForEntity(eq(BASE_URL + "/{id}"), eq(Grid.class), eq(1)))
                .thenThrow(new RestClientException("boom"));

        assertThatThrownBy(() -> gridService.getGridById(1))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Failed to fetch grid by id: 1");
    }

    @Test
    void testGetGridsInEnvironment_Success_ReturnsGrids() {
        Grid[] grids = {new Grid(1, 5, 5, "Grid1")};
        Mockito.when(restTemplate.getForEntity(eq(BASE_URL + "/environment/{environmentId}"), eq(Grid[].class), eq(3)))
                .thenReturn(ResponseEntity.ok(grids));

        List<Grid> result = gridService.getGridsInEnvironment(3);

        assertThat(result).hasSize(1);
    }

    @Test
    void testGetGridsInEnvironment_RestTemplateThrows_WrapsInServiceException() {
        Mockito.when(restTemplate.getForEntity(eq(BASE_URL + "/environment/{environmentId}"), eq(Grid[].class), eq(3)))
                .thenThrow(new RestClientException("boom"));

        assertThatThrownBy(() -> gridService.getGridsInEnvironment(3))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Failed to fetch grids in environment: 3");
    }

    @Test
    void testGetGridOfEntity_Found_ReturnsOptionalOfGrid() {
        Grid grid = new Grid(1, 5, 5, "Grid1");
        Mockito.when(restTemplate.getForEntity(eq(BASE_URL + "/entity/{entityId}"), eq(Grid.class), eq(9)))
                .thenReturn(ResponseEntity.ok(grid));

        Optional<Grid> result = gridService.getGridOfEntity(9);

        assertThat(result).contains(grid);
    }

    @Test
    void testGetGridOfEntity_NotFound_ReturnsEmptyOptional() {
        Mockito.when(restTemplate.getForEntity(eq(BASE_URL + "/entity/{entityId}"), eq(Grid.class), eq(9)))
                .thenThrow(HttpClientErrorException.NotFound.class);

        Optional<Grid> result = gridService.getGridOfEntity(9);

        assertThat(result).isEmpty();
    }

    @Test
    void testGetGridOfEntity_RestTemplateThrows_WrapsInServiceException() {
        Mockito.when(restTemplate.getForEntity(eq(BASE_URL + "/entity/{entityId}"), eq(Grid.class), eq(9)))
                .thenThrow(new RestClientException("boom"));

        assertThatThrownBy(() -> gridService.getGridOfEntity(9))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Failed to fetch grid for entity: 9");
    }
}
