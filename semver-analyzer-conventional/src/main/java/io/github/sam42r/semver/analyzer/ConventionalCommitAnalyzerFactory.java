package io.github.sam42r.semver.analyzer;

import io.github.sam42r.semver.analyzer.model.Configuration;
import lombok.NonNull;

import javax.inject.Named;
import java.util.Optional;

@Named("Conventional")
public class ConventionalCommitAnalyzerFactory implements CommitAnalyzerFactory {

    private static final String DEFAULT_CONFIG = "classpath:/configuration-conventional.yml";

    @Override
    public @NonNull CommitAnalyzer getInstance(String configuration) {
        return new ConventionalCommitAnalyzer(
                Configuration.read(Optional.ofNullable(configuration).orElse(DEFAULT_CONFIG)));
    }
}
