package io.github.sam42r.semver.release;

import io.github.sam24r.semver.release.ReleasePublisher;
import io.github.sam24r.semver.release.ReleasePublisherFactory;
import lombok.NonNull;

import javax.inject.Named;

@Named("Gitlab")
public class GitlabPublisherFactory implements ReleasePublisherFactory {

    @Override
    public @NonNull ReleasePublisher getInstance(String username, String password) {
        return getInstance("%s://%s/api/v4/projects/%s/releases", username, password);
    }

    @Override
    public @NonNull ReleasePublisher getInstance(@NonNull String baseUrl, String username, String password) {
        return new GitlabPublisher(baseUrl, password);
    }
}
