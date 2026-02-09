package io.github.sam42r.semver.model.analyze;

import io.github.sam42r.semver.model.scm.Commit;

import java.util.List;
import java.util.function.Predicate;

/**
 * Analyzed {@link Commit} based on commit-message analysis.
 *
 * @param commit   scm {@link Commit}
 * @param url      provider specific commit URL
 * @param header   commit header
 * @param body     commit body
 * @param footer   commit footer
 * @param type     commit type
 * @param category commit {@link ChangeCategory}
 * @param scope    commit scope
 * @param subject  commit subject
 * @param level    commit {@link SemVerChangeLevel}
 * @param issues   list of referenced {@link Issue}'s
 */
public record AnalyzedCommit(
        Commit commit,
        String url,
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
