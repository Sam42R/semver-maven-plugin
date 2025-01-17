package io.github.sam42r.semver.scm;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class GitProviderTest extends AbstractGitProviderTest {

    @Override
    SCMProvider getUut(Path tempDirectory) {
        return new GitProviderFactory()
                .getInstance(tempDirectory, null, null);
    }

    @Test
    @Disabled("local testing only")
    void shouldPushToRemote() throws IOException, SCMException {
        var path = Path.of("..").toRealPath();

        var scmProvider = new GitProvider(path, null, "***");

        var actual = scmProvider.push(false);
        assertThat(actual).isNotNull();
    }
}
