package io.github.sam42r.semver.analyzer;

import io.github.sam42r.semver.analyzer.model.Configuration;
import lombok.NonNull;

import javax.inject.Named;
import java.util.Optional;

@Named("Gitmoji")
public class GitMojiCommitAnalyzerFactory implements CommitAnalyzerFactory {

    private static final String DEFAULT_CONFIG = "classpath:/configuration-gitmoji.yml";

    @Override
    public @NonNull CommitAnalyzer getInstance(String configuration) {
        return new GitMojiCommitAnalyzer(
                Configuration.read(Optional.ofNullable(configuration).orElse(DEFAULT_CONFIG)));
    }
}
