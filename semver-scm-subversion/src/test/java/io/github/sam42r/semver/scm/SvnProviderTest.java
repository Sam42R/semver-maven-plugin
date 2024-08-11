package io.github.sam42r.semver.scm;

import io.github.sam42r.semver.scm.model.Commit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class SvnProviderTest {

    @TempDir
    private Path tempDirectory;
    private SCMProvider uut;

    @BeforeEach
    void setup() {
        uut = new SvnProviderFactory().getInstance(tempDirectory, null, null);
    }

    @Test
    void shouldReadCommits() throws SVNException, IOException, SCMException {
        var repository = SVNRepositoryFactory.create(
                SVNRepositoryFactory.createLocalRepository(
                        tempDirectory.toFile(), true, false));

        var editor = repository.getCommitEditor("Test commit", null, true, null);
        editor.openRoot(-1);
        Files.writeString(tempDirectory.resolve("README.md"), "#JUnit");
        editor.addFile("README.md", null, -1);
        editor.closeDir();

        var expected = editor.closeEdit();

        var actual = uut.readCommits(null);

        assertThat(actual).containsExactly(
                Commit.builder()
                        .id(Long.toString(expected.getNewRevision()))
                        .author(expected.getAuthor())
                        .timestamp(expected.getDate().toInstant())
                        .message("Test commit")
                        .build()
        );
    }
}
