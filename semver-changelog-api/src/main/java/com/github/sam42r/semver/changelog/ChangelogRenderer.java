package com.github.sam42r.semver.changelog;

import com.github.sam42r.semver.analyzer.model.AnalyzedCommit;
import com.github.sam42r.semver.scm.model.Commit;
import lombok.NonNull;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

public interface ChangelogRenderer {

    @NonNull InputStream renderChangelog(
            @NonNull Path path,
            @NonNull String version,
            @NonNull List<AnalyzedCommit> analyzedCommits
    );
}
