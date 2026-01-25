package io.github.sam42r.semver.release;

import io.github.sam24r.semver.release.ReleaseException;
import io.github.sam24r.semver.release.ReleasePublisher;
import io.github.sam24r.semver.release.model.ReleaseInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
                "%s://%s",
                null,
                "token"
        );
        client.reset();
    }

    @Test
    void shouldCreateRelease(MockServerClient client) throws ReleaseException {
        client.when(request("/user").withMethod("GET"), Times.exactly(1))
                .respond(response()
                        .withStatusCode(200)
                        .withBody("""
                                {
                                  "id": 1,
                                  "type": "User",
                                  "login": "octocat",
                                  "company": "GitHub",
                                  "url": "https://api.github.com/users/octocat"
                                }
                                """)
                );

        client.when(request("/repos/JUnit/Test").withMethod("GET"), Times.exactly(1))
                .respond(response()
                        .withStatusCode(200)
                        .withBody("""
                                {
                                   "id": 42,
                                   "name": "Test",
                                   "full_name": "JUnit/Test",
                                   "owner": {
                                     "id": 1,
                                     "type": "User",
                                     "login": "octocat",
                                     "url": "https://api.github.com/users/octocat"
                                   },
                                   "private": false,
                                   "html_url": "https://github.com/JUnit/Test",
                                   "description": "This your first repo!",
                                   "fork": false,
                                   "url": "https://api.github.com/repos/JUnit/Test"
                                }
                                """)
                );

        client.when(request("/repos/JUnit/Test/releases").withMethod("POST"), Times.exactly(1))
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
                        .withHeader("X-GitHub-Api-Version", "2022-11-28"),
                VerificationTimes.exactly(1));
    }

    @Test
    void shouldThrowOnHttp422(MockServerClient client) {
        client.when(request("/user").withMethod("GET"), Times.exactly(1))
                .respond(response()
                        .withStatusCode(200)
                        .withBody("""
                                {
                                  "id": 1,
                                  "type": "User",
                                  "login": "octocat",
                                  "company": "GitHub",
                                  "url": "https://api.github.com/users/octocat"
                                }
                                """)
                );

        client.when(request("/repos/JUnit/Test").withMethod("GET"), Times.exactly(1))
                .respond(response()
                        .withStatusCode(200)
                        .withBody("""
                                {
                                   "id": 42,
                                   "name": "Test",
                                   "full_name": "JUnit/Test",
                                   "owner": {
                                     "id": 1,
                                     "type": "User",
                                     "login": "octocat",
                                     "url": "https://api.github.com/users/octocat"
                                   },
                                   "private": false,
                                   "html_url": "https://github.com/JUnit/Test",
                                   "description": "This your first repo!",
                                   "fork": false,
                                   "url": "https://api.github.com/repos/JUnit/Test"
                                }
                                """)
                );

        client.when(request("/repos/JUnit/Test/releases").withMethod("POST"), Times.exactly(1))
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
                .hasMessage("org.kohsuke.github.HttpException: endpoint has been spammed");

        client.verify(request().withMethod("POST")
                        .withPath("/repos/JUnit/Test/releases")
                        .withHeader("Accept", "application/vnd.github+json")
                        .withHeader("X-GitHub-Api-Version", "2022-11-28"),
                VerificationTimes.exactly(1));
    }

    @ParameterizedTest
    @CsvSource({
            ",localhost:42,JUnit,Test,scheme",
            "http,,JUnit,Test,instance",
            "http,localhost:42,,Test,group",
            "http,localhost:42,JUnit,,project"
    })
    void shouldThrowOnNullValue(String scheme, String instance, String group, String project, String field) {
        var releaseInfo = ReleaseInfo.builder().build();
        assertThatThrownBy(() -> uut.publish(scheme, instance, group, project, releaseInfo))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("%s is marked non-null but is null".formatted(field));
    }
}
