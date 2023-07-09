package com.github.sam42r.semver;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;

public abstract class AbstractSemverMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    protected MavenProject project;

    protected Repository getRepository() throws MojoExecutionException, IOException {
        var projectBaseDirectory = project.getFile().getParentFile();
        var gitDirectory = new File(projectBaseDirectory, ".git");

        if (!gitDirectory.exists()) {
            throw new MojoExecutionException("Could not find git repository %s.".formatted(gitDirectory));
        }

        return FileRepositoryBuilder.create(gitDirectory);
    }

    public MavenProject getProject() {
        return project;
    }

    public void setProject(MavenProject project) {
        this.project = project;
    }
}
