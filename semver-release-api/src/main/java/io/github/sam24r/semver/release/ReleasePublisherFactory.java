package io.github.sam24r.semver.release;

import lombok.NonNull;

public interface ReleasePublisherFactory {

    @NonNull String getName();

    @NonNull ReleasePublisher getInstance(String username, String password);

    @NonNull ReleasePublisher getInstance(@NonNull String baseUrl, String username, String password);
}
