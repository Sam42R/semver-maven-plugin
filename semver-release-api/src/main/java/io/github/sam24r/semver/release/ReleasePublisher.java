package io.github.sam24r.semver.release;

import io.github.sam42r.semver.model.release.ProviderSpec;
import io.github.sam42r.semver.model.release.ReleaseInfo;
import lombok.NonNull;

public interface ReleasePublisher {

    void publish(
            @NonNull String scheme,
            @NonNull String instance,
            @NonNull String group,
            @NonNull String project,
            @NonNull ReleaseInfo releaseInfo
    ) throws ReleaseException;

    ProviderSpec providerSpec();
}
