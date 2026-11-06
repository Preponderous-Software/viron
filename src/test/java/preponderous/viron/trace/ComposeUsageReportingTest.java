// Copyright (c) 2024 Preponderous Software
// MIT License

package preponderous.viron.trace;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code compose.yml} names the {@code viron} service's environment one variable at a time and
 * has no {@code env_file}, so a variable it does not name never reaches the container. Each one
 * below is documented in the README as something an operator can set, and an opt-out that stops
 * at compose is an opt-out that silently does nothing.
 *
 * <p>The optional ones must be declared without a value. Compose then passes them only when they
 * are set; a {@code ${VAR:-}} default would hand the container an empty string instead, and an
 * empty endpoint or key turns reporting off rather than falling back to its default. Compose
 * itself is not run: this reads the file it would load.
 */
class ComposeUsageReportingTest {

    private static final List<String> PASSED_ONLY_WHEN_SET = List.of(
            "DO_NOT_TRACK",
            "TRACE_USAGE_REPORTING",
            TraceInstallId.ENV,
            "USAGE_REPORTING_ENDPOINT",
            "USAGE_REPORTING_KEY");

    @Test
    void optionalReportingVariablesArePassedOnlyWhenSet() throws IOException {
        Map<String, Object> environment = vironEnvironment();

        assertThat(environment).containsKeys(PASSED_ONLY_WHEN_SET.toArray(new String[0]));
        PASSED_ONLY_WHEN_SET.forEach(name -> assertThat(environment.get(name))
                .as("%s is declared without a value, so an unset variable stays unset", name)
                .isNull());
    }

    @Test
    void sampleEnvListsTheOptionalReportingVariables() throws IOException {
        List<String> lines = Files.readAllLines(Path.of("sample.env"));

        PASSED_ONLY_WHEN_SET.forEach(name -> assertThat(lines)
                .as("sample.env mentions %s, commented out so its default is kept", name)
                .anyMatch(line -> line.startsWith("#" + name + "=")));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> vironEnvironment() throws IOException {
        try (Reader compose = Files.newBufferedReader(Path.of("compose.yml"))) {
            Map<String, Object> file = new Yaml().load(compose);
            Map<String, Object> services = (Map<String, Object>) file.get("services");
            Map<String, Object> viron = (Map<String, Object>) services.get("viron");
            return (Map<String, Object>) viron.get("environment");
        }
    }
}
