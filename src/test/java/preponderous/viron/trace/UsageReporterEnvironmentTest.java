// Copyright (c) 2024 Preponderous Software
// MIT License

package preponderous.viron.trace;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The environment opt-outs shared by every program that reports to trace must win over this
 * service's own {@code usage-reporting.enabled} setting. The vendored client checks them in its
 * builder, so this only holds while {@link UsageReporter} routes its switch through the builder
 * instead of deciding beforehand. No context is started and the endpoint is a closed loopback
 * port, so nothing is sent.
 */
class UsageReporterEnvironmentTest {

    private static final String ENDPOINT = "http://127.0.0.1:9";

    private final Map<String, String> environment = new HashMap<>();
    private Function<String, String> realEnvironment;

    @BeforeEach
    void isolateEnvironment() {
        realEnvironment = TraceClient.environment;
        TraceClient.environment = environment::get;
    }

    @AfterEach
    void restoreEnvironment() {
        TraceClient.environment = realEnvironment;
    }

    @Test
    void doNotTrackDisablesReportingEvenWhenTheSettingSaysEnabled() {
        environment.put("DO_NOT_TRACK", "1");
        UsageReporter reporter = new UsageReporter(true, ENDPOINT, "test-key", "1.0");
        assertThat(reporter.isReporting()).as("DO_NOT_TRACK=1 switches reporting off").isFalse();
        reporter.close();
    }

    @Test
    void traceUsageReportingOffDisablesReportingEvenWhenTheSettingSaysEnabled() {
        environment.put("TRACE_USAGE_REPORTING", "off");
        UsageReporter reporter = new UsageReporter(true, ENDPOINT, "test-key", "1.0");
        assertThat(reporter.isReporting()).as("TRACE_USAGE_REPORTING=off switches reporting off").isFalse();
        reporter.close();
    }

    @Test
    void anEmptyEnvironmentLeavesTheSettingInCharge() {
        UsageReporter on = new UsageReporter(true, ENDPOINT, "test-key", "1.0");
        assertThat(on.isReporting()).isTrue();
        on.close();
        UsageReporter off = new UsageReporter(false, ENDPOINT, "test-key", "1.0");
        assertThat(off.isReporting()).isFalse();
        off.close();
    }

    @Test
    void aBlankEndpointStillYieldsADisabledReporterRatherThanAFailedStart() {
        UsageReporter reporter = new UsageReporter(true, " ", "test-key", "1.0");
        assertThat(reporter.isReporting()).isFalse();
        reporter.close();
    }
}
