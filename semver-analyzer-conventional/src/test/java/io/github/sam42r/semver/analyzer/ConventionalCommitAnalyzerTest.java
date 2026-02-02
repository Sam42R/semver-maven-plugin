package io.github.sam42r.semver.analyzer;

import io.github.sam42r.semver.model.analyze.AnalyzedCommit;
import io.github.sam42r.semver.model.analyze.ChangeCategory;
import io.github.sam42r.semver.model.analyze.SemVerChangeLevel;
import io.github.sam42r.semver.model.scm.Commit;
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
        ));

        assertThat(actual).containsExactly(
                new AnalyzedCommit(
                        new Commit("42", Instant.EPOCH, "JUnit",
                                """
                                        fix(scm): set clean commit message
                                        
                                        * added scope for commit messages
                                        
                                        refs #42
                                        """),
                        "fix(scm): set clean commit message",
                        "* added scope for commit messages",
                        "refs #42",
                        "fix",
                        ChangeCategory.FIXED,
                        "scm",
                        "set clean commit message",
                        SemVerChangeLevel.PATCH,
                        null)
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
        ));

        assertThat(actual).containsExactly(
                new AnalyzedCommit(
                        new Commit("42", Instant.EPOCH, "JUnit",
                                """
                                        fix(scm): set clean commit message
                                        
                                        * added scope for commit messages
                                        
                                        BREAKING CHANGE: breaks everything
                                        refs #42
                                        """),
                        "fix(scm): set clean commit message",
                        "* added scope for commit messages",
                        "BREAKING CHANGE: breaks everything%srefs #42".formatted(System.lineSeparator()),
                        "fix",
                        ChangeCategory.FIXED,
                        "scm",
                        "set clean commit message",
                        SemVerChangeLevel.MAJOR,
                        null),
                new AnalyzedCommit(
                        new Commit("42", Instant.EPOCH, "JUnit", "fix(scm)!: set clean commit message"),
                        "fix(scm)!: set clean commit message",
                        "",
                        "",
                        "fix",
                        ChangeCategory.FIXED,
                        "scm",
                        "set clean commit message",
                        SemVerChangeLevel.MAJOR,
                        null)
        );
    }
}
