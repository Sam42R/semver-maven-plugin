package com.github.sam42r.semver.scm;

import com.github.sam42r.semver.scm.SCMProvider;
import com.github.sam42r.semver.scm.impl.GitProvider;
import com.github.sam42r.semver.scm.model.Commit;
import com.github.sam42r.semver.scm.model.Tag;
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

    private SCMProvider uut;

    @BeforeEach
    void setup() {
        uut = new GitProvider();
    }

    @Test
    void shouldReadCommits(@TempDir Path tempDirectory) throws GitAPIException, IOException, SCMException {
        try (var git = Git.init().setDirectory(tempDirectory.toFile()).call()) {
            Files.writeString(tempDirectory.resolve("README.md"), "#JUnit");
            git.add().addFilepattern("README.md").call();
            var expected = git.commit().setMessage("Test commit").call();

            var actual = uut.readCommits(tempDirectory, null);
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
    void shouldReadTags(@TempDir Path tempDirectory) throws GitAPIException, IOException, SCMException {
        try (var git = Git.init().setDirectory(tempDirectory.toFile()).call()) {
            Files.writeString(tempDirectory.resolve("README.md"), "#JUnit");
            git.add().addFilepattern("README.md").call();
            var expected = git.commit().setMessage("Test commit").call();
            git.tag().setName("v1.0.0").call();

            var actual = uut.readTags(tempDirectory);
            assertThat(actual).containsExactly(
                    Tag.builder()
                            .name("v1.0.0")
                            .commitId(expected.getId().getName())
                            .build()
            );
        }
    }
}
