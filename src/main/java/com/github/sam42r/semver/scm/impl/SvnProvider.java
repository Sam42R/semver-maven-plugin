package com.github.sam42r.semver.scm.impl;

import com.github.sam42r.semver.scm.SCMProvider;
import com.github.sam42r.semver.scm.model.Commit;
import com.github.sam42r.semver.scm.model.Tag;
import lombok.NonNull;
import org.apache.commons.lang3.NotImplementedException;

import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * {@link SCMProvider} for <a href="https://subversion.apache.org/">subversion</a>.
 * TODO implement
 *
 * @author Sam42R
 */
public class SvnProvider implements SCMProvider {

    private static final String PROVIDER_NAME = "Subversion";

    @Override
    public @NonNull String getName() {
        return PROVIDER_NAME;
    }

    @Override
    public @NonNull Stream<Commit> readCommits(@NonNull Path path) {
        throw new NotImplementedException();
    }

    @Override
    public @NonNull Stream<Tag> readTags(@NonNull Path path) {
        throw new NotImplementedException();
    }
}
