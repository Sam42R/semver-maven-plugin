package io.github.sam42r.semver.scm;

import io.github.sam42r.semver.scm.model.Commit;
import io.github.sam42r.semver.scm.model.Remote;
import io.github.sam42r.semver.scm.model.Tag;
import io.github.sam42r.semver.scm.util.RemoteUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmRevision;
import org.apache.maven.scm.command.changelog.ChangeLogScmRequest;
import org.apache.maven.scm.manager.BasicScmManager;
import org.apache.maven.scm.manager.NoSuchScmProviderException;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;

import java.nio.file.Path;
import java.util.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * This class provides basic functions for <a href="https://maven.apache.org/scm/index.html">Apache Maven SCM</a>
 * based {@link SCMProvider}'s.
 */
@Getter(AccessLevel.PROTECTED)
abstract class AbstractScmProvider implements SCMProvider {

    private static final Predicate<ChangeSet> hasTag = changeSet -> changeSet.getTags() != null && !changeSet.getTags().isEmpty();
    private static final Predicate<ChangeSet> hasTagPath = changeSet -> changeSet.getFiles() != null &&
            !changeSet.getFiles().isEmpty() && changeSet.getFiles().get(0).getName().contains("/tags/");

    private final Path path;
    private final String username;
    private final String password;

    private final String providerType;
    private final ScmProvider provider;

    private final ScmManager scmManager;

    protected AbstractScmProvider(
            String providerType, ScmProvider provider,
            Path path, String username, String password
    ) {
        this.providerType = providerType;
        this.provider = provider;

        this.path = path;
        this.username = username;
        this.password = password;

        this.scmManager = new BasicScmManager();
        this.scmManager.setScmProvider(providerType, provider);
    }

    @Override
    public @NonNull Stream<Commit> readCommits(String fromCommitId) throws SCMException {
        return readCommits(fromCommitId, null);
    }

    private @NonNull Stream<Commit> readCommits(String fromCommitId, String toCommitId) throws SCMException {
        try {
            var repository = getScmRepository();

            var changeLogScmRequest = new ChangeLogScmRequest(repository, new ScmFileSet(path.toFile()));
            if (fromCommitId != null) {
                changeLogScmRequest.setStartRevision(new ScmRevision(fromCommitId));
            } else {
                changeLogScmRequest.setStartDate(Date.from(Instant.EPOCH));
            }
            if (toCommitId != null) {
                changeLogScmRequest.setEndRevision(new ScmRevision(toCommitId));
            }

            var changeLogScmResult = scmManager.changeLog(changeLogScmRequest);
            if (!changeLogScmResult.isSuccess()) {
                throw new SCMException(changeLogScmResult.getProviderMessage(), new IllegalStateException(changeLogScmResult.getCommandOutput()));
            }

            return changeLogScmResult.getChangeLog().getChangeSets().stream()
                    .filter(v -> v.getComment() != null)
                    .map(v -> Commit.builder()
                            .id(v.getRevision())
                            .timestamp(v.getDate().toInstant().truncatedTo(ChronoUnit.SECONDS))
                            .author(v.getAuthor())
                            .message(v.getComment().trim())
                            .build());
        } catch (ScmException e) {
            throw new SCMException(e);
        }
    }

    @Override
    public @NonNull Stream<Tag> readTags() throws SCMException {
        try {
            var repository = getScmRepository();

            var changeLogScmRequest = new ChangeLogScmRequest(repository, new ScmFileSet(path.toFile()));
            changeLogScmRequest.setStartDate(Date.from(Instant.EPOCH));

            var changeLogScmResult = scmManager.changeLog(changeLogScmRequest);
            if (!changeLogScmResult.isSuccess()) {
                throw new SCMException(changeLogScmResult.getProviderMessage(), new IllegalStateException(changeLogScmResult.getCommandOutput()));
            }

            return changeLogScmResult.getChangeLog().getChangeSets().stream()
                    .filter(hasTag.or(hasTagPath))
                    .map(v -> Tag.builder()
                            .name(getTag(v))
                            .commitId(v.getRevision())
                            .build());
        } catch (ScmException e) {
            throw new SCMException(e);
        }
    }

    private String getTag(@NonNull ChangeSet changeSet) {
        if (hasTag.test(changeSet)) {
            return changeSet.getTags().get(0);
        } else if (hasTagPath.test(changeSet)) {
            var filename = changeSet.getFiles().get(0).getName();
            return filename.substring(filename.lastIndexOf("/") + 1);
        }
        return null;
    }

    @Override
    public void addFile(@NonNull Path file) throws SCMException {
        try {
            var repository = getScmRepository();

            var addScmResult = scmManager.add(repository, new ScmFileSet(path.toFile(), file.toFile()));
            assert addScmResult.isSuccess();
        } catch (ScmException e) {
            throw new SCMException(e);
        }
    }

    @Override
    public @NonNull Commit commit(@NonNull String message) throws SCMException {
        try {
            var repository = getScmRepository();

            var checkInScmResult = scmManager.checkIn(repository, new ScmFileSet(path.toFile()), message);
            var scmRevision = checkInScmResult.getScmRevision();

            return readCommits(scmRevision, scmRevision).findFirst().orElseThrow();
        } catch (ScmException e) {
            throw new SCMException(e);
        }
    }

    @Override
    public @NonNull Tag tag(@NonNull String name) throws SCMException {
        try {
            var repository = getScmRepository();

            var tagScmResult = scmManager.tag(repository, new ScmFileSet(path.toFile()), name);
            assert tagScmResult.isSuccess();

            return readTags().filter(v -> name.equals(v.getName())).findFirst().orElseThrow();
        } catch (ScmException e) {
            throw new SCMException(e);
        }
    }

    @Override
    public @NonNull Remote getRemote() throws SCMException {
        return RemoteUtil.parseUrl(getRemoteUrl().orElseThrow());
    }

    protected ScmRepository getScmRepository() throws SCMException {
        try {
            var scmRepository = scmManager.makeScmRepository("scm:%s:%s".formatted(
                    providerType, getRemoteUrl().orElse("")));
            scmRepository.getProviderRepository().setPushChanges(false);
            return scmRepository;
        } catch (ScmRepositoryException | NoSuchScmProviderException e) {
            throw new SCMException(e);
        }
    }

    protected abstract Optional<String> getRemoteUrl() throws SCMException;
}
