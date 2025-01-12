package io.github.sam42r.semver.scm.util;

import io.github.sam42r.semver.scm.model.Remote;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.net.URI;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RemoteUtil {

    public static @NonNull Remote parseUrl(@NonNull String url) {
        return url.startsWith("http") ? parseHttpRemoteUrl(url) : parseSshRemoteUrl(url);
    }

    private static @NonNull Remote parseHttpRemoteUrl(@NonNull String url) {
        var uri = URI.create(url);

        var lastSlashIndex = uri.getPath().lastIndexOf("/");

        var group = uri.getPath().substring(1, lastSlashIndex);
        var project = uri.getPath().substring(lastSlashIndex + 1);

        if (project.contains(".")) {
            project = project.substring(0, project.lastIndexOf("."));
        }

        var hostAndPort = uri.getHost().concat(uri.getPort() > 0 ? ":%d".formatted(uri.getPort()) : "");

        return Remote.builder()
                .url(url)
                .scheme(uri.getScheme())
                .host(hostAndPort)
                .group(group)
                .project(project)
                .build();
    }

    private static @NonNull Remote parseSshRemoteUrl(@NonNull String url) {
        var startIndex = url.contains("@") ? url.indexOf("@") + 1 : 0;
        var lastColonIndex = url.lastIndexOf(":");

        var hostAndPort = url.substring(startIndex, lastColonIndex);

        var path = url.substring(lastColonIndex + 1);
        var lastSlashIndex = path.lastIndexOf("/");

        var group = path.substring(0, lastSlashIndex);
        var project = path.substring(lastSlashIndex + 1);

        if (project.contains(".")) {
            project = project.substring(0, project.lastIndexOf("."));
        }

        return Remote.builder()
                .url(url)
                .scheme("https")
                .host(hostAndPort)
                .group(group)
                .project(project)
                .build();
    }
}
