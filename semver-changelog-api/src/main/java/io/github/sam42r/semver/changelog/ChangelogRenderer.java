package io.github.sam42r.semver.changelog;

import io.github.sam42r.semver.model.changelog.VersionInfo;
import io.github.sam42r.semver.model.analyze.AnalyzedCommit;
import lombok.NonNull;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

public interface ChangelogRenderer {

    @NonNull InputStream renderChangelog(
            @NonNull Path path,
            @NonNull VersionInfo versionInfo,
            @NonNull List<AnalyzedCommit> analyzedCommits
    );
}
