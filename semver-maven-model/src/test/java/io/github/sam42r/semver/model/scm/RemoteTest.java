package io.github.sam42r.semver.model.scm;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class RemoteTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "git@github.com:JUnit/test.git",
            "https://github.com/JUnit/test.git"
    })
    void shouldReadRemoteGithub(String url) {
        var actual = Remote.of(url);

        assertThat(actual).isEqualTo(
                new Remote(url, "https", "github.com", "JUnit", "test"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "git@gitlab.local:10022:JUnit/subgroup/test.git",
            "https://gitlab.local:10022/JUnit/subgroup/test.git"
    })
    void shouldReadRemoteGitlab(String url) {
        var actual = Remote.of(url);

        assertThat(actual).isEqualTo(
                new Remote(url, "https", "gitlab.local:10022", "JUnit/subgroup", "test"));
    }
}
