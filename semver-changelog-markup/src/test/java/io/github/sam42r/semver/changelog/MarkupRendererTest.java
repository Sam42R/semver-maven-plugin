package io.github.sam42r.semver.changelog;

import io.github.sam42r.semver.model.analyze.AnalyzedCommit;
import io.github.sam42r.semver.model.analyze.ChangeCategory;
import io.github.sam42r.semver.model.analyze.Issue;
import io.github.sam42r.semver.model.analyze.SemVerChangeLevel;
import io.github.sam42r.semver.model.changelog.VersionInfo;
import io.github.sam42r.semver.model.scm.Commit;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MarkupRendererTest {

    @Test
    void shouldCreateChangelogFull(@TempDir Path tempDir) throws IOException {
        var uut = new MarkupRendererFactory().getInstance("changelog", true, true, true, true);

        var changelog = tempDir.resolve("Changelog.md");

        try (var inputStream = uut.renderChangelog(changelog, release("v1.0.0"), analyzedCommitsConventional())) {
            var actual = inputStream.readAllBytes();

            assertThat(actual).asString(StandardCharsets.UTF_8)
                    .startsWith("# Changelog")
                    .contains("## [v1.0.0](https:///junit.org/test/v0.0.1...v1.0.0) - %s".formatted(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE)))
                    .contains("- fix(scm): set clean commit message [(#42)](https://junit.org/test/42)")
                    .contains("* added scope for commit messages")
                    .doesNotContain("refs #42")
                    .contains("## Disclaimer");
        }
    }

    @Test
    void shouldUpdateChangelog(@TempDir Path tempDir) throws IOException {
        var uut = new MarkupRendererFactory().getInstance("changelog", false, false, false, false);

        var changelog = tempDir.resolve("Changelog.md");
        Files.writeString(
                changelog,
                """
                        # Changelog
                        
                        header text
                        
                        <!-- DO NOT REMOVE - c871f32ed1b7a85b24a0f22e8e7d9e3ee285742c - DO NOT REMOVE -->
                        
                        ## v0.9.0 - 2024-01-01
                        
                        ## Disclaimer
                        
                        footer text
                        """,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
        );

        try (var inputStream = uut.renderChangelog(changelog, release("v1.0.0"), analyzedCommitsGitmoji())) {
            var actual = inputStream.readAllBytes();

            assertThat(actual).asString(StandardCharsets.UTF_8)
                    .startsWith("# Changelog")
                    .contains("## v1.0.0 - %s".formatted(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE)))
                    .contains("- fix(scm): set clean commit message")
                    .doesNotContain("#42")
                    .contains("## v0.9.0 - 2024-01-01")
                    .contains("## Disclaimer");
        }
    }

    private VersionInfo release(String version) {
        return new VersionInfo(
                version,
                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE),
                "",
                "https:///junit.org/test/v0.0.1...%s".formatted(version)
        );
    }

    private List<AnalyzedCommit> analyzedCommitsConventional() {
        return List.of(
                new AnalyzedCommit(
                        new Commit(DigestUtils.sha256Hex("42"), Instant.EPOCH, "JUnit",
                                """
                                        fix(scm): set clean commit message
                                        
                                        * added scope for commit messages
                                        * some more changes done
                                        
                                        refs #42
                                        """),
                        "https://junit.org/test/%s".formatted(DigestUtils.sha256Hex("42")),
                        "fix(scm): set clean commit message",
                        """
                                * added scope for commit messages
                                * some more changes done
                                """,
                        "refs #42",
                        "fix",
                        ChangeCategory.FIXED,
                        "scm",
                        "set clean commit message",
                        SemVerChangeLevel.PATCH,
                        List.of(new Issue("42", "https://junit.org/test/42"))
                )
        );
    }

    private List<AnalyzedCommit> analyzedCommitsGitmoji() {
        return List.of(
                new AnalyzedCommit(
                        new Commit(DigestUtils.sha256Hex("42"), Instant.EPOCH, "JUnit",
                                "fix(scm): set clean commit message #42"),
                        "https://junit.org/test/%s".formatted(DigestUtils.sha256Hex("42")),
                        "fix(scm): set clean commit message #42",
                        null,
                        null,
                        "fix",
                        ChangeCategory.FIXED,
                        "scm",
                        "set clean commit message",
                        SemVerChangeLevel.PATCH,
                        List.of(new Issue("42", "https://junit.org/test/42"))
                )
        );
    }
}
