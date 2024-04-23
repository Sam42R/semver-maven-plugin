package com.github.sam42r.semver.analyzer;

import com.github.sam42r.semver.analyzer.model.CommitAnalyzerResponse;
import com.github.sam42r.semver.scm.model.Commit;
import lombok.NonNull;

import java.util.List;

/**
 * A commit message analyzer follows <a href="https://www.conventionalcommits.org/en/v1.0.0/">Conventional Commit</a>
 * specifications to identify the breaking, feat and fix release related commits.
 */
public interface CommitAnalyzer {

    @NonNull String getName();

    @NonNull CommitAnalyzerResponse analyzeCommits(@NonNull List<Commit> commits);
}
