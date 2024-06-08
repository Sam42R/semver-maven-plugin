package com.github.sam42r.semver.analyzer;

import com.github.sam42r.semver.analyzer.model.AnalyzedCommit;
import com.github.sam42r.semver.scm.model.Commit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ConventionalCommitAnalyzerTest {

    private CommitAnalyzer uut;

    @BeforeEach
    void setup() {
        uut = new ConventionalCommitAnalyzer();
    }

    @Test
    void shouldFindFix() {
        var actual = uut.analyzeCommits(List.of(
                Commit.builder()
                        .id("42")
                        .timestamp(Instant.EPOCH)
                        .author("JUnit")
                        .message("""
                                fix(scm): set clean commit message
                                                                
                                * added scope for commit messages
                                                                
                                refs #42
                                """)
                        .build()
        ));

        assertThat(actual).isEqualTo(List.of(
                AnalyzedCommit.builder()
                        .id("42")
                        .timestamp(Instant.EPOCH)
                        .author("JUnit")
                        .message("""
                                fix(scm): set clean commit message
                                                                
                                * added scope for commit messages
                                                                
                                refs #42
                                """)
                        .type(AnalyzedCommit.Type.FIX)
                        .header("fix(scm): set clean commit message")
                        .body("* added scope for commit messages")
                        .footer("refs #42")
                        .build()
        ));
    }
}
