package io.github.sam42r.semver.changelog;

import io.github.sam42r.semver.analyzer.model.AnalyzedCommit;
import io.github.sam42r.semver.analyzer.model.ChangeCategory;
import io.github.sam42r.semver.changelog.model.VersionInfo;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class HtmlRendererTest {

    @TempDir
    private Path tempDir;

    @ParameterizedTest
    @CsvSource({"default", "missive", "spinal"})
    void shouldCreateChangelogFull(String template) throws IOException {
        var uut = new HtmlRenderer(template);
        var changelog = tempDir.resolve("Changelog.html");

        try (var inputStream = uut.renderChangelog(changelog, release("v1.0.0"), analyzedCommits(), Function.identity())) {
            var actual = inputStream.readAllBytes();
            Files.write(changelog, actual);

            assertThat(actual).asString(StandardCharsets.UTF_8)
                    .startsWith("<!DOCTYPE html>")
                    .contains("<!-- DO NOT REMOVE - c871f32ed1b7a85b24a0f22e8e7d9e3ee285742c - DO NOT REMOVE -->")
                    .contains("v1.0.0");
        }
    }

    @ParameterizedTest
    @CsvSource({"default", "missive", "spinal"})
    void shouldUpdateChangelog(String template) throws IOException {
        var uut = new HtmlRenderer(template);
        var changelog = tempDir.resolve("Changelog.html");

        try (var inputStream = uut.renderChangelog(changelog, release("v1.0.0"), analyzedCommits(), Function.identity())) {
            var actual = inputStream.readAllBytes();
            Files.write(changelog, actual);
        }

        try (var inputStream = uut.renderChangelog(changelog, release("v1.0.1"), analyzedCommits(), Function.identity())) {
            var actual = inputStream.readAllBytes();
            Files.write(changelog, actual);

            assertThat(actual).asString(StandardCharsets.UTF_8)
                    .startsWith("<!DOCTYPE html>")
                    .contains("<!-- DO NOT REMOVE - c871f32ed1b7a85b24a0f22e8e7d9e3ee285742c - DO NOT REMOVE -->")
                    .contains("v1.0.1")
                    .contains("v1.0.0");
        }
    }

    private VersionInfo release(String version) {
        return new VersionInfo(version, LocalDateTime.now().format(DateTimeFormatter.ISO_DATE), "");
    }

    private List<AnalyzedCommit> analyzedCommits() {
        return List.of(
                AnalyzedCommit.builder()
                        .id("42")
                        .timestamp(Instant.EPOCH)
                        .author("JUnit")
                        .header("feat(scm): Lorem ipsum")
                        .body("* Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam")
                        .footer("refs #42")
                        .category(ChangeCategory.ADDED)
                        .build(),
                AnalyzedCommit.builder()
                        .id("42")
                        .timestamp(Instant.EPOCH)
                        .author("JUnit")
                        .header("fix(scm): Lorem ipsum")
                        .body("* Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam")
                        .footer("refs #42")
                        .category(ChangeCategory.FIXED)
                        .build(),
                AnalyzedCommit.builder()
                        .id("42")
                        .timestamp(Instant.EPOCH)
                        .author("JUnit")
                        .header("chore(scm): Lorem ipsum")
                        .body("* Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam")
                        .footer("refs #42")
                        .category(ChangeCategory.CHANGED)
                        .build()
        );
    }
}
