package io.github.sam42r.semver.analyzer;

import io.github.sam42r.semver.analyzer.model.AnalyzedCommit;
import io.github.sam42r.semver.analyzer.model.ChangeCategory;
import io.github.sam42r.semver.analyzer.model.Configuration;
import io.github.sam42r.semver.analyzer.model.SemVerChangeLevel;
import io.github.sam42r.semver.scm.model.Commit;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * {@link CommitAnalyzer} for
 * <a href="https://www.conventionalcommits.org/en/v1.0.0/">Conventional Commits specification</a>.
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ConventionalCommitAnalyzer implements CommitAnalyzer {

    private static final String COMMIT_HEADER_PATTERN = "(?<TYPE>([a-z]*))(?<SCOPE>(\\([a-z]*\\)))?(?<BREAKING>(!))?(?<DESCRIPTION>(: .*))";

    private final Configuration configuration;

    @Override
    public @NonNull String generateReleaseCommitMessage(@NonNull String version) {
        return "%s: release version %s".formatted(configuration.getRelease(), version);
    }

    @Override
    public @NonNull List<AnalyzedCommit> analyzeCommits(@NonNull List<Commit> commits) {
        return commits.stream().map(this::analyzeCommit).toList();
    }


    @SuppressWarnings("MismatchedQueryAndUpdateOfStringBuilder")
    private AnalyzedCommit analyzeCommit(@NonNull Commit commit) {
        var analyzedCommitBuilder = AnalyzedCommit.builder()
                .id(commit.getId())
                .timestamp(commit.getTimestamp())
                .author(commit.getAuthor())
                .message(commit.getMessage());

        var headerBuilder = new StringBuilder();
        var bodyBuilder = new StringBuilder();
        var footerBuilder = new StringBuilder();
        try (var reader = new BufferedReader(new StringReader(commit.getMessage()))) {
            String line;
            int emptyLinesCounter = 0;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    emptyLinesCounter++;
                }

                var stringBuilder = switch (emptyLinesCounter) {
                    case 0 -> headerBuilder;
                    case 1 -> bodyBuilder;
                    default -> footerBuilder;
                };

                stringBuilder.append(line);
                stringBuilder.append(System.lineSeparator());
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        var header = headerBuilder.toString().trim();
        var body = bodyBuilder.toString().trim();
        var footer = footerBuilder.toString().trim();
        analyzedCommitBuilder
                .header(header)
                .body(body)
                .footer(footer);

        if (!header.isEmpty()) {
            var pattern = Pattern.compile(COMMIT_HEADER_PATTERN);
            var matcher = pattern.matcher(header);
            if (matcher.find()) {
                var type = matcher.group("TYPE");
                var scope = Optional.ofNullable(matcher.group("SCOPE"))
                        .map(v -> v.replace("(", ""))
                        .map(v -> v.replace(")", ""))
                        .orElse(null);
                var breaking = Optional.ofNullable(matcher.group("BREAKING"));
                var description = matcher.group("DESCRIPTION").replaceFirst(":", "").trim();

                analyzedCommitBuilder
                        .type(type)
                        .scope(scope)
                        .subject(description)
                        .category(getCategory(type))
                        .level(breaking.isPresent() ? SemVerChangeLevel.MAJOR : getLevel(type));
            }
        }

        if (footer.contains("BREAKING CHANGE")) {
            analyzedCommitBuilder.level(SemVerChangeLevel.MAJOR);
        }

        // TODO search for issues in footer

        return analyzedCommitBuilder.build();
    }

    private ChangeCategory getCategory(String type) {
        if (type != null) {
            return configuration.getItems().stream()
                    .filter(v -> v.getType().equals(type))
                    .findAny()
                    .map(AnalyzedCommit::getCategory)
                    .orElse(ChangeCategory.OTHER);
        }
        return ChangeCategory.OTHER;
    }

    private SemVerChangeLevel getLevel(String type) {
        if (type != null) {
            return configuration.getItems().stream()
                    .filter(v -> v.getType().equals(type))
                    .findAny()
                    .map(AnalyzedCommit::getLevel)
                    .orElse(SemVerChangeLevel.NONE);
        }
        return SemVerChangeLevel.NONE;
    }
}
