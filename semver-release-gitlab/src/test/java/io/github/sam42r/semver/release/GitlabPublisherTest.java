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
class GitlabPublisherTest {

    private ReleasePublisher uut;

    @BeforeEach
    void setup(MockServerClient client) {
        uut = new GitlabPublisherFactory().getInstance(
                "%s://%s/api/v4/projects/%s/releases",
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
                                  "tag_name": "v1.0.0",
                                  "name": "v1.0.0",
                                  "created_at": "2024-01-01T12:00:00.000Z",
                                  "released_at": "2024-01-01T12:00:00.000Z"
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
                        .withPath("/api/v4/projects/JUnit%2FTest/releases")
                        .withHeader("Content-Type", "application/json")
                        .withHeader("PRIVATE-TOKEN", "token"),
                VerificationTimes.exactly(1));
    }

    @Test
    void shouldThrowOnHttp500(MockServerClient client) {
        client.when(request(), Times.exactly(1))
                .respond(response()
                        .withStatusCode(500)
                        .withBody("Internal server error")
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
                .hasMessage("Release API does return with HTTP-500 - Internal server error");

        client.verify(request().withMethod("POST")
                        .withPath("/api/v4/projects/JUnit%2FTest/releases")
                        .withHeader("Content-Type", "application/json")
                        .withHeader("PRIVATE-TOKEN", "token"),
                VerificationTimes.exactly(1));
    }
}
