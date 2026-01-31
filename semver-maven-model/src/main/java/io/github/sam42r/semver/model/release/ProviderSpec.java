package io.github.sam42r.semver.model.release;

import io.github.sam42r.semver.model.scm.Remote;

public record ProviderSpec(String issuePattern) {

    public String issueUrl(Remote remote, String id) {
        return issuePattern().formatted(remote.scheme(), remote.host(), remote.group(), remote.project(), id);
    }
}
