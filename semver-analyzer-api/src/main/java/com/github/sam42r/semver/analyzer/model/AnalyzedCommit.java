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

    public enum Category {
        ADDED, CHANGED, DEPRECATED, REMOVED, FIXED, SECURITY, OTHER;
    }

    public static Predicate<AnalyzedCommit> isBugfix = analyzedCommit ->
            Category.FIXED.equals(analyzedCommit.getCategory()) || Category.SECURITY.equals(analyzedCommit.getCategory());
    public static Predicate<AnalyzedCommit> isFeature = analyzedCommit -> Category.ADDED.equals(analyzedCommit.getCategory());
    public static Predicate<AnalyzedCommit> isBreaking = AnalyzedCommit::isBreaking;
}
