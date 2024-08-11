package io.github.sam42r.semver.scm;

import lombok.NonNull;

import java.nio.file.Path;

public class SvnProviderFactory implements SCMProviderFactory<SvnProvider> {

    private static final String PROVIDER_NAME = "Subversion";

    @Override
    public @NonNull String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public @NonNull SvnProvider getInstance(@NonNull Path path, String username, String password) {
        return new SvnProvider(path, username, password);
    }
}
