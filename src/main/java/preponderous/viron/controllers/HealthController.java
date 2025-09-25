package preponderous.viron.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Health check controller for monitoring application status.
 *
 * <p>Provides endpoints to verify that the Viron application is running and healthy.
 */
@RestController
@RequestMapping("/health")
@Tag(name = "Health", description = "Application health monitoring")
@Slf4j
public class HealthController {

  /**
   * Basic health check endpoint.
   *
   * @return ResponseEntity with health status information
   */
  @GetMapping
  @Operation(summary = "Check application health", description = "Returns basic health status")
  public ResponseEntity<Map<String, Object>> health() {
    log.debug("Health check requested");

    Map<String, Object> health =
        Map.of(
            "status", "UP",
            "timestamp", LocalDateTime.now(),
            "service", "Viron Spatial Simulation Service",
            "version", "0.6.0-SNAPSHOT");

    return ResponseEntity.ok(health);
  }
}