package io.github.sam42r.semver.analyzer;

import io.github.sam42r.semver.analyzer.model.Configuration;
import lombok.NonNull;

import java.util.Optional;

public class GitMojiCommitAnalyzerFactory implements CommitAnalyzerFactory {

    private static final String DEFAULT_CONFIG = "classpath:/configuration-gitmoji.yml";

    private static final String ANALYZER_NAME = "Gitmoji";

    @Override
    public @NonNull String getName() {
        return ANALYZER_NAME;
    }

    @Override
    public @NonNull CommitAnalyzer getInstance(String configuration) {
        return new GitMojiCommitAnalyzer(
                Configuration.read(Optional.ofNullable(configuration).orElse(DEFAULT_CONFIG)));
    }
}
