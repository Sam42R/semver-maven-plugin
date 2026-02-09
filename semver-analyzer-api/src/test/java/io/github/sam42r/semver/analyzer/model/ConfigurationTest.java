package io.github.sam42r.semver.analyzer.model;

import io.github.sam42r.semver.model.analyze.AnalyzedCommit;
import io.github.sam42r.semver.model.analyze.ChangeCategory;
import io.github.sam42r.semver.model.analyze.SemVerChangeLevel;
import org.junit.jupiter.api.Test;

import java.util.List;
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
                                new AnalyzedCommit(null, null, null, null, null, "D1", ChangeCategory.DEPRECATED, null, null, SemVerChangeLevel.NONE, null),
                                new AnalyzedCommit(null, null, null, null, null, "R1", ChangeCategory.REMOVED, null, null, SemVerChangeLevel.NONE, null),
                                new AnalyzedCommit(null, null, null, null, null, "F1", ChangeCategory.FIXED, null, null, SemVerChangeLevel.PATCH, null),
                                new AnalyzedCommit(null, null, null, null, null, "S1", ChangeCategory.SECURITY, null, null, SemVerChangeLevel.PATCH, null),
                                new AnalyzedCommit(null, null, null, null, null, "A1", ChangeCategory.ADDED, null, null, SemVerChangeLevel.MINOR, null),
                                new AnalyzedCommit(null, null, null, null, null, "C1", ChangeCategory.CHANGED, null, null, SemVerChangeLevel.MINOR, null),
                                new AnalyzedCommit(null, null, null, null, null, "B1", ChangeCategory.OTHER, null, null, SemVerChangeLevel.MAJOR, null)
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
                                new AnalyzedCommit(null, null, null, null, null, "D1", ChangeCategory.DEPRECATED, null, null, SemVerChangeLevel.NONE, null),
                                new AnalyzedCommit(null, null, null, null, null, "R1", ChangeCategory.REMOVED, null, null, SemVerChangeLevel.NONE, null),
                                new AnalyzedCommit(null, null, null, null, null, "F1", ChangeCategory.FIXED, null, null, SemVerChangeLevel.PATCH, null),
                                new AnalyzedCommit(null, null, null, null, null, "S1", ChangeCategory.SECURITY, null, null, SemVerChangeLevel.PATCH, null),
                                new AnalyzedCommit(null, null, null, null, null, "A1", ChangeCategory.ADDED, null, null, SemVerChangeLevel.MINOR, null),
                                new AnalyzedCommit(null, null, null, null, null, "C1", ChangeCategory.CHANGED, null, null, SemVerChangeLevel.MINOR, null),
                                new AnalyzedCommit(null, null, null, null, null, "B1", ChangeCategory.OTHER, null, null, SemVerChangeLevel.MAJOR, null)
                        )
                );
    }
}
