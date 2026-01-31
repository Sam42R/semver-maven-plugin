package io.github.sam24r.semver.release;

import io.github.sam24r.semver.release.model.ReleaseInfo;
import lombok.NonNull;

public interface ReleasePublisher {

    void publish(
            @NonNull String scheme,
            @NonNull String instance,
            @NonNull String group,
            @NonNull String project,
            @NonNull ReleaseInfo releaseInfo
    ) throws ReleaseException;

    default String generateIssueLink(String issue) { return issue; }
}
