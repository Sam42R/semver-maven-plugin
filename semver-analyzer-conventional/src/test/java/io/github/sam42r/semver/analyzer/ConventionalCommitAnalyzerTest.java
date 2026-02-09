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

class ConventionalCommitAnalyzerTest {

    private CommitAnalyzer uut;

    @BeforeEach
    void setup() {
        uut = new ConventionalCommitAnalyzerFactory().getInstance(null);
    }

    @Test
    void shouldFindFix() {
        var actual = uut.analyzeCommits(List.of(
                        new Commit("42", Instant.EPOCH, "JUnit",
                                """
                                        fix(scm): set clean commit message
                                        
                                        * added scope for commit messages
                                        
                                        refs #42
                                        """)
                ),
                Remote.of("git@github.com:Sam42R/semver-maven-plugin.git"),
                new ProviderSpec("%s://%s/%s/%s/%s", "%s://%s/%s/%s/%s")
        );

        assertThat(actual).containsExactly(
                new AnalyzedCommit(
                        new Commit("42", Instant.EPOCH, "JUnit",
                                """
                                        fix(scm): set clean commit message
                                        
                                        * added scope for commit messages
                                        
                                        refs #42
                                        """),
                        "https://github.com/Sam42R/semver-maven-plugin/42",
                        "fix(scm): set clean commit message",
                        "* added scope for commit messages",
                        "refs #42",
                        "fix",
                        ChangeCategory.FIXED,
                        "scm",
                        "set clean commit message",
                        SemVerChangeLevel.PATCH,
                        List.of(new Issue("42", "https://github.com/Sam42R/semver-maven-plugin/42")))
        );
    }

    @Test
    void shouldFindBreakingChanges() {
        var actual = uut.analyzeCommits(List.of(
                        new Commit("42", Instant.EPOCH, "JUnit",
                                """
                                        fix(scm): set clean commit message
                                        
                                        * added scope for commit messages
                                        
                                        BREAKING CHANGE: breaks everything
                                        refs #42
                                        """),
                        new Commit("42", Instant.EPOCH, "JUnit",
                                "fix(scm)!: set clean commit message")
                ),
                Remote.of("git@github.com:Sam42R/semver-maven-plugin.git"),
                new ProviderSpec("%s://%s/%s/%s/%s", "%s://%s/%s/%s/%s")
        );

        assertThat(actual).containsExactly(
                new AnalyzedCommit(
                        new Commit("42", Instant.EPOCH, "JUnit",
                                """
                                        fix(scm): set clean commit message
                                        
                                        * added scope for commit messages
                                        
                                        BREAKING CHANGE: breaks everything
                                        refs #42
                                        """),
                        "https://github.com/Sam42R/semver-maven-plugin/42",
                        "fix(scm): set clean commit message",
                        "* added scope for commit messages",
                        "BREAKING CHANGE: breaks everything%srefs #42".formatted(System.lineSeparator()),
                        "fix",
                        ChangeCategory.FIXED,
                        "scm",
                        "set clean commit message",
                        SemVerChangeLevel.MAJOR,
                        List.of(new Issue("42", "https://github.com/Sam42R/semver-maven-plugin/42"))),
                new AnalyzedCommit(
                        new Commit("42", Instant.EPOCH, "JUnit", "fix(scm)!: set clean commit message"),
                        "https://github.com/Sam42R/semver-maven-plugin/42",
                        "fix(scm)!: set clean commit message",
                        "",
                        "",
                        "fix",
                        ChangeCategory.FIXED,
                        "scm",
                        "set clean commit message",
                        SemVerChangeLevel.MAJOR,
                        List.of())
        );
    }
}
