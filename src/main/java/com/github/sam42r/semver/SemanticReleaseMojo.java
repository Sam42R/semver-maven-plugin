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
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

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

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        var projectBaseDirectory = project.getFile().getParentFile().toPath();

        var verifiedConditions = verifyConditions();
        var scmProvider = verifiedConditions.scmProvider()
                .orElseThrow(() -> new IllegalArgumentException("Could not find SCM provider with name '%s'".formatted(scmProviderName)));
        var commitAnalyzer = verifiedConditions.commitAnalyzer()
                .orElseThrow(() -> new IllegalArgumentException("Could not find commit analyzer with name '%s'".formatted(commitAnalyzerName)));
        getLog().info("Running semantic-release with SCM provider '%s'".formatted(scmProvider.getClass().getSimpleName()));
        getLog().info("Running semantic-release with commit analyzer '%s'".formatted(commitAnalyzer.getClass().getSimpleName()));

        var latestRelease = getLatestRelease(projectBaseDirectory, scmProvider);
        var latestTag = latestRelease.latestTag().orElse("None");
        var latestCommit = latestRelease.latestCommit().orElse("None");
        getLog().debug("Latest tag: '%s'".formatted(latestTag));
        getLog().debug("Latest commit: '%s'".formatted(latestCommit));

        var analyzedCommits = analyzeCommits(projectBaseDirectory, scmProvider, commitAnalyzer, latestCommit);
        getLog().debug("Found %d major, %d minor and %d patch commits".formatted(
                analyzedCommits.major().size(), analyzedCommits.minor().size(), analyzedCommits.patch().size()));
    }

    private VerifiedConditions verifyConditions() {
        return new VerifiedConditions(
                scmProviders.stream()
                        .map(ServiceLoader.Provider::get)
                        .filter(v -> scmProviderName.equalsIgnoreCase(v.getName()))
                        .findAny(),
                commitAnalyzers.stream()
                        .map(ServiceLoader.Provider::get)
                        .filter(v -> commitAnalyzerName.equalsIgnoreCase(v.getName()))
                        .findAny()
        );
    }

    private LatestRelease getLatestRelease(Path projectBaseDirectory, SCMProvider scmProvider) throws MojoExecutionException {
        try {
            var tags = scmProvider.readTags(projectBaseDirectory);
            var commits = scmProvider.readCommits(projectBaseDirectory);

            var latestTagOpt = tags.max(Comparator.comparing(Tag::getName));
            var latestCommitOpt = latestTagOpt.map(Tag::getCommitId)
                    .or(() -> commits.min(Comparator.comparing(Commit::getTimestamp)).map(Commit::getId));

            return new LatestRelease(
                    latestTagOpt.map(Tag::getName),
                    latestCommitOpt
            );
        } catch (SCMException e) {
            throw new MojoExecutionException(e.getMessage(), e.getCause());
        }
    }

    private AnalyzedCommits analyzeCommits(
            Path projectBaseDirectory,
            SCMProvider scmProvider,
            CommitAnalyzer commitAnalyzer,
            String latestCommit
    ) throws MojoExecutionException {
        try {
            // TODO always use 2 params method and add check in method!?
            var commits = latestCommit != null ?
                    scmProvider.readCommits(projectBaseDirectory, latestCommit) :
                    scmProvider.readCommits(projectBaseDirectory);

            var response = commitAnalyzer.analyzeCommits(commits.toList());
            return new AnalyzedCommits(
                    response.getBreaking(),
                    response.getFeatures(),
                    response.getFixes()
            );
        } catch (SCMException e) {
            throw new MojoExecutionException(e.getMessage(), e.getCause());
        }
    }

    private void verifyRelease() {

    }

    private void generateNotes() {

    }

    private void createTag() {

    }

    private record VerifiedConditions(Optional<SCMProvider> scmProvider, Optional<CommitAnalyzer> commitAnalyzer) {
    }

    private record LatestRelease(Optional<String> latestTag, Optional<String> latestCommit) {
    }

    private record AnalyzedCommits(List<Commit> major, List<Commit> minor, List<Commit> patch) {
    }
}
