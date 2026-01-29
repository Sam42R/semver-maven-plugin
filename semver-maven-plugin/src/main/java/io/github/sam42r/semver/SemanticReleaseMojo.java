package io.github.sam42r.semver;

import io.github.sam24r.semver.release.ReleaseException;
import io.github.sam24r.semver.release.ReleasePublisher;
import io.github.sam24r.semver.release.ReleasePublisherFactory;
import io.github.sam24r.semver.release.model.ReleaseInfo;
import io.github.sam42r.semver.analyzer.CommitAnalyzer;
import io.github.sam42r.semver.analyzer.CommitAnalyzerFactory;
import io.github.sam42r.semver.analyzer.model.AnalyzedCommit;
import io.github.sam42r.semver.changelog.ChangelogRenderer;
import io.github.sam42r.semver.changelog.ChangelogRendererFactory;
import io.github.sam42r.semver.changelog.model.VersionInfo;
import io.github.sam42r.semver.model.Version;
import io.github.sam42r.semver.scm.SCMException;
import io.github.sam42r.semver.scm.SCMProvider;
import io.github.sam42r.semver.scm.SCMProviderFactory;
import io.github.sam42r.semver.scm.model.Commit;
import io.github.sam42r.semver.scm.model.Tag;
import io.github.sam42r.semver.util.PomHelper;
import io.github.sam42r.semver.util.TagVersionComparator;
import lombok.NonNull;
import lombok.Setter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * @author Sam42R
 */
@Mojo(name = "semantic-release")
@SuppressWarnings("rawtypes")
public class SemanticReleaseMojo extends AbstractMojo {

    private static final String POM = "pom.xml";

    @Setter
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Setter
    @Parameter(name = "tag-format", defaultValue = Version.TAG_FORMAT_DEFAULT)
    private String tagFormat;

    @Setter
    @Parameter
    private Scm scm;

    @Setter
    @Parameter
    private Analyzer analyzer;

    @Setter
    @Parameter
    private Changelog changelog;

    @Setter
    @Parameter
    private Release release;

    private final Map<String, SCMProviderFactory> scmProviderFactories;
    private final Map<String, CommitAnalyzerFactory> commitAnalyzerFactories;
    private final Map<String, ChangelogRendererFactory> changelogRendererFactories;
    private final Map<String, ReleasePublisherFactory> releasePublisherFactories;

    @Inject
    public SemanticReleaseMojo(
            Map<String, SCMProviderFactory> scmProviderFactories,
            Map<String, CommitAnalyzerFactory> commitAnalyzerFactories,
            Map<String, ChangelogRendererFactory> changelogRendererFactories,
            Map<String, ReleasePublisherFactory> releasePublisherFactories
    ) {
        this.scmProviderFactories = scmProviderFactories;
        this.commitAnalyzerFactories = commitAnalyzerFactories;
        this.changelogRendererFactories = changelogRendererFactories;
        this.releasePublisherFactories = releasePublisherFactories;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (scm == null) {
            scm = Scm.builder().build();
        }
        if (analyzer == null) {
            analyzer = Analyzer.builder().build();
        }
        if (changelog == null) {
            changelog = Changelog.builder().build();
        }
        if (release == null) {
            release = Release.builder().build();
        }

        final var projectBaseDirectory = isModule(project) ?
                project.getFile().getParentFile().getParentFile().toPath() :
                project.getFile().getParentFile().toPath();

        var verifiedConditions = verifyConditions(projectBaseDirectory);
        var scmProvider = verifiedConditions.scmProvider()
                .orElseThrow(() -> new IllegalArgumentException("Could not find SCM provider with name '%s'".formatted(scm.getProviderName())));
        var commitAnalyzer = verifiedConditions.commitAnalyzer()
                .orElseThrow(() -> new IllegalArgumentException("Could not find commit analyzer with specification name '%s'".formatted(analyzer.getSpecificationName())));
        var changelogRenderer = verifiedConditions.changelogRenderer()
                .orElseThrow(() -> new IllegalArgumentException("Could not find changelog renderer with name '%s'".formatted(changelog.getRendererName())));
        var releasePublisher = verifiedConditions.releasePublisher()
                .orElseThrow(() -> new IllegalArgumentException("Could not find release publisher with name '%s'".formatted(release.getPublisherName())));
        getLog().info("Running semantic-release with SCM provider '%s'".formatted(scmProvider.getClass().getSimpleName()));
        getLog().info("Running semantic-release with commit analyzer '%s'".formatted(commitAnalyzer.getClass().getSimpleName()));
        getLog().info("Running semantic-release with changelog renderer '%s'".formatted(changelogRenderer.getClass().getSimpleName()));
        getLog().info("Running semantic-release with release publisher '%s'".formatted(releasePublisher.getClass().getSimpleName()));

        var latestRelease = getLatestRelease(scmProvider);
        var latestTag = latestRelease.latestTag().orElse("None");
        var latestCommit = latestRelease.latestCommit().orElse("None");
        getLog().debug("Latest tag: '%s'".formatted(latestTag));
        getLog().debug("Latest commit: '%s'".formatted(latestCommit));

        var latestVersion = latestRelease.latestTag()
                .map(t -> Version.of(t, tagFormat))
                .orElse(Version.of(0, 0, 0, tagFormat));
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
            var versionInfo = new VersionInfo(
                    latestVersion.toString(),
                    LocalDateTime.now().format(DateTimeFormatter.ISO_DATE),
                    "" // TODO read docs(changelog) commits and add as release description
            );
            var notes = generateNotes(projectBaseDirectory, changelogRenderer, versionInfo, analyzedCommits);

            getLog().debug("Setting project version in '%s' to '%s'".formatted(POM, latestVersion.toString()));
            var pomXml = projectBaseDirectory.resolve(POM);
            PomHelper.changeVersion(pomXml, latestVersion.toString());

            var modules = isModule(project) ? project.getParent().getModules() : project.getModules();
            var modulePomsXml = new ArrayList<Path>();
            for (var module : modules) {
                var modulePomXml = projectBaseDirectory.resolve(module).resolve(POM);
                PomHelper.changeParentVersion(modulePomXml, latestVersion.toString());

                modulePomsXml.add(modulePomXml);
            }

            try {
                scmProvider.addFile(notes);
                scmProvider.addFile(pomXml);

                for (var modulePomXml : modulePomsXml) {
                    scmProvider.addFile(modulePomXml);
                }

                scmProvider.commit(commitAnalyzer.generateReleaseCommitMessage(latestVersion.toString()));

                createTag(scmProvider, latestVersion);

                if (scm.isPush()) {
                    publish(scmProvider);
                }

                if (release.isPublish()) {
                    notify(scmProvider, releasePublisher, latestVersion);
                }
            } catch (SCMException e) {
                throw new MojoExecutionException(e);
            }
        }
    }

    private boolean isModule(@NonNull MavenProject mavenProject) {
        getLog().debug("Checking if project is maven module");
        if (project.hasParent()) {
            var parentProjectDirectory = mavenProject.getFile().getParentFile().getParentFile().toPath();
            getLog().debug("Looking for '%s' in '%s'".formatted(POM, parentProjectDirectory.toAbsolutePath().toString()));
            return Files.exists(parentProjectDirectory.resolve(POM));
        }
        return false;
    }

    private VerifiedConditions verifyConditions(Path projectBaseDirectory) {
        return new VerifiedConditions(
                scmProviderFactories.entrySet().stream()
                        .filter(v -> v.getKey().equalsIgnoreCase(scm.getProviderName()))
                        .map(Map.Entry::getValue)
                        .map(v -> v.getInstance(projectBaseDirectory, scm.getUsername(), scm.getPassword()))
                        .findAny(),
                commitAnalyzerFactories.entrySet().stream()
                        .filter(v -> v.getKey().equalsIgnoreCase(analyzer.getSpecificationName()))
                        .map(Map.Entry::getValue)
                        .map(v -> v.getInstance(analyzer.getConfigurationPath()))
                        .findAny(),
                changelogRendererFactories.entrySet().stream()
                        .filter(v -> v.getKey().equalsIgnoreCase(changelog.getRendererName()))
                        .map(Map.Entry::getValue)
                        .map(v -> v.getInstance(changelog.getTemplate()))
                        .findAny(),
                releasePublisherFactories.entrySet().stream()
                        .filter(v -> v.getKey().equalsIgnoreCase(release.getPublisherName()))
                        .map(Map.Entry::getValue)
                        .map(v -> v.getInstance(release.getUsername(), release.getPassword()))
                        .findAny()
        );
    }

    private LatestReleaseInfo getLatestRelease(SCMProvider scmProvider) throws MojoExecutionException {
        try {
            var tags = scmProvider.readTags();
            var commits = scmProvider.readCommits(null);

            var latestTagOpt = tags.max(new TagVersionComparator(tagFormat));
            var latestCommitOpt = latestTagOpt.map(Tag::getCommitId)
                    .or(() -> commits.min(Comparator.comparing(Commit::getTimestamp)).map(Commit::getId));

            return new LatestReleaseInfo(
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
            VersionInfo versionInfo,
            List<AnalyzedCommit> analyzedCommits
    ) throws MojoExecutionException {
        var path = projectBaseDirectory.resolve("Changelog.md");
        try (var inputStream = renderer.renderChangelog(path, versionInfo, analyzedCommits)) {
            Files.write(path, inputStream.readAllBytes());
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e.getCause());
        }

        return path;
    }

    private void createTag(
            SCMProvider scmProvider,
            Version version
    ) throws MojoExecutionException {
        try {
            scmProvider.tag(version.toTag());
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

    private void notify(
            SCMProvider scmProvider,
            ReleasePublisher releasePublisher,
            Version version
    ) throws MojoExecutionException {
        try {
            var remote = scmProvider.getRemote();

            releasePublisher.publish(
                    remote.getScheme(),
                    remote.getHost(),
                    remote.getGroup(),
                    remote.getProject(),
                    ReleaseInfo.builder()
                            .time(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS))
                            .tagName(version.toTag())
                            .name(version.toString())
                            //.description("TODO")
                            .build()
            );
        } catch (SCMException | ReleaseException e) {
            throw new MojoExecutionException(e.getMessage(), e.getCause());
        }
    }

    private record VerifiedConditions(Optional<SCMProvider> scmProvider,
                                      Optional<CommitAnalyzer> commitAnalyzer,
                                      Optional<ChangelogRenderer> changelogRenderer,
                                      Optional<ReleasePublisher> releasePublisher) {
    }

    private record LatestReleaseInfo(Optional<String> latestTag, Optional<String> latestCommit) {
    }
}
