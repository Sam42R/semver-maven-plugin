package io.github.sam42r.semver.changelog;

import com.github.mustachejava.DefaultMustacheFactory;
import io.github.sam42r.semver.changelog.model.Link;
import io.github.sam42r.semver.changelog.model.RenderedCommit;
import io.github.sam42r.semver.model.analyze.AnalyzedCommit;
import io.github.sam42r.semver.model.analyze.ChangeCategory;
import io.github.sam42r.semver.model.changelog.VersionInfo;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
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
                            AnalyzedCommit::category,
                            List::of,
                            (v1, v2) -> Stream.of(v1, v2).flatMap(List::stream).toList()
                    ));

            var mustache = mustacheFactory.compile(reader, CHANGELOG_TEMPLATE.formatted(template));
            var context = new HashMap<String, Object>();
            context.put("release", versionInfo);

            context.put("hasAdded", categorizedCommits.containsKey(ChangeCategory.ADDED));
            context.put("added", categorizedCommits.getOrDefault(ChangeCategory.ADDED, Collections.emptyList())
                    .stream().map(this::renderedCommit).toList());

            context.put("hasChanges", categorizedCommits.containsKey(ChangeCategory.CHANGED));
            context.put("changes", categorizedCommits.getOrDefault(ChangeCategory.CHANGED, Collections.emptyList())
                    .stream().map(this::renderedCommit).toList());

            context.put("hasDeprecated", categorizedCommits.containsKey(ChangeCategory.DEPRECATED));
            context.put("deprecated", categorizedCommits.getOrDefault(ChangeCategory.DEPRECATED, Collections.emptyList())
                    .stream().map(this::renderedCommit).toList());

            context.put("hasRemoved", categorizedCommits.containsKey(ChangeCategory.REMOVED));
            context.put("removed", categorizedCommits.getOrDefault(ChangeCategory.REMOVED, Collections.emptyList())
                    .stream().map(this::renderedCommit).toList());

            context.put("hasPatches", categorizedCommits.containsKey(ChangeCategory.FIXED));
            context.put("patches", categorizedCommits.getOrDefault(ChangeCategory.FIXED, Collections.emptyList())
                    .stream().map(this::renderedCommit).toList());

            context.put("hasSecurity", categorizedCommits.containsKey(ChangeCategory.SECURITY));
            context.put("securities", categorizedCommits.getOrDefault(ChangeCategory.SECURITY, Collections.emptyList())
                    .stream().map(this::renderedCommit).toList());

            context.put("hasOthers", categorizedCommits.containsKey(ChangeCategory.OTHER));
            context.put("others", categorizedCommits.getOrDefault(ChangeCategory.OTHER, Collections.emptyList())
                    .stream().map(this::renderedCommit).toList());

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
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Transform {@link AnalyzedCommit} to Mustache compliant {@link RenderedCommit}.<br/>
     * (Since Mustache is more or less logic less we have to use rendering optimized model)
     *
     * @param analyzedCommit the {@link AnalyzedCommit} to transform
     * @return Mustache compliant {@link RenderedCommit}
     */
    private RenderedCommit renderedCommit(AnalyzedCommit analyzedCommit) {
        return new RenderedCommit(
                analyzedCommit.header(),
                analyzedCommit.url() == null || analyzedCommit.url().isBlank() ?
                        null :
                        new Link(trimToLength(analyzedCommit.commit().id(), 7), analyzedCommit.url()),
                analyzedCommit.issues() == null || analyzedCommit.issues().isEmpty() ?
                        Collections.emptyList() :
                        analyzedCommit.issues().stream()
                                .filter(v -> v.url() != null && !v.url().isBlank())
                                .map(v -> new Link(v.id(), v.url()))
                                .toList()
        );
    }

    private String trimToLength(String string, int length) {
        if (string != null && string.length() > length) {
            return string.substring(0, length);
        }
        return string;
    }
}
