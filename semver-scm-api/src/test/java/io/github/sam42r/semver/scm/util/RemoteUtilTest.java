package io.github.sam42r.semver.scm.util;

import io.github.sam42r.semver.scm.model.Remote;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class RemoteUtilTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "git@github.com:JUnit/test.git",
            "https://github.com/JUnit/test.git"
    })
    void shouldReadRemoteGithub(String url) {
        var actual = RemoteUtil.parseUrl(url);

        assertThat(actual).isEqualTo(Remote.builder()
                .url(url)
                .scheme("https")
                .host("github.com")
                .group("JUnit")
                .project("test")
                .build());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "git@gitlab.local:10022:JUnit/subgroup/test.git",
            "https://gitlab.local:10022/JUnit/subgroup/test.git"
    })
    void shouldReadRemoteGitlab(String url) {
        var actual = RemoteUtil.parseUrl(url);

        assertThat(actual).isEqualTo(Remote.builder()
                .url(url)
                .scheme("https")
                .host("gitlab.local:10022")
                .group("JUnit/subgroup")
                .project("test")
                .build());
    }
}
