package com.github.sam42r.semver;

import com.github.sam42r.semver.analyzer.CommitAnalyzer;
import com.github.sam42r.semver.scm.SCMException;
import com.github.sam42r.semver.scm.SCMProvider;
import com.github.sam42r.semver.scm.model.Commit;
import com.github.sam42r.semver.scm.model.Tag;
import lombok.Setter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.ServiceLoader;

import static com.github.sam42r.semver.SemverContextVariable.LATEST_COMMIT;
import static com.github.sam42r.semver.SemverContextVariable.LATEST_TAG;

/**
 * @author Sam42R
 */
@Mojo(name = "semantic-release")
public class SemanticReleaseMojo extends AbstractMojo {

    private final ServiceLoader<SCMProvider> scmProviders = ServiceLoader.load(SCMProvider.class);
    private final ServiceLoader<CommitAnalyzer> commitAnalyzers = ServiceLoader.load(CommitAnalyzer.class);

    @Setter
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Setter
    @Parameter(name = "scm-provider-name", defaultValue = "Git")
    private String scmProviderName;

    @Setter
    @Parameter(name = "commit-analyzer-name", defaultValue = "Angular")
    private String commitAnalyzerName;

    private SCMProvider scmProvider;
    private CommitAnalyzer commitAnalyzer;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        var projectBaseDirectory = project.getFile().getParentFile().toPath();
        verifyConditions();
        getLatestRelease(projectBaseDirectory);
        analyzeCommits(projectBaseDirectory, null);
    }

    private void verifyConditions() {
        scmProvider = scmProviders.stream()
                .map(ServiceLoader.Provider::get)
                .filter(v -> scmProviderName.equalsIgnoreCase(v.getName()))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Could not find SCM provider with name '%s'".formatted(scmProviderName)));
        getLog().info("Running semantic-release with SCM provider '%s'".formatted(scmProvider.getClass().getSimpleName()));

        commitAnalyzer = commitAnalyzers.stream()
                .map(ServiceLoader.Provider::get)
                .filter(v -> commitAnalyzerName.equalsIgnoreCase(v.getName()))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Could not find commit analyzer with name '%s'".formatted(commitAnalyzerName)));
        getLog().info("Running semantic-release with commit analyzer '%s'".formatted(commitAnalyzer.getClass().getSimpleName()));
    }

    @SuppressWarnings("unchecked")
    private void getLatestRelease(Path projectBaseDirectory) throws MojoExecutionException {
        try {
            var commits = scmProvider.readCommits(projectBaseDirectory);
            var tags = scmProvider.readTags(projectBaseDirectory);

            var latestTagOpt = tags.max(Comparator.comparing(Tag::getName));
            var latestCommitOpt = latestTagOpt.map(Tag::getCommitId)
                    .or(() -> commits.min(Comparator.comparing(Commit::getTimestamp)).map(Commit::getId));

            // TODO refactor to method response
            getPluginContext().put(LATEST_TAG, latestTagOpt.map(Tag::getName).orElse("None"));
            getPluginContext().put(LATEST_COMMIT, latestCommitOpt.orElse("None"));

            getLog().debug("Latest tag: '%s'".formatted(getPluginContext().get(LATEST_TAG)));
            getLog().debug("Latest commit: '%s'".formatted(getPluginContext().get(LATEST_COMMIT)));
        } catch (SCMException e) {
            throw new MojoExecutionException(e.getMessage(), e.getCause());
        }
    }

    private void analyzeCommits(Path projectBaseDirectory, String latestCommit) throws MojoExecutionException {
        try {
            // TODO always use 2 params method and add check in method
            var commits = latestCommit != null ?
                    scmProvider.readCommits(projectBaseDirectory, latestCommit) :
                    scmProvider.readCommits(projectBaseDirectory);

            var response = commitAnalyzer.analyzeCommits(commits.toList());
        } catch (SCMException e) {
            throw new MojoExecutionException(e.getMessage(), e.getCause());
        }
    }

}
