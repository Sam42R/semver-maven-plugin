package io.github.sam42r.semver.scm;

import java.nio.file.Path;

class DefaultGitProviderTest extends AbstractGitProviderTest {

    @Override
    SCMProvider getUut(Path tempDirectory) {
        return new DefaultGitProviderFactory()
                .getInstance(tempDirectory, null, null);
    }
}
