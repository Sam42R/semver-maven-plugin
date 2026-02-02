package io.github.sam42r.semver.model.release;

import java.time.LocalDateTime;

public record ReleaseInfo(String tagName, String name, String description, LocalDateTime time) {
}
