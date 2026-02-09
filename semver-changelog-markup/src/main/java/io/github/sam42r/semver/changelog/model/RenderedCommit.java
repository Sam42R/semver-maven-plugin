package io.github.sam42r.semver.changelog.model;

import java.util.List;

public record RenderedCommit(String message, Link reference, List<Link> issues) {
}
