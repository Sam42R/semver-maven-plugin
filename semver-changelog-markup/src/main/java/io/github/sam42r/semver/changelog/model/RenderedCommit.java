package io.github.sam42r.semver.changelog.model;

import java.util.List;

public record RenderedCommit(String message, List<String> body, Link reference, List<Link> issues) {
}
