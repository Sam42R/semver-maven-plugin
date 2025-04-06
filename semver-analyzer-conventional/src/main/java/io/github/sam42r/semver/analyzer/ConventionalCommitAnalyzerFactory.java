package io.github.sam42r.semver.analyzer;

import io.github.sam42r.semver.analyzer.model.Configuration;
import lombok.NonNull;

import java.util.Optional;

public class ConventionalCommitAnalyzerFactory implements CommitAnalyzerFactory {

    private static final String DEFAULT_CONFIG = "classpath:/configuration-conventional.yml";

    private static final String ANALYZER_NAME = "Conventional";

    @Override
    public @NonNull String getName() {
        return ANALYZER_NAME;
    }

    @Override
    public @NonNull CommitAnalyzer getInstance(String configuration) {
        return new ConventionalCommitAnalyzer(
                Configuration.read(Optional.ofNullable(configuration).orElse(DEFAULT_CONFIG)));
    }
}
