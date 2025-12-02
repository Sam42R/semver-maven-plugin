package io.github.sam42r.semver.changelog;

import lombok.NonNull;

import javax.inject.Named;

@Named("Markup")
public class MarkupRendererFactory implements ChangelogRendererFactory<MarkupRenderer> {

    @Override
    public @NonNull MarkupRenderer getInstance(@NonNull String template) {
        return new MarkupRenderer(template);
    }
}
