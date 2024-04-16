package com.github.sam42r.semver;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AbstractSemverMojoTest {

    protected Git initializeGitRepository(Path path) throws GitAPIException {
        return Git.init().setDirectory(path.toFile()).call();
    }

    protected Path createFile(Path tmp, String name, String content) throws IOException {
        var file = Files.createFile(tmp.resolve(name));
        if (content != null) {
            Files.writeString(file, content);
        }
        return file;
    }
}
