package io.github.sam42r.semver.analyzer;

import io.github.sam42r.semver.analyzer.model.AnalyzedCommit;
import io.github.sam42r.semver.scm.model.Commit;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * {@link CommitAnalyzer} for
 * <a href="https://gitmoji.dev/specification">GitM&#x1f60e;ji commit message specification</a>.
 */
public class GitMojiCommitAnalyzer implements CommitAnalyzer {

    private static final String ANALYZER_NAME = "Gitmoji";

    private static final String COMMIT_MESSAGE_PATTERN = "(?<INTENTION>(:[a-z\\_]*:))(?<SCOPE>( \\([a-z]*\\)):?)?(?<MESSAGE>[^#]*)(?<REF>(#[0-9]*))?";

    @Override
    public @NonNull String getName() {
        return ANALYZER_NAME;
    }

    @Override
    public @NonNull List<AnalyzedCommit> analyzeCommits(@NonNull List<Commit> commits) {
        return commits.stream().map(this::analyzeCommit).toList();
    }

    @Override
    public @NonNull String generateReleaseCommitMessage(@NonNull String version) {
        return ":bookmark: (release): bump version %s".formatted(version);
    }

    private AnalyzedCommit analyzeCommit(@NonNull Commit commit) {
        var analyzedCommitBuilder = AnalyzedCommit.builder()
                .id(commit.getId())
                .timestamp(commit.getTimestamp())
                .author(commit.getAuthor())
                .header(commit.getMessage().trim())
                .message(commit.getMessage());

        if (commit.getMessage().startsWith(":")) {
            var pattern = Pattern.compile(COMMIT_MESSAGE_PATTERN);
            var matcher = pattern.matcher(commit.getMessage());
            if (matcher.find()) {
                var intention = matcher.group("INTENTION");
                var scope = Optional.ofNullable(matcher.group("SCOPE"))
                        .map(v -> v.replace("(", ""))
                        .map(v -> v.replace(")", ""))
                        .map(v -> v.replace(":", ""))
                        .map(String::trim)
                        .orElse(null);
                var message = matcher.group("MESSAGE").trim();
                var ref = Optional.ofNullable(matcher.group("REF"))
                        .map(v -> v.replace("#", ""))
                        .map(String::trim)
                        .map(List::of)
                        .orElse(null);
                analyzedCommitBuilder
                        .type(intention)
                        .scope(scope)
                        .subject(message)
                        .issues(ref)
                        .breaking(":boom:".equals(intention))
                        .category(getCategory(intention));
            }
        }
        return analyzedCommitBuilder.build();
    }

    private AnalyzedCommit.Category getCategory(String intention) {
        if (intention != null) {
            return switch (intention.toLowerCase()) {
                case ":sparkles:", ":boom:" -> AnalyzedCommit.Category.ADDED;
                case ":art:", ":recycle:" -> AnalyzedCommit.Category.CHANGED;
                case ":wastebasket:" -> AnalyzedCommit.Category.DEPRECATED;
                case ":fire:", ":coffin:" -> AnalyzedCommit.Category.REMOVED;
                case ":bug:", ":ambulance:" -> AnalyzedCommit.Category.FIXED;
                case ":lock:" -> AnalyzedCommit.Category.SECURITY;
                default -> AnalyzedCommit.Category.OTHER;
            };
        }
        return AnalyzedCommit.Category.OTHER;
    }
}
