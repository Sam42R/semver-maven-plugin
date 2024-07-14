package io.github.sam42r.semver.release;

import io.github.sam24r.semver.release.ReleasePublisher;
import io.github.sam24r.semver.release.ReleasePublisherFactory;
import lombok.NonNull;

public class GithubPublisherFactory implements ReleasePublisherFactory {

    private static final String PUBLISHER_NAME = "Github";

    @Override
    public @NonNull String getName() {
        return PUBLISHER_NAME;
    }

    @Override
    public @NonNull ReleasePublisher getInstance(String username, String password) {
        return getInstance("https://api.%s/repos/%s/%s/releases", username, password);
    }

    @Override
    public @NonNull ReleasePublisher getInstance(@NonNull String baseUrl, String username, String password) {
        return new GithubPublisher(baseUrl, password);
    }
}
