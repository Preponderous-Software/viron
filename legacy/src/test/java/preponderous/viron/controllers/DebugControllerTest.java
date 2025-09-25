package preponderous.viron.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import preponderous.viron.services.EntityService;
import preponderous.viron.services.EnvironmentService;
import preponderous.viron.services.GridService;
import preponderous.viron.services.LocationService;

@SpringBootTest
public class DebugControllerTest {

    @Mock
    private EntityService entityService;

    @Mock
    private EnvironmentService environmentService;

    @Mock
    private GridService gridService;

    @Mock
    private LocationService locationService;

    @InjectMocks
    private DebugController debugController;

    @BeforeEach
    public void setUp() {
        entityService = mock(EntityService.class);
        environmentService = mock(EnvironmentService.class);
        gridService = mock(GridService.class);
        locationService = mock(LocationService.class);
        debugController = new DebugController(entityService, environmentService, gridService, locationService);
    }

    // TODO: implement tests for DebugController methods
}