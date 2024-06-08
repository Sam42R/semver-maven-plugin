package com.github.sam42r.semver.analyzer.model;

import com.github.sam42r.semver.scm.model.Commit;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.function.Predicate;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class AnalyzedCommit extends Commit {

    private String header;
    private String body;
    private String footer;

    private Type type;
    private String scope;
    private String subject;

    private boolean breaking;

    private List<String> issues;

    public enum Type {
        FIX, FEAT, TEST, BUILD, CI, REFACTOR, DOCS, STYLE, PERF, CHORE;
    }

    public static Predicate<AnalyzedCommit> isBugfix = analyzedCommit -> Type.FIX.equals(analyzedCommit.getType());
    public static Predicate<AnalyzedCommit> isFeature = analyzedCommit -> Type.FEAT.equals(analyzedCommit.getType());
    public static Predicate<AnalyzedCommit> isBreaking = AnalyzedCommit::isBreaking;
}
