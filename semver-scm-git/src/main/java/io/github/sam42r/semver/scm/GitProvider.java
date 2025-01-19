package io.github.sam42r.semver.scm;


import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import io.github.sam42r.semver.scm.model.Commit;
import io.github.sam42r.semver.scm.model.Remote;
import io.github.sam42r.semver.scm.model.Tag;
import io.github.sam42r.semver.scm.util.RemoteUtil;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.transport.ssh.jsch.JschConfigSessionFactory;
import org.eclipse.jgit.transport.ssh.jsch.OpenSshConfig;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * {@link SCMProvider} for <a href="https://git-scm.com/">git</a>.
 *
 * @author Sam42R
 * @deprecated since switching to Maven-SCM {@link DefaultGitProvider} is used instead
 */
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Deprecated(since = "1.5.0", forRemoval = true)
public class GitProvider implements SCMProvider {

    private final Path repositoryPath;
    private final String username;
    private final String password;

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

    @Override
    public String push(boolean force) throws SCMException {
        var repository = getRepository();
        try (var git = new Git(repository)) {
            var pushBranchResults = git.push()
                    .setTransportConfigCallback(this::configureTransport)
                    .setRemote("origin")
                    .setForce(force)
                    .call();

            var pushTagResults = git.push()
                    .setTransportConfigCallback(this::configureTransport)
                    .setRemote("origin")
                    .setForce(force)
                    .setPushTags() // pushes tags only
                    .call();

            return Stream.concat(
                            StreamSupport.stream(pushBranchResults.spliterator(), false),
                            StreamSupport.stream(pushTagResults.spliterator(), false)
                    )
                    .map(PushResult::getMessages)
                    .collect(Collectors.joining(System.lineSeparator()));
        } catch (GitAPIException e) {
            throw new SCMException(e);
        }
    }

    @Override
    public @NonNull Remote getRemote() throws SCMException {
        var repository = getRepository();
        try (var git = new Git(repository)) {
            var remoteConfigs = git.remoteList().call();
            var url = remoteConfigs.stream()
                    .filter(v -> "origin".equals(v.getName()))
                    .map(RemoteConfig::getURIs)
                    .flatMap(List::stream)
                    .map(URIish::toString)
                    .findAny()
                    .orElseThrow();
            return RemoteUtil.parseUrl(url);
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

    private void configureTransport(@NonNull Transport transport) {
        if (transport instanceof SshTransport sshTransport) {
            configureSshTransport(sshTransport);
        } else if (transport instanceof HttpTransport httpTransport) {
            configureHttpTransport(httpTransport);
        } else {
            log.warn("Could not configure transport '{}'", transport.getClass().getSimpleName());
        }
    }

    private void configureSshTransport(@NonNull SshTransport sshTransport) {
        log.debug("Configure SshTransport with SSH keys");

        var jschConfigSessionFactory = new JschConfigSessionFactory() {
            @Override
            protected void configureJSch(JSch jsch) {
                super.configureJSch(jsch);

                Predicate<Path> isKeyFile = path -> path.getFileName().toString().toLowerCase().startsWith("id_");
                Predicate<Path> isPublicKeyFile = path -> path.getFileName().toString().toLowerCase().endsWith(".pub");

                var sshDirectory = SystemUtils.getUserHome().toPath().resolve(".ssh");
                try (var sshConfigFiles = Files.walk(sshDirectory)) {
                    var sshKeyFiles = sshConfigFiles.filter(isKeyFile).toList();

                    var publicKeyPath = sshKeyFiles.stream()
                            .filter(isPublicKeyFile)
                            .sorted()
                            .findFirst();
                    var publicKeyBytes = publicKeyPath.map(this::readAllBytes)
                            .orElseThrow(() -> new IllegalStateException("Could not find public key file"));

                    var privateKeyPath = sshKeyFiles.stream()
                            .filter(Predicate.not(isPublicKeyFile))
                            .sorted()
                            .findFirst();
                    var privateKeyBytes = privateKeyPath.map(this::readAllBytes)
                            .orElseThrow(() -> new IllegalStateException("Could not find private key file"));

                    var privateKeyPassword = Optional.ofNullable(password).map(String::getBytes).orElse(null);

                    jsch.addIdentity(null, privateKeyBytes, publicKeyBytes, privateKeyPassword);
                } catch (IOException | JSchException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            protected void configure(OpenSshConfig.Host hc, Session session) {
                // do nothing
                // session.setPassword("***"); // NOT supported by GitHub
            }

            private byte[] readAllBytes(@NonNull Path path) {
                try {
                    return Files.readAllBytes(path);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        };
        sshTransport.setSshSessionFactory(jschConfigSessionFactory);
    }


    private void configureHttpTransport(@NonNull HttpTransport httpTransport) {
        log.debug("Configure HttpTransport with username and password");

        var usernameToSet = Optional.ofNullable(username).orElse(SystemUtils.getUserName());
        var passwordToSet = Optional.ofNullable(password).orElse("");

        httpTransport.setCredentialsProvider(
                new UsernamePasswordCredentialsProvider(usernameToSet, passwordToSet)
        );
    }
}
