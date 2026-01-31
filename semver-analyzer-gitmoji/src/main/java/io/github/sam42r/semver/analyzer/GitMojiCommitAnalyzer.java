package io.github.sam42r.semver.analyzer;

import io.github.sam42r.semver.analyzer.model.Configuration;
import io.github.sam42r.semver.model.analyze.AnalyzedCommit;
import io.github.sam42r.semver.model.analyze.ChangeCategory;
import io.github.sam42r.semver.model.analyze.Issue;
import io.github.sam42r.semver.model.analyze.SemVerChangeLevel;
import io.github.sam42r.semver.model.release.ProviderSpec;
import io.github.sam42r.semver.model.scm.Commit;
import io.github.sam42r.semver.model.scm.Remote;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
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
    public @NonNull String generateReleaseCommitMessage(@NonNull String version) {
        return "%s: bump version %s".formatted(configuration.getRelease(), version);
    }

    @Override
    public @NonNull List<AnalyzedCommit> analyzeCommits(@NonNull List<Commit> commits, @NonNull Remote remote, ProviderSpec providerSpec) {
        return commits.stream().map(commit -> analyzeCommit(commit, remote, providerSpec)).toList();
    }

    private AnalyzedCommit analyzeCommit(@NonNull Commit commit, @NonNull Remote remote, ProviderSpec providerSpec) {
        var pattern = Pattern.compile(COMMIT_MESSAGE_PATTERN);
        var matcher = pattern.matcher(commit.message());

        if (!commit.message().startsWith(":") || !matcher.find()) {
            return new AnalyzedCommit(commit, null, null, null, null, null, null, null, null, null);
        }

        var intention = matcher.group("INTENTION");
        var scope = Optional.ofNullable(matcher.group("SCOPE"))
                .map(v -> v.replace("(", ""))
                .map(v -> v.replace(")", ""))
                .map(v -> v.replace(":", ""))
                .map(String::trim)
                .orElse(null);
        var message = matcher.group("MESSAGE").trim();
        var refs = Optional.ofNullable(matcher.group("REF"))
                .map(v -> v.replace("#", ""))
                .map(String::trim)
                .map(List::of)
                .orElse(Collections.emptyList());

        return new AnalyzedCommit(
                commit,
                commit.message(),
                null,
                null,
                intention,
                getCategory(intention),
                scope,
                message,
                getLevel(intention),
                refs.stream()
                        .map(ref -> new Issue(ref, providerSpec.issueUrl(remote, ref)))
                        .toList()
        );
    }

    private ChangeCategory getCategory(String intention) {
        if (intention != null) {
            return configuration.getItems().stream()
                    .filter(v -> v.type().equals(intention))
                    .findAny()
                    .map(AnalyzedCommit::category)
                    .orElse(ChangeCategory.OTHER);
        }
        return ChangeCategory.OTHER;
    }

    private SemVerChangeLevel getLevel(String intention) {
        if (intention != null) {
            return configuration.getItems().stream()
                    .filter(v -> v.type().equals(intention))
                    .findAny()
                    .map(AnalyzedCommit::level)
                    .orElse(SemVerChangeLevel.NONE);
        }
        return SemVerChangeLevel.NONE;
    }
}
