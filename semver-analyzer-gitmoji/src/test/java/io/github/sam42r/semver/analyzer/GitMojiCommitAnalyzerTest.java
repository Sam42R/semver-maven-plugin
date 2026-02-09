package io.github.sam42r.semver.analyzer;

import io.github.sam42r.semver.model.analyze.AnalyzedCommit;
import io.github.sam42r.semver.model.analyze.ChangeCategory;
import io.github.sam42r.semver.model.analyze.Issue;
import io.github.sam42r.semver.model.analyze.SemVerChangeLevel;
import io.github.sam42r.semver.model.release.ProviderSpec;
import io.github.sam42r.semver.model.scm.Commit;
import io.github.sam42r.semver.model.scm.Remote;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GitMojiCommitAnalyzerTest {

    private CommitAnalyzer uut;

    @BeforeEach
    void setup() {
        uut = new GitMojiCommitAnalyzerFactory().getInstance(null);
    }

    @Test
    void shouldFindFixAndFeat() {
        var actual = uut.analyzeCommits(List.of(
                        new Commit("42.1", Instant.EPOCH, "JUnit", ":bug: (scm) set clean commit message #42"),
                        new Commit("42.2", Instant.EPOCH, "JUnit", ":sparkles: add awesome things")
                ),
                Remote.of("git@github.com:Sam42R/semver-maven-plugin.git"),
                new ProviderSpec("%s://%s/%s/%s/%s", "%s://%s/%s/%s/%s")
        );

        assertThat(actual).containsExactlyInAnyOrder(
                new AnalyzedCommit(
                        new Commit("42.1", Instant.EPOCH, "JUnit", ":bug: (scm) set clean commit message #42"),
                        "https://github.com/Sam42R/semver-maven-plugin/42.1",
                        ":bug: (scm) set clean commit message #42",
                        null,
                        null,
                        ":bug:",
                        ChangeCategory.FIXED,
                        "scm",
                        "set clean commit message",
                        SemVerChangeLevel.PATCH,
                        List.of(new Issue("42", "https://github.com/Sam42R/semver-maven-plugin/42"))),
                new AnalyzedCommit(
                        new Commit("42.2", Instant.EPOCH, "JUnit", ":sparkles: add awesome things"),
                        "https://github.com/Sam42R/semver-maven-plugin/42.2",
                        ":sparkles: add awesome things",
                        null,
                        null,
                        ":sparkles:",
                        ChangeCategory.ADDED,
                        null,
                        "add awesome things",
                        SemVerChangeLevel.MINOR,
                        List.of())
        );
    }

    @Test
    void shouldFindBreaking() {
        var actual = uut.analyzeCommits(List.of(
                        new Commit("42.3", Instant.EPOCH, "JUnit", ":boom: (void): break some glass #42")
                ),
                Remote.of("git@github.com:Sam42R/semver-maven-plugin.git"),
                new ProviderSpec("%s://%s/%s/%s/%s", "%s://%s/%s/%s/%s")
        );

        assertThat(actual).containsExactly(
                new AnalyzedCommit(
                        new Commit("42.3", Instant.EPOCH, "JUnit", ":boom: (void): break some glass #42"),
                        "https://github.com/Sam42R/semver-maven-plugin/42.3",
                        ":boom: (void): break some glass #42",
                        null,
                        null,
                        ":boom:",
                        ChangeCategory.ADDED,
                        "void",
                        "break some glass",
                        SemVerChangeLevel.MAJOR,
                        List.of(new Issue("42", "https://github.com/Sam42R/semver-maven-plugin/42")))
        );
    }

    @Test
    void shouldFindOther() {
        var actual = uut.analyzeCommits(List.of(
                        new Commit("42.4", Instant.EPOCH, "JUnit", ":white_check_mark: add test for something")
                ),
                Remote.of("git@github.com:Sam42R/semver-maven-plugin.git"),
                new ProviderSpec("%s://%s/%s/%s/%s", "%s://%s/%s/%s/%s")
        );

        assertThat(actual).containsExactly(
                new AnalyzedCommit(
                        new Commit("42.4", Instant.EPOCH, "JUnit", ":white_check_mark: add test for something"),
                        "https://github.com/Sam42R/semver-maven-plugin/42.4",
                        ":white_check_mark: add test for something",
                        null,
                        null,
                        ":white_check_mark:",
                        ChangeCategory.OTHER,
                        null,
                        "add test for something",
                        SemVerChangeLevel.NONE,
                        List.of())
        );
    }
}
