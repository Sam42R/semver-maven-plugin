package io.github.sam42r.semver.release;

import io.github.sam24r.semver.release.ReleaseException;
import io.github.sam24r.semver.release.ReleasePublisher;
import io.github.sam42r.semver.model.release.ReleaseInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.matchers.Times;
import org.mockserver.verify.VerificationTimes;

import java.io.IOException;
import java.net.http.HttpClient;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@ExtendWith(MockServerExtension.class)
class CodebergPublisherTest {

    private ReleasePublisher uut;

    @BeforeEach
    void setup(MockServerClient client) {
        uut = new CodebergPublisherFactory().getInstance(null, "token");
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
                                  "published_at": "2024-01-01T12:00:00.000Z"
                                }
                                """)
                );

        var release = new ReleaseInfo(
                "v1.0.0",
                "v1.0.0",
                "# Release v1.0.0",
                LocalDateTime.now()
        );

        uut.publish(
                "http",
                "localhost:%d".formatted(client.getPort()),
                "JUnit",
                "Test",
                release);

        client.verify(request().withMethod("POST")
                        .withPath("/api/v1/repos/JUnit%2FTest/releases")
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Accept", "application/json")
                        .withHeader("Authorization", "token"),
                VerificationTimes.exactly(1));
    }

    @Test
    void shouldThrowOnHttpError(MockServerClient client) {
        client.when(request(), Times.exactly(1))
                .respond(response()
                        .withStatusCode(404)
                        .withBody("""
                                {
                                   "message": "GetUserByName",
                                   "url": "https://codeberg.org/api/swagger",
                                   "errors": [
                                     "user redirect does not exist [name: junit]"
                                   ]
                                 }
                                """)
                );

        var release = new ReleaseInfo(
                "v1.0.0",
                "v1.0.0",
                "# Release v1.0.0",
                LocalDateTime.now()
        );

        assertThatThrownBy(() -> uut.publish(
                "http",
                "localhost:%d".formatted(client.getPort()),
                "JUnit",
                "Test",
                release))
                .isInstanceOf(ReleaseException.class)
                .hasMessage("Release API does return with HTTP-404 - GetUserByName");

        client.verify(request().withMethod("POST")
                        .withPath("/api/v1/repos/JUnit%2FTest/releases")
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Accept", "application/json")
                        .withHeader("Authorization", "token"),
                VerificationTimes.exactly(1));
    }

    @ParameterizedTest
    @MethodSource("thrownExceptions")
    void shouldThrowOnException(Exception thrownException, String expectedMessage, MockServerClient client) throws IOException, InterruptedException {
        try (var httpClientStatic = mockStatic(HttpClient.class)) {
            var httpClient = mock(HttpClient.class);
            when(httpClient.send(Mockito.any(), Mockito.any())).thenThrow(thrownException);

            httpClientStatic.when(HttpClient::newHttpClient).thenReturn(httpClient);

            var release = new ReleaseInfo(
                    "v1.0.0",
                    "v1.0.0",
                    "# Release v1.0.0",
                    LocalDateTime.now()
            );

            assertThatThrownBy(() -> uut.publish(
                    "http",
                    "localhost:%d".formatted(client.getPort()),
                    "JUnit",
                    "Test",
                    release))
                    .isInstanceOf(ReleaseException.class)
                    .hasMessage(expectedMessage);
        }
    }

    private static Stream<Arguments> thrownExceptions() {
        return Stream.of(
                Arguments.of(new IOException("JUnit"), "java.io.IOException: JUnit"),
                Arguments.of(new InterruptedException("JUnit"), "java.lang.InterruptedException: JUnit")
        );
    }
}
