package io.github.sam42r.semver.scm;

import lombok.NonNull;

import java.nio.file.Path;

public interface SCMProviderFactory<P extends SCMProvider> {

    @NonNull String getProviderName();

    @NonNull P getInstance(@NonNull Path path, String username, String password);
}
