package com.github.sam42r.semver;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.ContextEnabled;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Comparator;
import java.util.stream.StreamSupport;

@Mojo(name = "analyze-commit-messages")
public class AnalyzeCommitsMojo extends AbstractSemverMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Analyzing commit messages");

        var latestCommit = getPluginContext().get(SemverContextVariable.LATEST_COMMIT);
        if(latestCommit == null) {
            throw new MojoExecutionException("Could NOT find '%s' in plugin context.".formatted(SemverContextVariable.LATEST_COMMIT));
        }

        try (var repository = getRepository()) {
            var git = new Git(repository);

            final var logCommand = git.log();

            var latestTagRefOpt = git.tagList().call().stream().max(Comparator.comparing(Ref::getName));
            if (latestTagRefOpt.isPresent()) {
                var latestTagRef = latestTagRefOpt.get();

                getLog().info("Analyzing commit messages since tag '%s'".formatted(latestTagRef.getName()));
                logCommand.addRange(
                        latestTagRef.getObjectId(),
                        git.getRepository().resolve("HEAD")
                );
            }

            var revCommits = logCommand.call();

            var fixes = StreamSupport.stream(revCommits.spliterator(), false)
                    .filter(c -> c.getFullMessage().startsWith("fix"))
                    .toList();
            var features = StreamSupport.stream(revCommits.spliterator(), false)
                    .filter(c -> c.getFullMessage().startsWith("feat"))
                    .toList();
            var breakingChanges = StreamSupport.stream(revCommits.spliterator(), false)
                    .filter(c -> c.getFullMessage().contains("BREAKING CHANGE"))
                    .toList();

            getLog().info("Found %d fix(es), %d feature(s) and %d breaking change(s)".formatted(fixes.size(), features.size(), breakingChanges.size()));

            // TODO parse existing tag or fallback to 1.0.0
            // TODO use parameter to set tag pattern
            // TODO use class for tag parsing

            // TODO increment major, minor or patch based on found commits
            // TODO create new tag if something changed

            // git.tag().setName("v1.0.2").setMessage("Semantic Release").call(); // FIXME
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (GitAPIException e) {
            throw new MojoFailureException(e);
        }
    }
}
