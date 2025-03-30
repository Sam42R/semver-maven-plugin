package io.github.sam42r.semver.analyzer;

import io.github.sam42r.semver.analyzer.model.AnalyzedCommit;
import io.github.sam42r.semver.scm.model.Commit;
import lombok.NonNull;

import java.util.List;

/**
 * A commit message analyzer checks commit messages to identify the breaking,
 * feat and fix release related commits.
 */
public interface CommitAnalyzer {

    @NonNull List<AnalyzedCommit> analyzeCommits(@NonNull List<Commit> commits);

    @NonNull String generateReleaseCommitMessage(@NonNull String version);
}
