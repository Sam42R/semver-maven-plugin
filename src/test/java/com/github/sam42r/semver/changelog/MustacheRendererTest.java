package com.github.sam42r.semver.changelog;

import com.github.sam42r.semver.changelog.impl.MustacheRenderer;
import com.github.sam42r.semver.scm.model.Commit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MustacheRendererTest {

    private ChangelogRenderer uut;

    @BeforeEach
    void setup() {
        uut = new MustacheRenderer();
    }

    @Test
    void shouldRenderChangelog() throws IOException {
        var actual = uut.renderChangelog(
                "v1.0.0",
                List.of(),
                List.of(),
                List.of()
        );

        assertThat(actual).asString(StandardCharsets.UTF_8)
                .startsWith("# Changelog");
    }
}
