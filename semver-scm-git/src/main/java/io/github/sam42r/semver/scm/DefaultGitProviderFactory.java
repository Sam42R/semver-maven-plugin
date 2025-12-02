package io.github.sam42r.semver.scm;

import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.inject.Named;
import java.nio.file.Path;

@Named("Git")
@NoArgsConstructor
public class DefaultGitProviderFactory implements SCMProviderFactory<DefaultGitProvider> {

    @Override
    public @NonNull DefaultGitProvider getInstance(@NonNull Path path, String username, String password) {
        return new DefaultGitProvider(path, username, password);
    }
}
