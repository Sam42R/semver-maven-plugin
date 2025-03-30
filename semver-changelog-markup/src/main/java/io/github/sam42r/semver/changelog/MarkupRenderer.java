package io.github.sam42r.semver.changelog;

import com.github.mustachejava.DefaultMustacheFactory;
import io.github.sam42r.semver.analyzer.model.AnalyzedCommit;
import io.github.sam42r.semver.analyzer.model.ChangeCategory;
import io.github.sam42r.semver.changelog.model.VersionInfo;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class MarkupRenderer implements ChangelogRenderer {

    private static final String CHANGELOG_TEMPLATE = "%s.mustache";

    private final String template;

    @Override
    public @NonNull InputStream renderChangelog(
            @NonNull Path path,
            @NonNull VersionInfo versionInfo,
            @NonNull List<AnalyzedCommit> analyzedCommits
    ) {
        var marker = DigestUtils.sha1Hex("Sam42R");

        var alreadyExists = Files.exists(path);

        // TODO
        // Changelog -> docs(changelog): ...

        // Added -> feat
        // Changed -> refactor
        // Deprecated -> DEPRECATED footer
        // Removed -> ???
        // Fixed -> fix
        // Security -> fix(security): ... OR feat(security): ...
        // Other -> all others

        var mustacheFactory = new DefaultMustacheFactory("io/github/sam42r/semver/changelog");
        try (
                var reader = mustacheFactory.getReader(CHANGELOG_TEMPLATE.formatted(template));
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

            var mustache = mustacheFactory.compile(reader, CHANGELOG_TEMPLATE.formatted(template));
            var context = new HashMap<String, Object>();
            context.put("release", versionInfo);

            context.put("hasAdded", categorizedCommits.containsKey(ChangeCategory.ADDED));
            context.put("added", categorizedCommits.get(ChangeCategory.ADDED));

            context.put("hasChanges", categorizedCommits.containsKey(ChangeCategory.CHANGED));
            context.put("changes", categorizedCommits.get(ChangeCategory.CHANGED));

            context.put("hasDeprecated", categorizedCommits.containsKey(ChangeCategory.DEPRECATED));
            context.put("deprecated", categorizedCommits.get(ChangeCategory.DEPRECATED));

            context.put("hasRemoved", categorizedCommits.containsKey(ChangeCategory.REMOVED));
            context.put("removed", categorizedCommits.get(ChangeCategory.REMOVED));

            context.put("hasPatches", categorizedCommits.containsKey(ChangeCategory.FIXED));
            context.put("patches", categorizedCommits.get(ChangeCategory.FIXED));

            context.put("hasSecurity", categorizedCommits.containsKey(ChangeCategory.SECURITY));
            context.put("securities", categorizedCommits.get(ChangeCategory.SECURITY));

            context.put("hasOthers", categorizedCommits.containsKey(ChangeCategory.OTHER));
            context.put("others", categorizedCommits.get(ChangeCategory.OTHER));

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
}
