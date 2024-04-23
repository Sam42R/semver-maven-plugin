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

    @NonNull String getName();

    @NonNull Stream<Commit> readCommits(@NonNull Path path) throws SCMException;

    @NonNull Stream<Commit> readCommits(@NonNull Path path, @NonNull String fromCommitId) throws SCMException;

    @NonNull Stream<Tag> readTags(@NonNull Path path) throws SCMException;
}
