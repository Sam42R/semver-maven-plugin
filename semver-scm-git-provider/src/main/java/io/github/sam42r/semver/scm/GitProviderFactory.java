package io.github.sam42r.semver.scm;

import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.nio.file.Path;

@NoArgsConstructor
public class GitProviderFactory implements SCMProviderFactory<GitProvider> {

    private static final String PROVIDER_NAME = "Git";

    @Override
    public @NonNull String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public @NonNull GitProvider getInstance(@NonNull Path path) {
        return new GitProvider(path);
    }
}
