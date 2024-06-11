package com.github.sam42r.semver.changelog;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.sam42r.semver.analyzer.model.AnalyzedCommit;
import com.github.sam42r.semver.changelog.model.Release;
import lombok.NonNull;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

public class MustacheRenderer implements ChangelogRenderer {

    private static final String CHANGELOG_TEMPLATE = "changelog.mustache";

    @Override
    public @NonNull InputStream renderChangelog(
            @NonNull Path path,
            @NonNull String version,
            @NonNull List<AnalyzedCommit> major,
            @NonNull List<AnalyzedCommit> minor,
            @NonNull List<AnalyzedCommit> patch
    ) {
        var marker = DigestUtils.sha1Hex("Sam42R");

        var release = getRelease(version);

        var alreadyExists = Files.exists(path);

        // TODO
        // Changelog -> docs(changelog): ...

        // Added -> feat
        // Changed -> refactor
        // Deprecated -> DEPRECATED footer
        // Removed -> ???
        // Fixed -> fix
        // Security -> fix(security): ... OR feat(security): ...
        // Other ->

        var mustacheFactory = new DefaultMustacheFactory("com/github/sam42r/semver/changelog");
        try (
                var reader = mustacheFactory.getReader(CHANGELOG_TEMPLATE);
                var outputStream = new ByteArrayOutputStream();
                var writer = new OutputStreamWriter(outputStream);
                var finalOutputStream = new ByteArrayOutputStream();
                var finalWriter = new BufferedWriter(new OutputStreamWriter(finalOutputStream))
        ) {
            var mustache = mustacheFactory.compile(reader, CHANGELOG_TEMPLATE);
            var context = new HashMap<String, Object>();
            context.put("release", release);
            context.put("hasAdded", !minor.isEmpty());
            context.put("added", minor);
            context.put("hasPatches", !patch.isEmpty());
            context.put("patches", patch);
            context.put("renderHeader", !alreadyExists);
            context.put("renderFooter", !alreadyExists);
            mustache.execute(writer, context).flush();

            if (alreadyExists) {
                for (var line : Files.readAllLines(path)) {
                    finalWriter.write(line);
                    finalWriter.newLine();
                    if (line.contains(marker)) {
                        finalWriter.write(outputStream.toString(StandardCharsets.UTF_8));
                    }
                }
            } else {
                finalWriter.write(outputStream.toString(StandardCharsets.UTF_8));
            }
            finalWriter.flush();
            return new ByteArrayInputStream(finalOutputStream.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Release getRelease(String version) {
        return Release.builder()
                .version(version)
                .date(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE))
                .message("") // TODO read docs(changelog) commits and add as release description
                .build();
    }
}
