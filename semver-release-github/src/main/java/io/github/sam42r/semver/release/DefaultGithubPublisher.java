package io.github.sam42r.semver.release;

import io.github.sam24r.semver.release.ReleaseException;
import io.github.sam24r.semver.release.ReleasePublisher;
import io.github.sam42r.semver.model.release.ProviderSpec;
import io.github.sam42r.semver.model.release.ReleaseInfo;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GitHubBuilder;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class DefaultGithubPublisher implements ReleasePublisher {

    private final String endPoint;
    private final String username;
    private final String password;

    @Override
    public void publish(
            @NonNull String scheme,
            @NonNull String instance,
            @NonNull String group,
            @NonNull String project,
            @NonNull ReleaseInfo releaseInfo
    ) throws ReleaseException {
        try {
            var gitHubBuilder = new GitHubBuilder().withEndpoint(endPoint.formatted(scheme, instance));

            if (username != null && password != null) {
                gitHubBuilder.withPassword(username, password);
            } else if (password != null) {
                gitHubBuilder.withOAuthToken(password);
            }

            var gitHub = gitHubBuilder.build();

            var repository = gitHub.getRepository("%s/%s".formatted(group, project));

            var release = repository.createRelease(releaseInfo.tagName())
                    .name(releaseInfo.name())
                    .body(releaseInfo.description())
                    .create();

            log.debug("Released {}", release.getHtmlUrl());
        } catch (IOException e) {
            throw new ReleaseException(e);
        }
    }

    @Override
    public ProviderSpec providerSpec() {
        return new ProviderSpec(
                "%s://%s/%s/%s/issues/%s",
                "%s://%s/%s/%s/commit/%s",
                "%s://%s/%s/%s/compare/%s...%s"
        );
    }
}
