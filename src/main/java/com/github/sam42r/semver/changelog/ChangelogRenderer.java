package com.github.sam42r.semver.changelog;

import com.github.sam42r.semver.scm.model.Commit;
import lombok.NonNull;

import java.io.InputStream;
import java.util.List;

public interface ChangelogRenderer {

    @NonNull InputStream renderChangelog(
            @NonNull String version,
            @NonNull List<Commit> major,
            @NonNull List<Commit> minor,
            @NonNull List<Commit> patch
    );
}
