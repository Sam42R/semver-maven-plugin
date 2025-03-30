package io.github.sam42r.semver.analyzer;

import io.github.sam42r.semver.analyzer.model.Configuration;
import lombok.NonNull;

public class ConventionalCommitAnalyzerFactory implements CommitAnalyzerFactory {

    private static final String ANALYZER_NAME = "Conventional";

    @Override
    public @NonNull String getName() {
        return ANALYZER_NAME;
    }

    @Override
    public @NonNull CommitAnalyzer getInstance(@NonNull String configuration) {
        return new ConventionalCommitAnalyzer(Configuration.read(configuration));
    }
}
