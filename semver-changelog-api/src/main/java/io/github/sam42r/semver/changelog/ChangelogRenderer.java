package io.github.sam42r.semver.changelog;

import io.github.sam42r.semver.analyzer.model.AnalyzedCommit;
import io.github.sam42r.semver.changelog.model.VersionInfo;
import lombok.NonNull;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

public interface ChangelogRenderer {

    @NonNull InputStream renderChangelog(
            @NonNull Path path,
            @NonNull VersionInfo versionInfo,
            @NonNull List<AnalyzedCommit> analyzedCommits,
            @NonNull Function<String, String> issueLinkGenerator
            );
}
