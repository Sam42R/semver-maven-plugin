package io.github.sam42r.semver.analyzer.model;

import io.github.sam42r.semver.scm.model.Commit;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.function.Predicate;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class AnalyzedCommit extends Commit {

    private String header;
    private String body;
    private String footer;

    private String type;
    private ChangeCategory category;
    private String scope;
    private String subject;
    private SemVerChangeLevel level;

    private List<String> issues;

    public static final Predicate<AnalyzedCommit> isBugfix = analyzedCommit ->
            SemVerChangeLevel.PATCH.equals(analyzedCommit.getLevel());
    public static final Predicate<AnalyzedCommit> isFeature = analyzedCommit ->
            SemVerChangeLevel.MINOR.equals(analyzedCommit.getLevel());
    public static final Predicate<AnalyzedCommit> isBreaking = analyzedCommit ->
            SemVerChangeLevel.MAJOR.equals(analyzedCommit.getLevel());
}
