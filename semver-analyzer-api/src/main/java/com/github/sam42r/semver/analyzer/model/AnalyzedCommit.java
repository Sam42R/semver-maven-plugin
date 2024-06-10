package com.github.sam42r.semver.analyzer.model;

import com.github.sam42r.semver.scm.model.Commit;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.function.Predicate;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
public class AnalyzedCommit extends Commit {

    private String header;
    private String body;
    private String footer;

    private String type;
    private Category category;
    private String scope;
    private String subject;
    private boolean breaking;

    private List<String> issues;

    // TODO reduce enum to categories rendered in changelog
    public enum Category {
        FIX, FEAT, TEST, BUILD, CI, REFACTOR, DOCS, STYLE, PERF, CHORE;
    }

    public static Predicate<AnalyzedCommit> isBugfix = analyzedCommit -> Category.FIX.equals(analyzedCommit.getCategory());
    public static Predicate<AnalyzedCommit> isFeature = analyzedCommit -> Category.FEAT.equals(analyzedCommit.getCategory());
    public static Predicate<AnalyzedCommit> isBreaking = AnalyzedCommit::isBreaking;
}
