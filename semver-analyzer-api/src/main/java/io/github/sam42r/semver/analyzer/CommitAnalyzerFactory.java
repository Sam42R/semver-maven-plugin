package io.github.sam42r.semver.analyzer;

import lombok.NonNull;

public interface CommitAnalyzerFactory {

    @NonNull CommitAnalyzer getInstance(String configuration);
}
