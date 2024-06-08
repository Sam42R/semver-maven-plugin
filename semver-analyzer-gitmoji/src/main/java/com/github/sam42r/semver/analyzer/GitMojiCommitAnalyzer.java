package com.github.sam42r.semver.analyzer;

import com.github.sam42r.semver.analyzer.model.AnalyzedCommit;
import com.github.sam42r.semver.scm.model.Commit;
import lombok.NonNull;
import org.apache.commons.lang3.NotImplementedException;

import java.util.List;

/**
 * {@link com.github.sam42r.semver.analyzer.CommitAnalyzer} for
 * <a href="https://gitmoji.dev/specification">GitM&#x1f60e;ji commit message specification</a>.
 */
public class GitMojiCommitAnalyzer implements CommitAnalyzer {

    private static final String ANALYZER_NAME = "Gitmoji";

    @Override
    public @NonNull String getName() {
        return ANALYZER_NAME;
    }

    @Override
    public @NonNull List<AnalyzedCommit> analyzeCommits(@NonNull List<Commit> commits) {
        throw new NotImplementedException();
    }

    @Override
    public @NonNull String generateReleaseCommitMessage(@NonNull String version) {
        return ":bookmark: (release): bump version %s".formatted(version);
    }
}
