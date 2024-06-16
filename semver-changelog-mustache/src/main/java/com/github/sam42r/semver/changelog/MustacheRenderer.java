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
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MustacheRenderer implements ChangelogRenderer {

    private static final String CHANGELOG_TEMPLATE = "changelog.mustache";

    private static AnalyzedCommit apply(AnalyzedCommit v) {
        return v;
    }

    @Override
    public @NonNull InputStream renderChangelog(
            @NonNull Path path,
            @NonNull String version,
            @NonNull List<AnalyzedCommit> analyzedCommits
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
            var categorizedCommits = analyzedCommits.stream()
                    .collect(Collectors.toMap(
                            AnalyzedCommit::getCategory,
                            List::of,
                            (v1, v2) -> Stream.of(v1, v2).flatMap(List::stream).toList()
                    ));

            var mustache = mustacheFactory.compile(reader, CHANGELOG_TEMPLATE);
            var context = new HashMap<String, Object>();
            context.put("release", release);

            context.put("hasAdded", categorizedCommits.containsKey(AnalyzedCommit.Category.ADDED));
            context.put("added", categorizedCommits.get(AnalyzedCommit.Category.ADDED));

            context.put("hasChanges", categorizedCommits.containsKey(AnalyzedCommit.Category.CHANGED));
            context.put("changes", categorizedCommits.get(AnalyzedCommit.Category.CHANGED));

            context.put("hasDeprecated", categorizedCommits.containsKey(AnalyzedCommit.Category.DEPRECATED));
            context.put("deprecated", categorizedCommits.get(AnalyzedCommit.Category.DEPRECATED));

            context.put("hasRemoved", categorizedCommits.containsKey(AnalyzedCommit.Category.REMOVED));
            context.put("removed", categorizedCommits.get(AnalyzedCommit.Category.REMOVED));

            context.put("hasPatches", categorizedCommits.containsKey(AnalyzedCommit.Category.FIXED));
            context.put("patches", categorizedCommits.get(AnalyzedCommit.Category.FIXED));

            context.put("hasSecurity", categorizedCommits.containsKey(AnalyzedCommit.Category.SECURITY));
            context.put("securities", categorizedCommits.get(AnalyzedCommit.Category.SECURITY));

            context.put("hasOthers", categorizedCommits.containsKey(AnalyzedCommit.Category.OTHER));
            context.put("others", categorizedCommits.get(AnalyzedCommit.Category.OTHER));

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
