package io.github.sam42r.semver;

import io.github.sam42r.semver.model.Version;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SemanticReleaseMojoTest {

    private static final String POM = """
            <project>
                <groupId>org.junit</groupId>
                <artifactId>test</artifactId>
                <version>0.0.1-SNAPSHOT</version>
            </project>
            """;

    @TempDir
    private Path tmp;

    private SemanticReleaseMojo uut;

    @BeforeEach
    void setup() throws IOException {
        uut = new SemanticReleaseMojo();

        var logMock = mock(Log.class);
        uut.setLog(logMock);

        var pluginContext = new HashMap<>();
        uut.setPluginContext(pluginContext);
        uut.setScm(Scm.builder().push(false).build());
        uut.setAnalyzer(Analyzer.builder().build());
        uut.setChangelog(Changelog.builder().build());
        uut.setRelease(Release.builder().build());
        uut.setTagFormat(Version.TAG_FORMAT_DEFAULT);

        var mavenProjectMock = mock(MavenProject.class);
        uut.setProject(mavenProjectMock);

        var pomXml = createFile(tmp, "pom.xml", POM);
        when(mavenProjectMock.getFile()).thenReturn(pomXml.toFile());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldFindNoLatestRelease() throws GitAPIException, MojoExecutionException, MojoFailureException {
        try (var git = initializeGitRepository(tmp)) {
            git.add().addFilepattern("pom.xml").call();
            var commit = git.commit().setMessage("Initial commit").call();

            uut.execute();

            // TODO assert something
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldFindLatestRelease() throws IOException, GitAPIException, MojoExecutionException, MojoFailureException {
        try (var git = initializeGitRepository(tmp)) {
            git.add().addFilepattern("pom.xml").call();
            git.commit().setMessage("Initial commit").call();
            git.tag().setName("v1.0.0").call();

            var file = createFile(tmp, "README.md", "# Project");
            git.add().addFilepattern(file.getFileName().toString()).call();
            git.commit().setMessage("feat: add some feature").call();

            uut.execute();

            // TODO assert something
        }
    }

    @Test
    void shouldThrowWithEmptyGitRepository() throws GitAPIException, MojoExecutionException, MojoFailureException {
        try (var ignored = initializeGitRepository(tmp)) {
            assertThatThrownBy(() -> uut.execute())
                    .isInstanceOf(MojoExecutionException.class)
                    .hasRootCauseMessage("Cannot resolve HEAD");
        }
    }

    @Test
    void shouldThrowWithMissingGitRepository() {
        assertThatThrownBy(() -> uut.execute())
                .isInstanceOf(MojoExecutionException.class)
                .hasMessageStartingWith("Could not find git configuration");
    }

    private Git initializeGitRepository(Path path) throws GitAPIException {
        return Git.init().setDirectory(path.toFile()).call();
    }

    private Path createFile(Path path, String filename, String content) throws IOException {
        var file = Files.createFile(path.resolve(filename));
        if (content != null) {
            Files.writeString(file, content);
        }
        return file;
    }
}
