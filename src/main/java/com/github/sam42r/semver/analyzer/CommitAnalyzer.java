package com.github.sam42r.semver.analyzer;

import com.github.sam42r.semver.analyzer.model.AnalyzedCommit;
import com.github.sam42r.semver.scm.model.Commit;
import lombok.NonNull;

import java.util.List;

/**
 * A commit message analyzer checks commit messages to identify the breaking,
 * feat and fix release related commits.
 */
public interface CommitAnalyzer {

    @NonNull String getName();

    @NonNull List<AnalyzedCommit> analyzeCommits(@NonNull List<Commit> commits);

    @NonNull String generateReleaseCommitMessage(@NonNull String version);
}
