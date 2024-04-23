package com.github.sam42r.semver.analyzer.impl;

import com.github.sam42r.semver.analyzer.CommitAnalyzer;
import com.github.sam42r.semver.analyzer.model.CommitAnalyzerResponse;
import com.github.sam42r.semver.scm.model.Commit;
import lombok.NonNull;

import java.util.List;

/**
 * {@link CommitAnalyzer} for
 * <a href="https://github.com/angular/angular/blob/main/CONTRIBUTING.md#commit">Angular Commit Message Format</a>
 */
public class AngularCommitAnalyzer implements CommitAnalyzer {

    private static final String ANALYZER_NAME = "Angular";

    @Override
    public @NonNull String getName() {
        return ANALYZER_NAME;
    }

    @Override
    public @NonNull CommitAnalyzerResponse analyzeCommits(@NonNull List<Commit> commits) {
        var fixes = commits.stream()
                .filter(c -> c.getMessage().startsWith("fix"))
                .toList();
        var features = commits.stream()
                .filter(c -> c.getMessage().startsWith("feat"))
                .toList();
        var breakingChanges = commits.stream()
                .filter(c -> c.getMessage().contains("BREAKING CHANGE"))
                .toList();

        return new CommitAnalyzerResponse(breakingChanges, features, fixes);
    }
}
