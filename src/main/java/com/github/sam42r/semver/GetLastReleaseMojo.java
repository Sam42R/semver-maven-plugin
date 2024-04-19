package com.github.sam42r.semver;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Comparator;
import java.util.stream.StreamSupport;

/**
 * Obtain the commit corresponding to the last release by analyzing
 * <a href="https://git-scm.com/book/en/v2/Git-Basics-Tagging">Git tags</a>.
 *
 * @author Sam42R
 */
@Mojo(name = "get-latest-release")
public class GetLastReleaseMojo extends AbstractSemverMojo {

    @Parameter(name = "option", property = "semver.option", defaultValue = "test")
    private String option;

    @SuppressWarnings("unchecked")
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Get latest release");
        getLog().info("Option: %s".formatted(option));

        try (var repository = getRepository()) {
            var git = new Git(repository);

            var latestTagOpt = git.tagList().call().stream().max(Comparator.comparing(Ref::getName));

            if (latestTagOpt.isPresent()) {
                var latestTag = latestTagOpt.get();
                getLog().info("Found latest release '%s'".formatted(latestTag.getName()));

                var peeelRef = repository.getRefDatabase().peel(latestTag);
                var objectId = peeelRef.getPeeledObjectId() != null ? peeelRef.getPeeledObjectId() : latestTag.getObjectId();

                getPluginContext().put(SemverContextVariable.LATEST_TAG, latestTag.getName());
                getPluginContext().put(SemverContextVariable.LATEST_COMMIT, objectId.getName());
            } else {
                getLog().info("No release found");

                var firstCommit = StreamSupport.stream(git.log().call().spliterator(), false)
                        .min(Comparator.comparing(RevCommit::getCommitTime))
                        .orElseThrow();

                getPluginContext().put(SemverContextVariable.LATEST_TAG, "none");
                getPluginContext().put(SemverContextVariable.LATEST_COMMIT, firstCommit.getId().getName());
            }

            getLog().debug("Latest tag: '%s'".formatted(getPluginContext().get(SemverContextVariable.LATEST_TAG)));
            getLog().debug("Latest commit: '%s'".formatted(getPluginContext().get(SemverContextVariable.LATEST_COMMIT)));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (GitAPIException e) {
            throw new MojoFailureException(e);
        }
    }
}
