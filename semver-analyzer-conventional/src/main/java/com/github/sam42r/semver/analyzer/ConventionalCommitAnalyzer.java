package com.github.sam42r.semver.analyzer;

import com.github.sam42r.semver.analyzer.model.AnalyzedCommit;
import com.github.sam42r.semver.scm.model.Commit;
import lombok.NonNull;

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
public class ConventionalCommitAnalyzer implements CommitAnalyzer {

    private static final String ANALYZER_NAME = "Conventional";

    private static final String COMMIT_HEADER_PATTERN = "(?<TYPE>([a-z]*))(?<SCOPE>(\\([a-z]*\\)))?(?<DESCRIPTION>(: .*))";

    @Override
    public @NonNull String getName() {
        return ANALYZER_NAME;
    }

    @Override
    public @NonNull String generateReleaseCommitMessage(@NonNull String version) {
        return "chore(release): release version %s".formatted(version);
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
                var description = matcher.group("DESCRIPTION").replaceFirst(":", "").trim();

                analyzedCommitBuilder
                        .type(type)
                        .scope(scope)
                        .subject(description)
                        .category(getCategory(type));
            }
        }

        if (!footer.isEmpty()) {
            analyzedCommitBuilder.breaking(footer.contains("BREAKING CHANGE"));
            // TODO search for issues in footer
        }

        return analyzedCommitBuilder.build();
    }

    private AnalyzedCommit.Category getCategory(String type) {
        if (type != null) {
            return switch (type.toLowerCase()) {
                case "feat" -> AnalyzedCommit.Category.ADDED;
                case "refactor" -> AnalyzedCommit.Category.CHANGED;
                // TODO DEPRECATED footer!?
                //case "TODO" -> AnalyzedCommit.Category.DEPRECATED;
                // TODO how to with conventional specification!?
                //case "TODO" -> AnalyzedCommit.Category.REMOVED;
                case "fix" -> AnalyzedCommit.Category.FIXED;
                // TODO fix(security) | feat(security) !?
                //case "TODO" -> AnalyzedCommit.Category.SECURITY;
                default -> AnalyzedCommit.Category.OTHER;
            };
        }
        return AnalyzedCommit.Category.OTHER;
    }
}
