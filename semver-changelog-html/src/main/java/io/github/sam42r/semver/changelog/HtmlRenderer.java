package io.github.sam42r.semver.changelog;

import io.github.sam42r.semver.analyzer.model.AnalyzedCommit;
import lombok.NonNull;
import org.apache.commons.lang3.NotImplementedException;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

public class HtmlRenderer implements ChangelogRenderer {

    private static final String RENDERER_NAME = "Html";

    @Override
    public @NonNull String getName() {
        return RENDERER_NAME;
    }

    @Override
    public @NonNull InputStream renderChangelog(
            @NonNull Path path,
            @NonNull String version,
            @NonNull List<AnalyzedCommit> analyzedCommits
    ) {
        throw new NotImplementedException();
    }
}
