package io.github.sam42r.semver.scm;

import io.github.sam42r.semver.scm.model.Commit;
import io.github.sam42r.semver.scm.model.Tag;
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
public class SvnProvider implements SCMProviderFactory<SvnProvider>, SCMProvider {

    private static final String PROVIDER_NAME = "Subversion";

    @Override
    public @NonNull String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public @NonNull SvnProvider getInstance(@NonNull Path path) {
        throw new NotImplementedException();
    }

    @Override
    public @NonNull Stream<Commit> readCommits(String fromCommitId) throws SCMException {
        throw new NotImplementedException();
    }

    @Override
    public @NonNull Stream<Tag> readTags() throws SCMException {
        throw new NotImplementedException();
    }

    @Override
    public void addFile(@NonNull Path file) throws SCMException {
        throw new NotImplementedException();
    }

    @Override
    public @NonNull Commit commit(@NonNull String message) throws SCMException {
        throw new NotImplementedException();
    }

    @Override
    public @NonNull Tag tag(@NonNull String name) throws SCMException {
        throw new NotImplementedException();
    }

    @Override
    public String push(boolean force) throws SCMException {
        throw new NotImplementedException();
    }
}
