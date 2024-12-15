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
        assertThat(actual.toString()).isEqualTo("1.0.0");
        assertThat(actual.toTag()).isEqualTo("v1.0.0");
    }

    @Test
    void shouldCreateVersionWithCustomRegex() {
        var actual = Version.of("Release 1.0.0", "Release ${version}");

        assertThat(actual)
                .extracting("major", "minor", "patch")
                .containsExactly(1, 0, 0);
        assertThat(actual).hasToString("1.0.0");
        assertThat(actual.toTag()).isEqualTo("Release 1.0.0");
    }

    @Test
    void shouldThrowWithInvalidRegex() {
        assertThatThrownBy(() -> Version.of("v1.0.0", "invalid"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith("Given tag format 'invalid' does not contain required version placeholder '${version}'");
    }

    @Test
    void shouldIncrement() {
        var actual = Version.of("v1.0.0");

        actual.increment(Version.Type.PATCH);
        assertThat(actual).hasToString("1.0.1");
        assertThat(actual.toTag()).isEqualTo("v1.0.1");

        actual.increment(Version.Type.MINOR);
        assertThat(actual).hasToString("1.1.0");
        assertThat(actual.toTag()).isEqualTo("v1.1.0");

        actual.increment(Version.Type.MAJOR);
        assertThat(actual).hasToString("2.0.0");
        assertThat(actual.toTag()).isEqualTo("v2.0.0");
    }
}
