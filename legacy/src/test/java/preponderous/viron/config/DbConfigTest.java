// Copyright (c) 2024 Preponderous Software
// MIT License

package preponderous.viron.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class DbConfigTest {
    
    @Test
    void testInitialization() {
        DbConfig dbConfig = new DbConfig();
    }

    @Test
    void testGetDbUrl() {
        DbConfig dbConfig = new DbConfig();
        dbConfig.setDbUrl("localhost");
        assert(dbConfig.getDbUrl().equals("localhost"));
    }

    @Test
    void testGetDbUsername() {
        DbConfig dbConfig = new DbConfig();
        dbConfig.setDbUsername("viron");
        assert(dbConfig.getDbUsername().equals("viron"));
    }

    @Test
    void testGetDbPassword() {
        DbConfig dbConfig = new DbConfig();
        dbConfig.setDbPassword("viron");
        assert(dbConfig.getDbPassword().equals("viron"));
    }
}
