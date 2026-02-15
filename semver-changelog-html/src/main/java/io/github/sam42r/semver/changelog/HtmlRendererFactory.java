package io.github.sam42r.semver.changelog;

import lombok.NonNull;

import javax.inject.Named;

@Named("Html")
public class HtmlRendererFactory implements ChangelogRendererFactory<HtmlRenderer> {

    @Override
    public @NonNull HtmlRenderer getInstance(
            @NonNull String template,
            boolean renderIssueLinks,
            boolean renderCommitLinks,
            boolean renderReleaseLinks,
            boolean renderBody
    ) {
        return new HtmlRenderer(template, renderIssueLinks, renderCommitLinks, renderReleaseLinks, renderBody);
    }
}
