// Copyright (c) 2024 Preponderous Software
// MIT License

package preponderous.viron.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;

@Setter
@Getter
@ConfigurationProperties("database")
@PropertySource("classpath:application.properties")
public class DbConfig {
    private String dbUrl;
    private String dbUsername;
    private String dbPassword;
}
