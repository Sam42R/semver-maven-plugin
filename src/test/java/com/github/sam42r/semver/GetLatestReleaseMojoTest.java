package com.github.sam42r.semver;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.assertj.core.api.Assertions;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetLatestReleaseMojoTest {

    private AbstractSemverMojo uut;
    private MavenProject mavenProjectMock;

    @BeforeEach
    void setup(@TempDir Path tmp) {
        uut = new GetLastReleaseMojo();

        var logMock = mock(Log.class);
        uut.setLog(logMock);

        var pluginContext = new HashMap<>();
        uut.setPluginContext(pluginContext);

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
                    new AbstractMap.SimpleEntry<>(SemverContextVariable.LATEST_TAG, "none"),
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
            var commit = git.commit().setMessage("Initial commit").call();

            var tag = git.tag()
                    .setName("v1.0.0")
                    .setForceUpdate(true)
                    .call();

            uut.execute();

            assertThat((Map<SemverContextVariable, String>) uut.getPluginContext()).contains(
                    new AbstractMap.SimpleEntry<>(SemverContextVariable.LATEST_TAG, tag.getName()),
                    new AbstractMap.SimpleEntry<>(SemverContextVariable.LATEST_COMMIT, commit.getName())
            );
        }
    }

    @Test
    @Disabled("used for local testing")
    @SuppressWarnings("unchecked")
    void shouldFindLatestReleaseLocal() throws MojoExecutionException, MojoFailureException {
        when(mavenProjectMock.getFile()).thenReturn(new File("C:\\Users\\r_ric\\IdeaProjects\\SimpleTestProject\\pom.xml"));

        uut.execute();

        uut.getPluginContext().forEach((key, value) -> {
            System.out.printf("%s: %s%n", key, value);
        });
    }

    @Test
    void shouldThrowWithMissingGitRepository(@TempDir Path tmp) throws IOException {
        var pomXml = createFile(tmp, "pom.xml", "<project/>");
        when(mavenProjectMock.getFile()).thenReturn(pomXml.toFile());

        Assertions.assertThatThrownBy(() -> uut.execute())
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
