package io.github.sam42r.semver.changelog;

import lombok.NonNull;

import javax.inject.Named;

@Named("Markup")
public class MarkupRendererFactory implements ChangelogRendererFactory<MarkupRenderer> {

    @Override
    public @NonNull MarkupRenderer getInstance(
            @NonNull String template,
            boolean renderIssueLinks,
            boolean renderCommitLinks,
            boolean renderReleaseLinks,
            boolean renderBody
    ) {
        return new MarkupRenderer(template, renderIssueLinks, renderCommitLinks, renderReleaseLinks, renderBody);
    }
}
