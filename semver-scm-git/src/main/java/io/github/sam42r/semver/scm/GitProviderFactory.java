package io.github.sam42r.semver.scm;

import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.nio.file.Path;


/**
 * @deprecated since switching to Maven-SCM {@link DefaultGitProviderFactory} is used instead
 */
@NoArgsConstructor
@Deprecated(since = "1.5.0", forRemoval = true)
public class GitProviderFactory implements SCMProviderFactory<GitProvider> {

    private static final String PROVIDER_NAME = "Git";

    @Override
    public @NonNull String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public @NonNull GitProvider getInstance(@NonNull Path path, String username, String password) {
        return new GitProvider(path, username, password);
    }
}
