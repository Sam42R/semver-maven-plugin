package io.github.sam42r.semver.model.analyze;

import io.github.sam42r.semver.model.scm.Commit;

import java.util.List;
import java.util.function.Predicate;

public record AnalyzedCommit(
        Commit commit,
        String header,
        String body,
        String footer,
        String type,
        ChangeCategory category,
        String scope,
        String subject,
        SemVerChangeLevel level,
        List<Issue> issues
) {
    public static final Predicate<AnalyzedCommit> isBugfix = analyzedCommit ->
            SemVerChangeLevel.PATCH.equals(analyzedCommit.level());
    public static final Predicate<AnalyzedCommit> isFeature = analyzedCommit ->
            SemVerChangeLevel.MINOR.equals(analyzedCommit.level());
    public static final Predicate<AnalyzedCommit> isBreaking = analyzedCommit ->
            SemVerChangeLevel.MAJOR.equals(analyzedCommit.level());
}
