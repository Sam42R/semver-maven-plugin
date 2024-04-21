package com.github.sam42r.semver;

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

    private final ServiceLoader<SCMProvider> serviceLoader = ServiceLoader.load(SCMProvider.class);

    @Setter
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Setter
    @Parameter(name = "scm-provider-name", defaultValue = "Git")
    private String scmProviderName;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        var projectBaseDirectory = project.getFile().getParentFile().toPath();
        getLatestRelease(projectBaseDirectory);
    }

    @SuppressWarnings("unchecked")
    private void getLatestRelease(Path projectBaseDirectory) throws MojoExecutionException {
        try {
            var scmProvider = serviceLoader.stream()
                    .map(ServiceLoader.Provider::get)
                    .filter(v -> scmProviderName.equalsIgnoreCase(v.getName()))
                    .findAny()
                    .orElseThrow(() -> new IllegalArgumentException("Could not find SCM provider with name '%s'".formatted(scmProviderName)));

            getLog().info("Running semantic-release with '%s'".formatted(scmProvider.getClass().getSimpleName()));

            var commits = scmProvider.readCommits(projectBaseDirectory);
            var tags = scmProvider.readTags(projectBaseDirectory);

            var latestTagOpt = tags.max(Comparator.comparing(Tag::getName));
            var latestCommitOpt = latestTagOpt.map(Tag::getCommitId)
                    .or(() -> commits.min(Comparator.comparing(Commit::getTimestamp)).map(Commit::getId));

            getPluginContext().put(LATEST_TAG, latestTagOpt.map(Tag::getName).orElse("None"));
            getPluginContext().put(LATEST_COMMIT, latestCommitOpt.orElse("None"));

            getLog().debug("Latest tag: '%s'".formatted(getPluginContext().get(LATEST_TAG)));
            getLog().debug("Latest commit: '%s'".formatted(getPluginContext().get(LATEST_COMMIT)));
            // TODO
        } catch (SCMException e) {
            throw new MojoExecutionException(e.getMessage(), e.getCause());
        }
    }
}
