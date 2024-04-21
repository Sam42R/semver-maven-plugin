package com.github.sam42r.semver;

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
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SemanticReleaseMojoTest {

    private SemanticReleaseMojo uut;
    private MavenProject mavenProjectMock;

    @BeforeEach
    void setup(@TempDir Path tmp) {
        uut = new SemanticReleaseMojo();

        var logMock = mock(Log.class);
        uut.setLog(logMock);

        var pluginContext = new HashMap<>();
        uut.setPluginContext(pluginContext);
        uut.setScmProviderName("Git");

        mavenProjectMock = mock(MavenProject.class);
        uut.setProject(mavenProjectMock);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldFindNoLatestRelease(@TempDir Path tmp) throws IOException, GitAPIException, MojoExecutionException, MojoFailureException {
        var pomXml = createFile(tmp, "pom.xml", "<project/>");
        when(mavenProjectMock.getFile()).thenReturn(pomXml.toFile());

        try (var git = initializeGitRepository(tmp)) {
            git.add().addFilepattern("pom.xml").call();
            var commit = git.commit().setMessage("Initial commit").call();

            uut.execute();

            assertThat((Map<SemverContextVariable, String>) uut.getPluginContext()).contains(
                    new AbstractMap.SimpleEntry<>(SemverContextVariable.LATEST_TAG, "None"),
                    new AbstractMap.SimpleEntry<>(SemverContextVariable.LATEST_COMMIT, commit.getName())
            );
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldFindLatestRelease(@TempDir Path tmp) throws IOException, GitAPIException, MojoExecutionException, MojoFailureException {
        var pomXml = createFile(tmp, "pom.xml", "<project/>");
        when(mavenProjectMock.getFile()).thenReturn(pomXml.toFile());

        try (var git = initializeGitRepository(tmp)) {
            git.add().addFilepattern("pom.xml").call();
            var expected = git.commit().setMessage("Initial commit").call();
            git.tag().setName("v1.0.0").call();

            uut.execute();

            assertThat((Map<SemverContextVariable, String>) uut.getPluginContext()).contains(
                    new AbstractMap.SimpleEntry<>(SemverContextVariable.LATEST_TAG, "v1.0.0"),
                    new AbstractMap.SimpleEntry<>(SemverContextVariable.LATEST_COMMIT, expected.getName())
            );
        }
    }

    @Test
    void shouldThrowWithEmptyGitRepository(@TempDir Path tmp) throws IOException, GitAPIException, MojoExecutionException, MojoFailureException {
        var pomXml = createFile(tmp, "pom.xml", "<project/>");
        when(mavenProjectMock.getFile()).thenReturn(pomXml.toFile());

        try (var ignored = initializeGitRepository(tmp)) {
            assertThatThrownBy(() -> uut.execute())
                    .isInstanceOf(MojoExecutionException.class)
                    .hasMessageStartingWith("No HEAD exists");
        }
    }

    @Test
    void shouldThrowWithMissingGitRepository(@TempDir Path tmp) throws IOException {
        var pomXml = createFile(tmp, "pom.xml", "<project/>");
        when(mavenProjectMock.getFile()).thenReturn(pomXml.toFile());

        assertThatThrownBy(() -> uut.execute())
                .isInstanceOf(MojoExecutionException.class)
                .hasMessageStartingWith("Could not find git repository");
    }

    private Git initializeGitRepository(Path path) throws GitAPIException {
        return Git.init().setDirectory(path.toFile()).call();
    }

    private Path createFile(Path tmp, String name, String content) throws IOException {
        var file = Files.createFile(tmp.resolve(name));
        if (content != null) {
            Files.writeString(file, content);
        }
        return file;
    }
}
