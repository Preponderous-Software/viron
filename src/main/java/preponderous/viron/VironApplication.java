package preponderous.viron;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for Viron - the foundational spatial simulation service.
 *
 * <p>Viron manages environments, grids, locations, and entities through a clean REST API, serving
 * as a reusable backend component for simulation-based games, AI experiments, and virtual world
 * applications.
 */
@SpringBootApplication
public class VironApplication {

  public static void main(String[] args) {
    SpringApplication.run(VironApplication.class, args);
  }
}
