package io.github.sam42r.semver.model.scm;

import java.time.Instant;

public record Commit(String id, Instant timestamp, String author, String message) {
}
