package io.github.sam42r.semver.release;

import io.github.sam24r.semver.release.ReleaseException;
import io.github.sam24r.semver.release.ReleasePublisher;
import io.github.sam24r.semver.release.model.ReleaseInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.matchers.Times;
import org.mockserver.verify.VerificationTimes;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@ExtendWith(MockServerExtension.class)
class GithubPublisherTest {

    private ReleasePublisher uut;

    @BeforeEach
    void setup(MockServerClient client) {
        uut = new GithubPublisherFactory().getInstance(
                "%s://%s/repos/%s/%s/releases",
                null,
                "token"
        );
        client.reset();
    }

    @Test
    void shouldCreateRelease(MockServerClient client) throws ReleaseException {
        client.when(request(), Times.exactly(1))
                .respond(response()
                        .withStatusCode(201)
                        .withBody("""
                                {
                                  "url": "https://api.github.com/repos/octocat/Hello-World/releases/1",
                                  "html_url": "https://github.com/octocat/Hello-World/releases/v1.0.0"
                                }
                                """)
                );

        var release = ReleaseInfo.builder()
                .tagName("v1.0.0")
                .name("v1.0.0")
                .description("# Release v1.0.0")
                .time(LocalDateTime.now())
                .build();

        uut.publish(
                "http",
                "localhost:%d".formatted(client.getPort()),
                "JUnit",
                "Test",
                release);

        client.verify(request().withMethod("POST")
                        .withPath("/repos/JUnit/Test/releases")
                        .withHeader("Accept", "application/vnd.github+json")
                        .withHeader("Authorization", "Bearer token")
                        .withHeader("X-GitHub-Api-Version", "2022-11-28"),
                VerificationTimes.exactly(1));
    }

    @Test
    void shouldThrowOnHttp422(MockServerClient client) {
        client.when(request(), Times.exactly(1))
                .respond(response()
                        .withStatusCode(422)
                        .withBody("endpoint has been spammed")
                );

        var release = ReleaseInfo.builder()
                .tagName("v1.0.0")
                .name("v1.0.0")
                .description("# Release v1.0.0")
                .time(LocalDateTime.now())
                .build();

        assertThatThrownBy(() -> uut.publish(
                "http",
                "localhost:%d".formatted(client.getPort()),
                "JUnit",
                "Test",
                release))
                .isInstanceOf(ReleaseException.class)
                .hasMessage("Release API does return with HTTP-422 - endpoint has been spammed");

        client.verify(request().withMethod("POST")
                        .withPath("/repos/JUnit/Test/releases")
                        .withHeader("Accept", "application/vnd.github+json")
                        .withHeader("Authorization", "Bearer token")
                        .withHeader("X-GitHub-Api-Version", "2022-11-28"),
                VerificationTimes.exactly(1));
    }
}
