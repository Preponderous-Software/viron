// Copyright (c) 2024 Preponderous Software
// MIT License

package preponderous.viron.trace;


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link UsageReporter}'s construction rules: the opt-out, the no-key no-op,
 * the malformed-endpoint fallback, and the tags the startup event carries. No Spring context
 * and no network.
 */
class UsageReporterTest {

    private static final String ENDPOINT = "http://127.0.0.1:9";

    @Test
    void reportsWhenEnabledWithAKey() {
        UsageReporter reporter = new UsageReporter(true, ENDPOINT, "a-key", "1.2.3");
        try {
            assertThat(reporter.isReporting()).isTrue();
        } finally {
            reporter.close();
        }
    }

    @Test
    void optOutDisablesReporting() {
        UsageReporter reporter = new UsageReporter(false, ENDPOINT, "a-key", "1.2.3");
        assertThat(reporter.isReporting()).isFalse();
        reporter.reportStartup(); // a no-op, must not throw
        reporter.close();
    }

    @Test
    void blankKeyDisablesReporting() {
        UsageReporter reporter = new UsageReporter(true, ENDPOINT, "   ", "1.2.3");
        assertThat(reporter.isReporting()).isFalse();
        reporter.close();
    }

    @Test
    void malformedEndpointDisablesReportingInsteadOfFailingStartup() {
        UsageReporter reporter = new UsageReporter(true, "   ", "a-key", "1.2.3");
        assertThat(reporter.isReporting()).isFalse();
        reporter.close();
    }

    @Test
    void startupIsTaggedAsAServiceWithItsVersion() {
        UsageReporter reporter = new UsageReporter(false, ENDPOINT, "a-key", " 0.7.0 ");
        assertThat(reporter.startupTags()).containsEntry("service", "true").hasSize(1);
        assertThat(reporter.version()).isEqualTo("0.7.0");
        reporter.close();
    }

    // Outside a Maven build the property keeps its unfilled placeholder; that must not be reported as a version.
    @Test
    void unfilledVersionPlaceholderIsNotReported() {
        UsageReporter reporter = new UsageReporter(false, ENDPOINT, "a-key", "@project.version@");
        assertThat(reporter.version()).isNotEqualTo("@project.version@").isNotBlank();
        assertThat(reporter.startupTags()).containsEntry("service", "true").doesNotContainKey("@project.version@");
        reporter.close();
    }

    @Test
    void closeIsIdempotent() {
        UsageReporter reporter = new UsageReporter(true, ENDPOINT, "a-key", "1.2.3");
        reporter.close();
        reporter.close();
    }
}
