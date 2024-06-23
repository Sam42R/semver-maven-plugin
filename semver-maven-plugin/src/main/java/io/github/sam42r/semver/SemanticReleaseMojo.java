package io.github.sam42r.semver;

import io.github.sam42r.semver.analyzer.CommitAnalyzer;
import io.github.sam42r.semver.analyzer.model.AnalyzedCommit;
import io.github.sam42r.semver.changelog.ChangelogRenderer;
import io.github.sam42r.semver.changelog.MustacheRenderer;
import io.github.sam42r.semver.model.Version;
import io.github.sam42r.semver.scm.SCMException;
import io.github.sam42r.semver.scm.SCMProvider;
import io.github.sam42r.semver.scm.SCMProviderFactory;
import io.github.sam42r.semver.scm.model.Commit;
import io.github.sam42r.semver.scm.model.Tag;
import io.github.sam42r.semver.util.PomHelper;
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
import java.util.*;

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
    @Parameter
    private Scm scm;

    @Setter
    @Parameter(name = "commit-analyzer-name", defaultValue = "Conventional")
    private String commitAnalyzerName;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (scm == null) {
            scm = Scm.builder().build();
        }

        final var projectBaseDirectory = project.hasParent() ?
                project.getFile().getParentFile().getParentFile().toPath() :
                project.getFile().getParentFile().toPath();

        var verifiedConditions = verifyConditions(projectBaseDirectory);
        var scmProvider = verifiedConditions.scmProvider()
                .orElseThrow(() -> new IllegalArgumentException("Could not find SCM provider with name '%s'".formatted(scm.getProviderName())));
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

        var majorCount = analyzedCommits.stream().filter(AnalyzedCommit.isBreaking).count();
        var minorCount = analyzedCommits.stream().filter(AnalyzedCommit.isFeature).count();
        var patchCount = analyzedCommits.stream().filter(AnalyzedCommit.isBugfix).count();
        getLog().debug("Found %d major, %d minor and %d patch commits".formatted(
                majorCount, minorCount, patchCount));

        var nextVersionType = verifyRelease(majorCount > 0, minorCount > 0, patchCount > 0);

        if (nextVersionType == null) {
            getLog().info("No commits found to release");
        } else {
            getLog().info("Continue with '%s' release".formatted(nextVersionType.name()));

            latestVersion.increment(nextVersionType);
            getLog().debug("Release version: '%s'".formatted(latestVersion.toString()));

            getLog().debug("Writing release notes to 'Changelog.md' for version '%s'".formatted(latestVersion.toString()));
            var notes = generateNotes(projectBaseDirectory, new MustacheRenderer(), latestVersion.toString(), analyzedCommits);

            getLog().debug("Setting project version in 'pom.xml' to '%s'".formatted(latestVersion.toString()));
            var pomXml = projectBaseDirectory.resolve("pom.xml");
            PomHelper.changeVersion(pomXml, latestVersion.toString());

            var modulePomsXml = new ArrayList<Path>();
            if (project.hasParent()) {
                for (var module : project.getParent().getModules()) {
                    var modulePomXml = projectBaseDirectory.resolve(module).resolve("pom.xml");
                    PomHelper.changeParentVersion(modulePomXml, latestVersion.toString());

                    modulePomsXml.add(modulePomXml);
                }
            }

            try {
                scmProvider.addFile(notes);
                scmProvider.addFile(pomXml);

                for (var modulePomXml : modulePomsXml) {
                    scmProvider.addFile(modulePomXml);
                }

                scmProvider.commit(commitAnalyzer.generateReleaseCommitMessage(latestVersion.toString()));

                createTag(scmProvider, latestVersion);

                if(scm.isPush()) {
                    publish(scmProvider);
                }
            } catch (SCMException e) {
                throw new MojoExecutionException(e);
            }
        }
    }

    private VerifiedConditions verifyConditions(Path projectBaseDirectory) {
        return new VerifiedConditions(
                scmProviderFactories.stream()
                        .map(ServiceLoader.Provider::get)
                        .filter(v -> scm.getProviderName().equalsIgnoreCase(v.getProviderName()))
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

    private List<AnalyzedCommit> analyzeCommits(
            SCMProvider scmProvider,
            CommitAnalyzer commitAnalyzer,
            String latestCommit
    ) throws MojoExecutionException {
        try {
            var commits = scmProvider.readCommits(latestCommit);
            return commitAnalyzer.analyzeCommits(commits.toList());
        } catch (SCMException e) {
            throw new MojoExecutionException(e.getMessage(), e.getCause());
        }
    }

    private Version.Type verifyRelease(boolean hasMajor, boolean hasMinor, boolean hasPatch) {
        Version.Type nextVersionType = null;

        if (hasMajor) {
            nextVersionType = Version.Type.MAJOR;
        } else if (hasMinor) {
            nextVersionType = Version.Type.MINOR;
        } else if (hasPatch) {
            nextVersionType = Version.Type.PATCH;
        }

        return nextVersionType;
    }

    private Path generateNotes(
            Path projectBaseDirectory,
            ChangelogRenderer renderer,
            String version,
            List<AnalyzedCommit> analyzedCommits
    ) throws MojoExecutionException {
        var changelog = projectBaseDirectory.resolve("Changelog.md");

        try (var inputStream = renderer.renderChangelog(changelog, version, analyzedCommits)) {
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

    private void publish(SCMProvider scmProvider) throws MojoExecutionException {
        try {
            var messages = scmProvider.push(false);
            getLog().debug(messages);
        } catch (SCMException e) {
            throw new MojoExecutionException(e.getMessage(), e.getCause());
        }
    }

    private record VerifiedConditions(Optional<SCMProvider> scmProvider, Optional<CommitAnalyzer> commitAnalyzer) {
    }

    private record LatestRelease(Optional<String> latestTag, Optional<String> latestCommit) {
    }
}
