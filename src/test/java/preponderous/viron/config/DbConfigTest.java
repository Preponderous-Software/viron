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

    // #212: a DbConfig built by hand, as the H2-backed tests do, is already bounded and sized.
    @Test
    void poolBoundsDefaultToBoundedValues() {
        DbConfig dbConfig = new DbConfig();
        assert(dbConfig.getMaxPoolSize() == DbConfig.DEFAULT_MAX_POOL_SIZE);
        assert(dbConfig.getLockTimeoutMs() == DbConfig.DEFAULT_LOCK_TIMEOUT_MS);
        assert(dbConfig.getLockTimeoutMs() > 0);
    }

    @Test
    void testGetPoolBounds() {
        DbConfig dbConfig = new DbConfig();
        dbConfig.setMaxPoolSize(3);
        dbConfig.setLockTimeoutMs(1234);
        assert(dbConfig.getMaxPoolSize() == 3);
        assert(dbConfig.getLockTimeoutMs() == 1234);
    }
}
