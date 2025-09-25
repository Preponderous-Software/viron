// Copyright (c) 2024 Preponderous Software
// MIT License

package preponderous.viron;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import preponderous.viron.config.DbConfig;
import preponderous.viron.config.ServiceConfig;

@SpringBootApplication
@Import({DbConfig.class, ServiceConfig.class})
public class VironApplication {

	public static void main(String[] args) {
		SpringApplication.run(VironApplication.class, args);
	}

}
