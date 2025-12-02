package io.github.sam42r.semver.scm;

import io.github.sam42r.semver.scm.model.Commit;
import io.github.sam42r.semver.scm.model.Remote;
import io.github.sam42r.semver.scm.model.Tag;
import lombok.NonNull;
import org.apache.commons.lang3.NotImplementedException;

import javax.inject.Named;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * {@link SCMProvider} for <a href="https://www.mercurial-scm.org/">mercurial</a>.
 * TODO implement
 *
 * @author Sam42R
 */
@Named("Mercurial")
public class HgProvider implements SCMProviderFactory<HgProvider>, SCMProvider {

    @Override
    public @NonNull HgProvider getInstance(@NonNull Path path, String username, String password) {
        throw new NotImplementedException();
    }

    @Override
    public @NonNull Stream<Commit> readCommits(String fromCommitId) throws SCMException {
        throw new SCMException(new NotImplementedException());
    }

    @Override
    public @NonNull Stream<Tag> readTags() throws SCMException {
        throw new SCMException(new NotImplementedException());
    }

    @Override
    public void addFile(@NonNull Path file) throws SCMException {
        throw new SCMException(new NotImplementedException());
    }

    @Override
    public @NonNull Commit commit(@NonNull String message) throws SCMException {
        throw new SCMException(new NotImplementedException());
    }

    @Override
    public @NonNull Tag tag(@NonNull String name) throws SCMException {
        throw new SCMException(new NotImplementedException());
    }

    @Override
    public String push(boolean force) throws SCMException {
        throw new SCMException(new NotImplementedException());
    }

    @Override
    public @NonNull Remote getRemote() throws SCMException {
        throw new SCMException(new NotImplementedException());
    }
}
