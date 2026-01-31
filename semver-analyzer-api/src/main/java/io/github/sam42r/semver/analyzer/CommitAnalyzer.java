package io.github.sam42r.semver.analyzer;

import io.github.sam42r.semver.model.analyze.AnalyzedCommit;
import io.github.sam42r.semver.model.release.ProviderSpec;
import io.github.sam42r.semver.model.scm.Commit;
import io.github.sam42r.semver.model.scm.Remote;
import lombok.NonNull;

import java.util.List;

/**
 * A commit message analyzer checks commit messages to identify the breaking,
 * feat and fix release related commits.
 */
public interface CommitAnalyzer {

    @NonNull List<AnalyzedCommit> analyzeCommits(@NonNull List<Commit> commits, @NonNull Remote remote, ProviderSpec providerSpec);

    @NonNull String generateReleaseCommitMessage(@NonNull String version);
}
