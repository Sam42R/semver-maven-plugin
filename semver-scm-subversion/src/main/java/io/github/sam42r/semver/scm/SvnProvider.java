package io.github.sam42r.semver.scm;

import io.github.sam42r.semver.scm.model.Commit;
import io.github.sam42r.semver.scm.model.Remote;
import io.github.sam42r.semver.scm.model.Tag;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;

import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * {@link SCMProvider} for <a href="https://subversion.apache.org/">subversion</a>.
 * TODO implement
 *
 * @author Sam42R
 */
@Slf4j
@RequiredArgsConstructor
public class SvnProvider implements SCMProvider {

    private final Path repositoryPath;
    private final String username;
    private final String password;

    private SVNRepository repository;

    @Override
    public @NonNull Stream<Commit> readCommits(String fromCommitId) throws SCMException {
        var repository = getRepository();
        try {
            var startRevision = 1L; // revision 0 (repository initialization) should be skipped
            var endRevision = -1L; // HEAD (the latest) revision

            Stream<?> rawStream = repository.log(new String[]{""}, null, startRevision, endRevision, true, true).stream();
            var svnLogEntries = rawStream.filter(v -> v instanceof SVNLogEntry).map(v -> (SVNLogEntry) v).toList();

            return svnLogEntries.stream()
                    .map(v -> Commit.builder()
                            .id(Long.toString(v.getRevision()))
                            .timestamp(v.getDate().toInstant())
                            .author(v.getAuthor())
                            .message(v.getMessage())
                            .build());
        } catch (SVNException e) {
            throw new SCMException(e);
        }
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

    private SVNRepository getRepository() throws SCMException {
        if (repository == null) {
            try {
                var svnUrl = SVNURL.fromFile(repositoryPath.toFile());
                repository = SVNRepositoryFactory.create(svnUrl);
            } catch (SVNException e) {
                throw new SCMException(e);
            }
        }
        return repository;
    }
}
