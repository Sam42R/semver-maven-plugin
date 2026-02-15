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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.Collections;
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
    private static final String COMMIT_FOOTER_PATTERN = "(?<REF>(#\\d*))";

    private final Configuration configuration;

    @Override
    public @NonNull String generateReleaseCommitMessage(@NonNull String version) {
        return "%s: release version %s".formatted(configuration.getRelease(), version);
    }

    @Override
    public @NonNull List<AnalyzedCommit> analyzeCommits(@NonNull List<Commit> commits, @NonNull Remote remote, ProviderSpec providerSpec) {
        return commits.stream().map(commit -> analyzeCommit(commit, remote, providerSpec)).toList();
    }

    @SuppressWarnings("MismatchedQueryAndUpdateOfStringBuilder")
    private AnalyzedCommit analyzeCommit(@NonNull Commit commit, @NonNull Remote remote, ProviderSpec providerSpec) {
        var headerBuilder = new StringBuilder();
        var bodyBuilder = new StringBuilder();
        var footerBuilder = new StringBuilder();
        try (var reader = new BufferedReader(new StringReader(commit.message()))) {
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

        var headerMatcher = Pattern.compile(COMMIT_HEADER_PATTERN).matcher(header);

        if (header.isEmpty() || !headerMatcher.find()) {
            return new AnalyzedCommit(commit, null, null, null, null, null, null, null, null, null, null);
        }

        var type = headerMatcher.group("TYPE");
        var scope = Optional.ofNullable(headerMatcher.group("SCOPE"))
                .map(v -> v.replace("(", ""))
                .map(v -> v.replace(")", ""))
                .orElse(null);
        var breaking = Optional.ofNullable(headerMatcher.group("BREAKING"));
        var description = headerMatcher.group("DESCRIPTION").replaceFirst(":", "").trim();

        var footerMatcher = Pattern.compile(COMMIT_FOOTER_PATTERN).matcher(footer);

        var refs = footerMatcher.find() ?
                Optional.ofNullable(footerMatcher.group("REF"))
                        .map(v -> v.replace("#", ""))
                        .map(String::trim)
                        .map(List::of)
                        .orElse(Collections.emptyList()) :
                Collections.<String>emptyList();

        return new AnalyzedCommit(
                commit,
                providerSpec.issueUrl(remote, commit.id()),
                header,
                body,
                footer,
                type,
                getCategory(type, scope),
                scope,
                description,
                breaking.isPresent() || footer.contains("BREAKING CHANGE") ? SemVerChangeLevel.MAJOR : getLevel(type),
                refs.stream()
                        .map(ref -> new Issue(ref, providerSpec.issueUrl(remote, ref)))
                        .toList()
        );
    }

    private ChangeCategory getCategory(String type, String scope) {
        if ("security".equalsIgnoreCase(scope)) {
            return ChangeCategory.SECURITY;
        }

        if (type != null) {
            return configuration.getItems().stream()
                    .filter(v -> v.type().equals(type))
                    .findAny()
                    .map(AnalyzedCommit::category)
                    .orElse(ChangeCategory.OTHER);
        }
        return ChangeCategory.OTHER;
    }

    private SemVerChangeLevel getLevel(String type) {
        if (type != null) {
            return configuration.getItems().stream()
                    .filter(v -> v.type().equals(type))
                    .findAny()
                    .map(AnalyzedCommit::level)
                    .orElse(SemVerChangeLevel.NONE);
        }
        return SemVerChangeLevel.NONE;
    }
}
