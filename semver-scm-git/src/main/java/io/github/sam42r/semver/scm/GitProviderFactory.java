package io.github.sam42r.semver.scm;

import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.inject.Named;
import java.nio.file.Path;


/**
 * @deprecated since switching to Maven-SCM {@link DefaultGitProviderFactory} is used instead
 */
@Named("Git(deprecated)")
@NoArgsConstructor
@Deprecated(since = "1.5.0", forRemoval = true)
public class GitProviderFactory implements SCMProviderFactory<GitProvider> {

    @Override
    public @NonNull GitProvider getInstance(@NonNull Path path, String username, String password) {
        return new GitProvider(path, username, password);
    }
}
