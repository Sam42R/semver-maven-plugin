package io.github.sam42r.semver.model.scm;

import java.net.URI;

public record Remote(String url, String scheme, String host, String group, String project) {

    public static Remote of(String url) {
        return url.startsWith("http") ? parseHttpUrl(url) : parseSshUrl(url);
    }

    private static Remote parseHttpUrl(String url) {
        var uri = URI.create(url);

        var lastSlashIndex = uri.getPath().lastIndexOf("/");

        var group = uri.getPath().substring(1, lastSlashIndex);
        var project = uri.getPath().substring(lastSlashIndex + 1);

        if (project.contains(".")) {
            project = project.substring(0, project.lastIndexOf("."));
        }

        var hostAndPort = uri.getHost().concat(uri.getPort() > 0 ? ":%d".formatted(uri.getPort()) : "");

        return new Remote(url, uri.getScheme(), hostAndPort, group, project);
    }

    private static Remote parseSshUrl(String url) {
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

        return new Remote(url, "https", hostAndPort, group, project);
    }
}
