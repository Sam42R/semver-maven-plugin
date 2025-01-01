package io.github.sam42r.semver.changelog;

import lombok.NonNull;

public interface ChangelogRendererFactory<R extends ChangelogRenderer> {

    @NonNull String getName();

    @NonNull R getInstance(@NonNull String template);
}
