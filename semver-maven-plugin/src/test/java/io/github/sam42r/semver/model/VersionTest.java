package io.github.sam42r.semver.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VersionTest {

    @Test
    void shouldCreateVersionWithDefaultRegex() {
        var actual = Version.of("v1.0.0");

        assertThat(actual)
                .extracting("major", "minor", "patch")
                .containsExactly(1, 0, 0);
        assertThat(actual.toString()).isEqualTo("v1.0.0");
    }

    @Test
    void shouldCreateVersionWithCustomRegex() {
        var actual = Version.of("Version 1-0-0", "Version (?<MAJOR>[0-9]*)-(?<MINOR>[0-9]*)-(?<PATCH>[0-9]*)");

        assertThat(actual)
                .extracting("major", "minor", "patch")
                .containsExactly(1, 0, 0);
        assertThat(actual.toString()).isEqualTo("Version 1-0-0");
    }

    @Test
    void shouldThrowWithInvalidRegex() {
        assertThatThrownBy(() -> Version.of("v1.0.0", "invalid"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith("Regular expression 'invalid' does not contain required capture groups");
    }

    @Test
    void shouldIncrement() {
        var actual = Version.of("v1.0.0");

        actual.increment(Version.Type.PATCH);
        assertThat(actual.toString()).isEqualTo("v1.0.1");

        actual.increment(Version.Type.MINOR);
        assertThat(actual.toString()).isEqualTo("v1.1.0");

        actual.increment(Version.Type.MAJOR);
        assertThat(actual.toString()).isEqualTo("v2.0.0");
    }
}
