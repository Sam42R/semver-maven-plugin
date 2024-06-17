package com.github.sam42r.semver.scm;

import com.github.sam42r.semver.scm.model.Commit;
import com.github.sam42r.semver.scm.model.Tag;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * {@link SCMProvider} for <a href="https://git-scm.com/">git</a>.
 *
 * @author Sam42R
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class GitProvider implements SCMProvider {

    private final Path repositoryPath;
    private Repository repository;

    @Override
    public @NonNull Stream<Commit> readCommits(String fromCommitId) throws SCMException {
        var repository = getRepository();
        try (var git = new Git(repository)) {
            var logCommand = git.log();

            if (fromCommitId != null) {
                logCommand = logCommand.addRange(
                        ObjectId.fromString(fromCommitId),
                        git.getRepository().resolve("HEAD"));
            }

            return StreamSupport.stream(logCommand.call().spliterator(), false)
                    .map(v -> Commit.builder()
                            .id(v.getId().getName())
                            .timestamp(Instant.ofEpochSecond(v.getCommitTime()))
                            .author(v.getAuthorIdent().getName())
                            .message(v.getFullMessage())
                            .build());
        } catch (IOException | GitAPIException e) {
            throw new SCMException(e.getMessage(), e);
        }
    }

    @Override
    public @NonNull Stream<Tag> readTags() throws SCMException {
        var repository = getRepository();
        try (var git = new Git(repository)) {
            return git.tagList().call().stream()
                    .map(v -> Tag.builder()
                            .name(v.getName().replace("refs/tags/", ""))
                            .commitId(getObjectId(repository, v).getName())
                            .build());
        } catch (GitAPIException e) {
            throw new SCMException(e);
        }
    }

    @Override
    public void addFile(@NonNull Path file) throws SCMException {
        var repository = getRepository();
        try (var git = new Git(repository)) {
            // note: jGit seems to accept slash as file separator only; therefore we have to replace the platform file
            // separator to make it run on windows also
            git.add().addFilepattern(repositoryPath.relativize(file).toString().replace(File.separatorChar, '/')).call();
        } catch (GitAPIException e) {
            throw new SCMException(e);
        }
    }

    @Override
    public @NonNull Commit commit(@NonNull String message) throws SCMException {
        var repository = getRepository();
        try (var git = new Git(repository)) {
            var commit = git.commit().setMessage(message).call();
            return Commit.builder()
                    .id(commit.getId().getName())
                    .timestamp(Instant.ofEpochSecond(commit.getCommitTime()))
                    .author(commit.getAuthorIdent().getName())
                    .message(commit.getFullMessage())
                    .build();
        } catch (GitAPIException e) {
            throw new SCMException(e);
        }
    }

    @Override
    public @NonNull Tag tag(@NonNull String name) throws SCMException {
        var repository = getRepository();
        try (var git = new Git(repository)) {
            var tag = git.tag().setName(name).call();
            return Tag.builder()
                    .name(tag.getName().replace("refs/tags/", ""))
                    .commitId(getObjectId(repository, tag).getName())
                    .build();
        } catch (GitAPIException e) {
            throw new SCMException(e);
        }
    }

    private Repository getRepository() throws SCMException {
        if (repository == null) {
            try {
                var gitDirectory = repositoryPath.resolve(".git");
                if (Files.notExists(gitDirectory) || !Files.isDirectory(gitDirectory) || !Files.isReadable(gitDirectory)) {
                    throw new SCMException("Could not find git repository");
                }
                repository = FileRepositoryBuilder.create(gitDirectory.toFile());
            } catch (IOException e) {
                throw new SCMException(e);
            }
        }
        return repository;
    }

    private ObjectId getObjectId(Repository repository, Ref ref) {
        try {
            var peel = repository.getRefDatabase().peel(ref);
            return Optional.ofNullable(peel.getPeeledObjectId()).orElse(ref.getObjectId());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
