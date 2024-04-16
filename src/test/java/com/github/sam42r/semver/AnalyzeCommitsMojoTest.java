package com.github.sam42r.semver;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AnalyzeCommitsMojoTest extends AbstractSemverMojoTest {

    private AbstractSemverMojo uut;
    private MavenProject mavenProjectMock;

    @BeforeEach
    void setup(@TempDir Path tmp) {
        uut = new AnalyzeCommitsMojo();

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
            uut.getPluginContext().put(SemverContextVariable.LATEST_TAG, "none");
            uut.getPluginContext().put(SemverContextVariable.LATEST_COMMIT, commit.getName());

            uut.execute();

            assertThat((Map<SemverContextVariable, String>) uut.getPluginContext()).contains(
                    new AbstractMap.SimpleEntry<>(SemverContextVariable.LATEST_TAG, "none"),
                    new AbstractMap.SimpleEntry<>(SemverContextVariable.LATEST_COMMIT, commit.getName())
            );
        }
    }
}
