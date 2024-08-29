package io.github.sam42r.semver.release;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.sam24r.semver.release.ReleaseException;
import io.github.sam24r.semver.release.ReleasePublisher;
import io.github.sam24r.semver.release.model.ReleaseInfo;
import io.github.sam42r.semver.release.model.GithubRelease;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * {@link ReleasePublisher} for GitHub.<br/>
 *
 * @see <a href="https://docs.github.com/de/rest/releases/releases?apiVersion=latest">
 * REST-API-Endpunkte für Releases - GitHub Dokumentation</a>
 */
@Slf4j
@RequiredArgsConstructor
public class GithubPublisher implements ReleasePublisher {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String baseUrl;
    private final String token;

    @Override
    public void publish(
            @NonNull String scheme,
            @NonNull String instance,
            @NonNull String group,
            @NonNull String project,
            @NonNull ReleaseInfo releaseInfo
    ) throws ReleaseException {
        var uri = URI.create(baseUrl.formatted(scheme, instance, group, project));
        var payload = generatePayload(releaseInfo);

        try {
            var json = objectMapper.writeValueAsString(payload);

            var httpRequest = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Accept", "application/vnd.github+json")
                    .header("Authorization", "Bearer %s".formatted(token))
                    .header("X-GitHub-Api-Version", "2022-11-28")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            var response = HttpClient.newHttpClient()
                    .send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 201) {
                throw new ReleaseException("Release API does return with HTTP-%d - %s".formatted(
                        response.statusCode(), response.body()));
            }
        } catch (IOException | InterruptedException e) {
            throw new ReleaseException(e);
        }
    }

    private GithubRelease generatePayload(ReleaseInfo releaseInfo) {
        return GithubRelease.builder()
                .tagName(releaseInfo.getTagName())
                .name(releaseInfo.getName())
                .body(releaseInfo.getDescription())
                .build();
    }
}
