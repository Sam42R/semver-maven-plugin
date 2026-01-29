package io.github.sam42r.semver.analyzer;

import io.github.sam42r.semver.analyzer.model.AnalyzedCommit;
import io.github.sam42r.semver.analyzer.model.ChangeCategory;
import io.github.sam42r.semver.analyzer.model.SemVerChangeLevel;
import io.github.sam42r.semver.scm.model.Commit;
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
                Commit.builder()
                        .id("42.1")
                        .timestamp(Instant.EPOCH)
                        .author("JUnit")
                        .message(":bug: (scm) set clean commit message #42")
                        .build(),
                Commit.builder()
                        .id("42.2")
                        .timestamp(Instant.EPOCH)
                        .author("JUnit")
                        .message(":sparkles: add awesome things")
                        .build()
        ));

        assertThat(actual).containsExactlyInAnyOrder(
                AnalyzedCommit.builder()
                        .id("42.1")
                        .timestamp(Instant.EPOCH)
                        .author("JUnit")
                        .header(":bug: (scm) set clean commit message #42")
                        .message(":bug: (scm) set clean commit message #42")
                        .type(":bug:")
                        .scope("scm")
                        .subject("set clean commit message")
                        .issues(List.of("42"))
                        .category(ChangeCategory.FIXED)
                        .level(SemVerChangeLevel.PATCH)
                        .build(),
                AnalyzedCommit.builder()
                        .id("42.2")
                        .timestamp(Instant.EPOCH)
                        .author("JUnit")
                        .header(":sparkles: add awesome things")
                        .message(":sparkles: add awesome things")
                        .type(":sparkles:")
                        .subject("add awesome things")
                        .category(ChangeCategory.ADDED)
                        .level(SemVerChangeLevel.MINOR)
                        .build()
        );
    }

    @Test
    void shouldFindBreaking() {
        var actual = uut.analyzeCommits(List.of(
                Commit.builder()
                        .id("42.3")
                        .timestamp(Instant.EPOCH)
                        .author("JUnit")
                        .message(":boom: (void): break some glass #42")
                        .build()
        ));

        assertThat(actual).containsExactly(
                AnalyzedCommit.builder()
                        .id("42.3")
                        .timestamp(Instant.EPOCH)
                        .author("JUnit")
                        .header(":boom: (void): break some glass #42")
                        .message(":boom: (void): break some glass #42")
                        .type(":boom:")
                        .scope("void")
                        .subject("break some glass")
                        .issues(List.of("42"))
                        .category(ChangeCategory.ADDED)
                        .level(SemVerChangeLevel.MAJOR)
                        .build()
        );
    }

    @Test
    void shouldFindOther() {
        var actual = uut.analyzeCommits(List.of(
                Commit.builder()
                        .id("42.4")
                        .timestamp(Instant.EPOCH)
                        .author("JUnit")
                        .message(":white_check_mark: add test for something")
                        .build()
        ));

        assertThat(actual).containsExactly(
                AnalyzedCommit.builder()
                        .id("42.4")
                        .timestamp(Instant.EPOCH)
                        .author("JUnit")
                        .header(":white_check_mark: add test for something")
                        .message(":white_check_mark: add test for something")
                        .type(":white_check_mark:")
                        .subject("add test for something")
                        .category(ChangeCategory.OTHER)
                        .level(SemVerChangeLevel.NONE)
                        .build()
        );
    }
}
