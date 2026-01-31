package io.github.sam42r.semver.changelog;

import io.github.sam42r.semver.analyzer.model.AnalyzedCommit;
import lombok.NonNull;

import java.util.function.Function;

public abstract class AbstractChangelogRenderer implements ChangelogRenderer {

    protected AnalyzedCommit generateIssueLinks(
            @NonNull AnalyzedCommit analyzedCommit, @NonNull Function<String, String> generateIssueLink) {
        if (analyzedCommit.getIssues() != null && !analyzedCommit.getIssues().isEmpty()) {
            analyzedCommit.setIssues(
                    analyzedCommit.getIssues().stream()
                            .map(generateIssueLink)
                            .toList()
            );
        }
        return analyzedCommit;
    }
}
