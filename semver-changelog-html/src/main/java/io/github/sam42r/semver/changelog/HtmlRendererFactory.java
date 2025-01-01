package io.github.sam42r.semver.changelog;

import lombok.NonNull;

public class HtmlRendererFactory implements ChangelogRendererFactory<HtmlRenderer> {

    private static final String RENDERER_NAME = "Html";

    @Override
    public @NonNull String getName() {
        return RENDERER_NAME;
    }

    @Override
    public @NonNull HtmlRenderer getInstance(@NonNull String template) {
        return new HtmlRenderer(template);
    }
}
