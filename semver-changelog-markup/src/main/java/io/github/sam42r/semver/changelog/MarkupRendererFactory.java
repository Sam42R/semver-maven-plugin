package io.github.sam42r.semver.changelog;

import lombok.NonNull;

public class MarkupRendererFactory implements ChangelogRendererFactory<MarkupRenderer> {

    private static final String RENDERER_NAME = "Markup";

    @Override
    public @NonNull String getName() {
        return RENDERER_NAME;
    }

    @Override
    public @NonNull MarkupRenderer getInstance(@NonNull String template) {
        return new MarkupRenderer(template);
    }
}
