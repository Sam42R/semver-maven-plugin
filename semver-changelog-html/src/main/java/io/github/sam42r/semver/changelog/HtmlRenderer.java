package io.github.sam42r.semver.changelog;

import io.github.sam42r.semver.analyzer.model.AnalyzedCommit;
import io.github.sam42r.semver.changelog.model.VersionInfo;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RequiredArgsConstructor
public class HtmlRenderer implements ChangelogRenderer {

    private final String template;

    @Override
    public @NonNull InputStream renderChangelog(
            @NonNull Path path,
            @NonNull VersionInfo versionInfo,
            @NonNull List<AnalyzedCommit> analyzedCommits
    ) {
        var marker = DigestUtils.sha1Hex("Sam42R");

        var alreadyExists = Files.exists(path);

        var templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("/templates/%s/".formatted(template));
        templateResolver.setSuffix(".html");

        var templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
        try (
                var outputStream = new ByteArrayOutputStream();
                var writer = new OutputStreamWriter(outputStream);
                var finalOutputStream = new ByteArrayOutputStream();
                var finalWriter = new BufferedWriter(new OutputStreamWriter(finalOutputStream))
        ) {
            var context = new Context();

            context.setVariable("release", versionInfo);
            context.setVariable("commits", analyzedCommits);

            templateEngine.process(template, context, writer);

            if (alreadyExists) {
                var markerCount = 0;
                for (var line : Files.readAllLines(path)) {
                    finalWriter.write(line);
                    finalWriter.newLine();
                    if (line.contains(marker)) {
                        markerCount++;
                        if (markerCount == 1) {
                            writePartial(finalWriter, new ByteArrayInputStream(outputStream.toByteArray()), marker);
                        }
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

    private void writePartial(Writer writer, InputStream inputStream, String marker) throws IOException {
        try (var reader = new BufferedReader(new InputStreamReader(inputStream))) {
            var markerCount = 0;
            for (var line : reader.lines().toList()) {
                if (line.contains(marker)) {
                    markerCount++;
                } else {
                    if (markerCount == 1) {
                        writer.write(line);
                        writer.append(System.lineSeparator());
                    }
                }
            }
        }
    }
}
