package com.github.sam42r.semver.scm;

import com.github.sam42r.semver.scm.model.Commit;
import com.github.sam42r.semver.scm.model.Tag;
import lombok.NonNull;

import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * A Source-Code-Management system provider enables access to version control information.
 *
 * @author Sam42R
 */
public interface SCMProvider {

    @NonNull Stream<Commit> readCommits(String fromCommitId) throws SCMException;

    @NonNull Stream<Tag> readTags() throws SCMException;

    void addFile(@NonNull Path file) throws SCMException;

    @NonNull Commit commit(@NonNull String message) throws SCMException;

    @NonNull Tag tag(@NonNull String name) throws SCMException;
}
