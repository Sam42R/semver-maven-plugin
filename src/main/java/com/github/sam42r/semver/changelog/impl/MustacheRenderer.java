package com.github.sam42r.semver.changelog.impl;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.sam42r.semver.changelog.ChangelogRenderer;
import com.github.sam42r.semver.changelog.model.Release;
import com.github.sam42r.semver.scm.model.Commit;
import lombok.NonNull;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class MustacheRenderer implements ChangelogRenderer {

    private static final String CHANGELOG_TEMPLATE = "changelog.mustache";

    @Override
    public @NonNull InputStream renderChangelog(
            @NonNull String version,
            @NonNull List<Commit> major,
            @NonNull List<Commit> minor,
            @NonNull List<Commit> patch
    ) {
        var release = getRelease(version);

        // TODO
        // Changelog -> docs(changelog): ...
        // Added -> feat
        // Changed -> refactor
        // Deprecated -> DEPRECATED footer
        // Removed -> ???
        // Fixed -> fix
        // Security -> fix(security): ... OR feat(security): ...

        var mustacheFactory = new DefaultMustacheFactory();
        try (
                var inputStream = MustacheRenderer.class.getResourceAsStream(CHANGELOG_TEMPLATE);
                var reader = new InputStreamReader(Objects.requireNonNull(inputStream));
                var outputStream = new ByteArrayOutputStream();
                var writer = new OutputStreamWriter(outputStream)
        ) {
            var mustache = mustacheFactory.compile(reader, CHANGELOG_TEMPLATE);
            var context = new HashMap<String, Object>();
            context.put("release", release);
            context.put("hasAdded", !minor.isEmpty());
            context.put("added", minor);
            context.put("hasPatches", !patch.isEmpty());
            context.put("patches", patch);
            mustache.execute(writer, context).flush();
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Release getRelease(String version) {
        return Release.builder()
                .version(version)
                .date(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE))
                .message("TODO read docs(changelog) commits and add as release description")
                .build();
    }
}
