package com.github.sam42r.semver.changelog;

import com.github.sam42r.semver.changelog.impl.MustacheRenderer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MustacheRendererTest {

    private ChangelogRenderer uut;

    @BeforeEach
    void setup() {
        uut = new MustacheRenderer();
    }

    @Test
    void shouldCreateChangelogFull(@TempDir Path tempDir) throws IOException {
        var changelog = tempDir.resolve("Changelog.md");
        try (var inputStream = uut.renderChangelog(
                changelog,
                "v1.0.0",
                List.of(),
                List.of(),
                List.of()
        )) {
            var actual = inputStream.readAllBytes();

            assertThat(actual).asString(StandardCharsets.UTF_8)
                    .startsWith("# Changelog")
                    .contains("## v1.0.0 - %s".formatted(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE)))
                    .contains("## Disclaimer");

            // var path = Path.of("Changelog.md");
            // Files.write(path, actual, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }
    }

    @Test
    void shouldUpdateChangelog(@TempDir Path tempDir) throws IOException {
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

        try (var inputStream = uut.renderChangelog(
                changelog,
                "v1.0.0",
                List.of(),
                List.of(),
                List.of()
        )) {
            var actual = inputStream.readAllBytes();

            assertThat(actual).asString(StandardCharsets.UTF_8)
                    .startsWith("# Changelog")
                    .contains("## v1.0.0 - %s".formatted(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE)))
                    .contains("## v0.9.0 - 2024-01-01")
                    .contains("## Disclaimer");

            // var path = Path.of("Changelog.md");
            // Files.write(path, actual, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }
    }
}
