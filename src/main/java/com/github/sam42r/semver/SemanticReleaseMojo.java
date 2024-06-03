package com.github.sam42r.semver;

import com.github.sam42r.semver.analyzer.CommitAnalyzer;
import com.github.sam42r.semver.changelog.ChangelogRenderer;
import com.github.sam42r.semver.changelog.impl.MustacheRenderer;
import com.github.sam42r.semver.model.Version;
import com.github.sam42r.semver.scm.SCMException;
import com.github.sam42r.semver.scm.SCMProvider;
import com.github.sam42r.semver.scm.SCMProviderFactory;
import com.github.sam42r.semver.scm.model.Commit;
import com.github.sam42r.semver.scm.model.Tag;
import com.github.sam42r.semver.util.PomHelper;
import lombok.Setter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.IOException;
import java.nio.file.Files;
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

    // TODO we can not use html markup in pom.xml configuration (e.g. named groups)
    protected static final String VERSION_NUMBER_PATTERN_DEFAULT = "v(?<MAJOR>[0-9]*).(?<MINOR>[0-9]*).(?<PATCH>[0-9]*)";

    @SuppressWarnings("rawtypes")
    private final ServiceLoader<SCMProviderFactory> scmProviderFactories = ServiceLoader.load(SCMProviderFactory.class);
    private final ServiceLoader<CommitAnalyzer> commitAnalyzers = ServiceLoader.load(CommitAnalyzer.class);

    @Setter
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Setter
    @Parameter(name = "version-number-pattern", defaultValue = VERSION_NUMBER_PATTERN_DEFAULT)
    private String versionNumberPattern;

    @Setter
    @Parameter(name = "scm-provider-name", defaultValue = "Git")
    private String scmProviderName;

    @Setter
    @Parameter(name = "commit-analyzer-name", defaultValue = "Angular")
    private String commitAnalyzerName;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        var projectBaseDirectory = project.getFile().getParentFile().toPath();

        var verifiedConditions = verifyConditions(projectBaseDirectory);
        var scmProvider = verifiedConditions.scmProvider()
                .orElseThrow(() -> new IllegalArgumentException("Could not find SCM provider with name '%s'".formatted(scmProviderName)));
        var commitAnalyzer = verifiedConditions.commitAnalyzer()
                .orElseThrow(() -> new IllegalArgumentException("Could not find commit analyzer with name '%s'".formatted(commitAnalyzerName)));
        getLog().info("Running semantic-release with SCM provider '%s'".formatted(scmProvider.getClass().getSimpleName()));
        getLog().info("Running semantic-release with commit analyzer '%s'".formatted(commitAnalyzer.getClass().getSimpleName()));

        var latestRelease = getLatestRelease(scmProvider);
        var latestTag = latestRelease.latestTag().orElse("None");
        var latestCommit = latestRelease.latestCommit().orElse("None");
        getLog().debug("Latest tag: '%s'".formatted(latestTag));
        getLog().debug("Latest commit: '%s'".formatted(latestCommit));

        var latestVersion = latestRelease.latestTag()
                .map(t -> Version.of(t, versionNumberPattern))
                .orElse(Version.of(0, 0, 0, versionNumberPattern));
        getLog().debug("Actual version: '%s'".formatted(latestVersion.toString()));

        var analyzedCommits = analyzeCommits(scmProvider, commitAnalyzer, latestCommit);
        getLog().debug("Found %d major, %d minor and %d patch commits".formatted(
                analyzedCommits.major().size(), analyzedCommits.minor().size(), analyzedCommits.patch().size()));

        var nextVersionType = verifyRelease(analyzedCommits);

        if (nextVersionType == null) {
            getLog().info("No commits found to release");
        } else {
            getLog().info("Continue with '%s' release".formatted(nextVersionType.name()));

            latestVersion.increment(nextVersionType);
            getLog().debug("Release version: '%s'".formatted(latestVersion.toString()));

            getLog().debug("Writing release notes to 'Changelog.md' for version '%s'".formatted(latestVersion.toString()));
            var notes = generateNotes(projectBaseDirectory, new MustacheRenderer(), latestVersion.toString(),
                    analyzedCommits.major(), analyzedCommits.minor(), analyzedCommits.patch());

            getLog().debug("Setting project version in 'pom.xml' to '%s'".formatted(latestVersion.toString()));
            var pomXml = projectBaseDirectory.resolve("pom.xml");
            PomHelper.changeVersion(pomXml, latestVersion.toString());

            try {
                scmProvider.addFile(notes);
                scmProvider.addFile(pomXml);

                scmProvider.commit("chore(release): release version %s".formatted(latestVersion.toString()));

                createTag(scmProvider, latestVersion);
            } catch (SCMException e) {
                throw new MojoExecutionException(e);
            }
        }
    }

    private VerifiedConditions verifyConditions(Path projectBaseDirectory) {
        return new VerifiedConditions(
                scmProviderFactories.stream()
                        .map(ServiceLoader.Provider::get)
                        .filter(v -> scmProviderName.equalsIgnoreCase(v.getProviderName()))
                        .map(v -> v.getInstance(projectBaseDirectory))
                        .findAny(),
                commitAnalyzers.stream()
                        .map(ServiceLoader.Provider::get)
                        .filter(v -> commitAnalyzerName.equalsIgnoreCase(v.getName()))
                        .findAny()
        );
    }

    private LatestRelease getLatestRelease(SCMProvider scmProvider) throws MojoExecutionException {
        try {
            var tags = scmProvider.readTags();
            var commits = scmProvider.readCommits(null);

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
            SCMProvider scmProvider,
            CommitAnalyzer commitAnalyzer,
            String latestCommit
    ) throws MojoExecutionException {
        try {
            var commits = scmProvider.readCommits(latestCommit);

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

    private Version.Type verifyRelease(AnalyzedCommits analyzedCommits) {
        Version.Type nextVersionType = null;

        if (!analyzedCommits.major().isEmpty()) {
            nextVersionType = Version.Type.MAJOR;
        } else if (!analyzedCommits.minor().isEmpty()) {
            nextVersionType = Version.Type.MINOR;
        } else if (!analyzedCommits.patch().isEmpty()) {
            nextVersionType = Version.Type.PATCH;
        }

        return nextVersionType;
    }

    private Path generateNotes(
            Path projectBaseDirectory,
            ChangelogRenderer renderer,
            String version,
            List<Commit> major,
            List<Commit> minor,
            List<Commit> patch
    ) throws MojoExecutionException {
        var changelog = projectBaseDirectory.resolve("Changelog.md");

        try (var inputStream = renderer.renderChangelog(changelog, version, major, minor, patch)) {
            Files.write(changelog, inputStream.readAllBytes());
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e.getCause());
        }

        return changelog;
    }

    private void createTag(
            SCMProvider scmProvider,
            Version version
    ) throws MojoExecutionException {
        try {
            scmProvider.tag(version.toString());
        } catch (SCMException e) {
            throw new MojoExecutionException(e.getMessage(), e.getCause());
        }
    }

    private record VerifiedConditions(Optional<SCMProvider> scmProvider, Optional<CommitAnalyzer> commitAnalyzer) {
    }

    private record LatestRelease(Optional<String> latestTag, Optional<String> latestCommit) {
    }

    private record AnalyzedCommits(List<Commit> major, List<Commit> minor, List<Commit> patch) {
    }
}
