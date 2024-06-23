package io.github.sam42r.semver.scm;

import io.github.sam42r.semver.scm.model.Commit;
import io.github.sam42r.semver.scm.model.Tag;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.UserConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class GitProviderTest {

    @TempDir
    private Path tempDirectory;
    private SCMProvider uut;

    @BeforeEach
    void setup() {
        uut = new GitProviderFactory().getInstance(tempDirectory);
    }

    @Test
    void shouldReadCommits() throws GitAPIException, IOException, SCMException {
        try (var git = Git.init().setDirectory(tempDirectory.toFile()).call()) {
            Files.writeString(tempDirectory.resolve("README.md"), "#JUnit");
            git.add().addFilepattern("README.md").call();
            var expected = git.commit().setMessage("Test commit").call();

            var actual = uut.readCommits(null);
            assertThat(actual).containsExactly(
                    Commit.builder()
                            .id(expected.getId().getName())
                            .author(git.getRepository().getConfig().get(UserConfig.KEY).getAuthorName())
                            .timestamp(Instant.ofEpochSecond(expected.getCommitTime()))
                            .message("Test commit")
                            .build()
            );
        }
    }

    @Test
    void shouldReadTags() throws GitAPIException, IOException, SCMException {
        try (var git = Git.init().setDirectory(tempDirectory.toFile()).call()) {
            Files.writeString(tempDirectory.resolve("README.md"), "#JUnit");
            git.add().addFilepattern("README.md").call();
            var expected = git.commit().setMessage("Test commit").call();
            git.tag().setName("v1.0.0").call();

            var actual = uut.readTags();
            assertThat(actual).containsExactly(
                    Tag.builder()
                            .name("v1.0.0")
                            .commitId(expected.getId().getName())
                            .build()
            );
        }
    }

    @Test
    void shouldAddFiles() throws GitAPIException, IOException, SCMException {
        try (var git = Git.init().setDirectory(tempDirectory.toFile()).call()) {
            var file = Files.writeString(tempDirectory.resolve("README.md"), "#JUnit");
            var directory = Files.createDirectory(tempDirectory.resolve("directory"));
            var anotherFile = Files.writeString(directory.resolve("file.dat"), "JUnit");

            uut.addFile(file);
            uut.addFile(anotherFile);

            var actual = git.status().call();
            assertThat(actual.getAdded()).hasSize(2);
        }
    }
}
