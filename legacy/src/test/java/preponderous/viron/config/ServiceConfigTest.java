// Copyright (c) 2024 Preponderous Software
// MIT License

package preponderous.viron.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ServiceConfigTest {
    
    @Test
    void testInitialization() {
        ServiceConfig serviceConfig = new ServiceConfig();
    }

    @Test
    void testGetVironHost() {
        ServiceConfig serviceConfig = new ServiceConfig();
        serviceConfig.setVironHost("localhost");
        assert(serviceConfig.getVironHost().equals("localhost"));
    }

    @Test
    void testGetVironPort() {
        ServiceConfig serviceConfig = new ServiceConfig();
        serviceConfig.setVironPort(9999);
        assert(serviceConfig.getVironPort() == 9999);
    }
}
