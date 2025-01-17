package io.github.sam42r.semver.scm;

import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.nio.file.Path;

@NoArgsConstructor
public class DefaultGitProviderFactory implements SCMProviderFactory<DefaultGitProvider> {

    private static final String PROVIDER_NAME = "Git";

    @Override
    public @NonNull String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public @NonNull DefaultGitProvider getInstance(@NonNull Path path, String username, String password) {
        return new DefaultGitProvider(path, username, password);
    }
}
