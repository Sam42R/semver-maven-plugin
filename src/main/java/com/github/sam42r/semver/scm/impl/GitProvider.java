package com.github.sam42r.semver.scm.impl;

import com.github.sam42r.semver.scm.SCMException;
import com.github.sam42r.semver.scm.SCMProvider;
import com.github.sam42r.semver.scm.model.Commit;
import com.github.sam42r.semver.scm.model.Tag;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

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
@NoArgsConstructor
public class GitProvider implements SCMProvider {

    private static final String PROVIDER_NAME = "Git";

    @Override
    public @NonNull String getName() {
        return PROVIDER_NAME;
    }

    @Override
    public @NonNull Stream<Commit> readCommits(@NonNull Path path, String fromCommitId) throws SCMException {
        var repository = getRepository(path);
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
    public @NonNull Stream<Tag> readTags(@NonNull Path path) throws SCMException {
        var repository = getRepository(path);
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

    private Repository getRepository(Path path) throws SCMException {
        try {
            var gitDirectory = path.resolve(".git");
            if (Files.notExists(gitDirectory) || !Files.isDirectory(gitDirectory) || !Files.isReadable(gitDirectory)) {
                throw new SCMException("Could not find git repository");
            }
            return FileRepositoryBuilder.create(gitDirectory.toFile());
        } catch (IOException e) {
            throw new SCMException(e);
        }
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
