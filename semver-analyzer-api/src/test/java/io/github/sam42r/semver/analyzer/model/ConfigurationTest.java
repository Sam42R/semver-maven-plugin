package io.github.sam42r.semver.analyzer.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigurationTest {

    @Test
    void shouldReadConfigFromClasspath() {
        var actual = Configuration.read("classpath:/configuration.yml");

        assertThat(actual).extracting(
                        Configuration::getRelease, Configuration::getItems)
                .containsExactly(
                        "R0", List.of(
                                AnalyzedCommit.builder().type("D1").category(ChangeCategory.DEPRECATED).level(SemVerChangeLevel.NONE).build(),
                                AnalyzedCommit.builder().type("R1").category(ChangeCategory.REMOVED).level(SemVerChangeLevel.NONE).build(),
                                AnalyzedCommit.builder().type("F1").category(ChangeCategory.FIXED).level(SemVerChangeLevel.PATCH).build(),
                                AnalyzedCommit.builder().type("S1").category(ChangeCategory.SECURITY).level(SemVerChangeLevel.PATCH).build(),
                                AnalyzedCommit.builder().type("A1").category(ChangeCategory.ADDED).level(SemVerChangeLevel.MINOR).build(),
                                AnalyzedCommit.builder().type("C1").category(ChangeCategory.CHANGED).level(SemVerChangeLevel.MINOR).build(),
                                AnalyzedCommit.builder().type("B1").category(ChangeCategory.OTHER).level(SemVerChangeLevel.MAJOR).build()
                        )
                );
    }

    @Test
    void shouldReadConfigFromFile() {
        var actual = Configuration.read(
                Objects.requireNonNull(ConfigurationTest.class.getResource("/configuration.yml")).getFile()
        );

        assertThat(actual).extracting(
                        Configuration::getRelease, Configuration::getItems)
                .containsExactly(
                        "R0", List.of(
                                AnalyzedCommit.builder().type("D1").category(ChangeCategory.DEPRECATED).level(SemVerChangeLevel.NONE).build(),
                                AnalyzedCommit.builder().type("R1").category(ChangeCategory.REMOVED).level(SemVerChangeLevel.NONE).build(),
                                AnalyzedCommit.builder().type("F1").category(ChangeCategory.FIXED).level(SemVerChangeLevel.PATCH).build(),
                                AnalyzedCommit.builder().type("S1").category(ChangeCategory.SECURITY).level(SemVerChangeLevel.PATCH).build(),
                                AnalyzedCommit.builder().type("A1").category(ChangeCategory.ADDED).level(SemVerChangeLevel.MINOR).build(),
                                AnalyzedCommit.builder().type("C1").category(ChangeCategory.CHANGED).level(SemVerChangeLevel.MINOR).build(),
                                AnalyzedCommit.builder().type("B1").category(ChangeCategory.OTHER).level(SemVerChangeLevel.MAJOR).build()
                        )
                );
    }
}
