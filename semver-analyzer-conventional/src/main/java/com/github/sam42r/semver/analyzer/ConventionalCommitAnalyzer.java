package com.github.sam42r.semver.analyzer;

import com.github.sam42r.semver.analyzer.model.AnalyzedCommit;
import com.github.sam42r.semver.scm.model.Commit;
import lombok.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.List;

/**
 * {@link CommitAnalyzer} for
 * <a href="https://www.conventionalcommits.org/en/v1.0.0/">Conventional Commits specification</a>.
 */
public class ConventionalCommitAnalyzer implements CommitAnalyzer {

    private static final String ANALYZER_NAME = "Conventional";

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
    private AnalyzedCommit analyzeCommit(Commit commit) {
        var builder = AnalyzedCommit.builder()
                .id(commit.getId())
                .timestamp(commit.getTimestamp())
                .author(commit.getAuthor())
                .message(commit.getMessage());

        if (commit.getMessage().startsWith("fix")) {
            builder.type(AnalyzedCommit.Type.FIX);
        } else if (commit.getMessage().startsWith("feat")) {
            builder.type(AnalyzedCommit.Type.FEAT);
        } else {
            builder.type(AnalyzedCommit.Type.CHORE);
        }

        builder.breaking(commit.getMessage().contains("BREAKING CHANGE"));

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

        // TODO split header to type, scope and subject
        return builder
                .header(headerBuilder.toString().trim())
                .body(bodyBuilder.toString().trim())
                .footer(footerBuilder.toString().trim())
                .build();
    }
}
