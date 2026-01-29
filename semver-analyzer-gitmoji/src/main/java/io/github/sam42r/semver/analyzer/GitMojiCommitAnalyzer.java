package io.github.sam42r.semver.analyzer;

import io.github.sam42r.semver.analyzer.model.AnalyzedCommit;
import io.github.sam42r.semver.analyzer.model.ChangeCategory;
import io.github.sam42r.semver.analyzer.model.Configuration;
import io.github.sam42r.semver.analyzer.model.SemVerChangeLevel;
import io.github.sam42r.semver.scm.model.Commit;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * {@link CommitAnalyzer} for
 * <a href="https://gitmoji.dev/specification">GitM&#x1f60e;ji commit message specification</a>.
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class GitMojiCommitAnalyzer implements CommitAnalyzer {

    private static final String COMMIT_MESSAGE_PATTERN = "(?<INTENTION>(:[a-z_]*:))(?<SCOPE>( \\([a-z]*\\)):?)?(?<MESSAGE>[^#]*)(?<REF>(#\\d*))?";

    private final Configuration configuration;

    @Override
    public @NonNull List<AnalyzedCommit> analyzeCommits(@NonNull List<Commit> commits) {
        return commits.stream().map(this::analyzeCommit).toList();
    }

    @Override
    public @NonNull String generateReleaseCommitMessage(@NonNull String version) {
        return "%s: bump version %s".formatted(configuration.getRelease(), version);
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
                        .category(getCategory(intention))
                        .level(getLevel(intention));
            }
        }
        return analyzedCommitBuilder.build();
    }

    private ChangeCategory getCategory(String intention) {
        if (intention != null) {
            return configuration.getItems().stream()
                    .filter(v -> v.getType().equals(intention))
                    .findAny()
                    .map(AnalyzedCommit::getCategory)
                    .orElse(ChangeCategory.OTHER);
        }
        return ChangeCategory.OTHER;
    }

    private SemVerChangeLevel getLevel(String intention) {
        if (intention != null) {
            return configuration.getItems().stream()
                    .filter(v -> v.getType().equals(intention))
                    .findAny()
                    .map(AnalyzedCommit::getLevel)
                    .orElse(SemVerChangeLevel.NONE);
        }
        return SemVerChangeLevel.NONE;
    }
}
