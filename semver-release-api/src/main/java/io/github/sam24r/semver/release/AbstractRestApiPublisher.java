package io.github.sam24r.semver.release;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.sam42r.semver.model.release.ReleaseInfo;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
public abstract class AbstractRestApiPublisher implements ReleasePublisher {

    @Getter
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String baseUrl;

    @Override
    public void publish(@NonNull String scheme, @NonNull String instance, @NonNull String group, @NonNull String project, @NonNull ReleaseInfo releaseInfo) throws ReleaseException {
        var projectPath = "%s/%s".formatted(group, project);
        var encodedProjectPath = URLEncoder.encode(projectPath, StandardCharsets.UTF_8);
        var uri = URI.create(baseUrl.formatted(scheme, instance, encodedProjectPath));
        var payload = generatePayload(releaseInfo);

        try (var httpClient = HttpClient.newHttpClient()) {
            var json = objectMapper.writeValueAsString(payload);

            var httpRequest = HttpRequest.newBuilder()
                    .uri(uri)
                    .headers(headers())
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            var response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 201) {
                throw new ReleaseException("Release API does return with HTTP-%d - %s".formatted(
                        response.statusCode(), getErrorMessage(response.body())));
            }
        } catch (IOException e) {
            throw new ReleaseException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ReleaseException(e);
        }
    }

    protected abstract Object generatePayload(ReleaseInfo releaseInfo);

    protected abstract String[] headers();

    protected abstract String getErrorMessage(String responseBody) throws IOException;
}
