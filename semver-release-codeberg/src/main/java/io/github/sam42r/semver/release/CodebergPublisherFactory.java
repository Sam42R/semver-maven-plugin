package io.github.sam42r.semver.release;

import io.github.sam24r.semver.release.ReleasePublisher;
import lombok.NonNull;

import javax.inject.Named;

@Named("Codeberg")
public class CodebergPublisherFactory implements io.github.sam24r.semver.release.ReleasePublisherFactory {

    @Override
    public @NonNull ReleasePublisher getInstance(String username, String password) {
        return getInstance("%s://%s/api/v1/repos/%s/releases", username, password);
    }

    @Override
    public @NonNull ReleasePublisher getInstance(@NonNull String baseUrl, String username, String password) {
        return new CodebergPublisher(baseUrl, password);
    }
}
