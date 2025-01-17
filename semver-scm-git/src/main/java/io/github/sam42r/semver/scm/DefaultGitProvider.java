package io.github.sam42r.semver.scm;

import lombok.NonNull;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.maven.scm.provider.git.jgit.JGitScmProvider;
import org.apache.maven.scm.provider.git.jgit.command.JGitUtils;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.Transport;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class DefaultGitProvider extends AbstractScmProvider {

    public DefaultGitProvider(Path path, String username, String password) {
        super("jgit", new JGitScmProvider(null), path, username, password);
    }

    @Override
    protected Optional<String> getRemoteUrl() throws SCMException {
        var configPath = getPath().resolve(".git").resolve("config");

        if (Files.notExists(configPath)) {
            throw new SCMException("Could not find git configuration");
        }

        try (var inputStream = Files.newInputStream(configPath, StandardOpenOption.READ);
             var reader = new InputStreamReader(inputStream)) {
            var iniConfiguration = new INIConfiguration();
            iniConfiguration.read(reader);
            return Optional.ofNullable(iniConfiguration.getSection("remote \"origin\"").getString("url"));
        } catch (IOException | ConfigurationException e) {
            throw new SCMException(e);
        }
    }

    @Override
    public @NonNull String push(boolean force) throws SCMException {
        try (var git = JGitUtils.openRepo(getPath().toFile())) {
            var scmProviderRepository = getScmRepository().getProviderRepository();
            if (scmProviderRepository instanceof GitScmProviderRepository repo) {
                repo.setUser(getUsername());
                repo.setPassword(getPassword());
                repo.setPassphrase(getPassword());

                var branch = git.getRepository().getBranch();
                var branchRefSpec = new RefSpec(Constants.R_HEADS + branch + ":" + Constants.R_HEADS + branch);
                var pushBranchResults = JGitUtils.push(git, repo, branchRefSpec);

                var tagRefSpec = Transport.REFSPEC_TAGS;
                var pushTagResults = JGitUtils.push(git, repo, tagRefSpec);

                return Stream.concat(
                                StreamSupport.stream(pushBranchResults.spliterator(), false),
                                StreamSupport.stream(pushTagResults.spliterator(), false)
                        )
                        .map(PushResult::getMessages)
                        .collect(Collectors.joining(System.lineSeparator()));
            }
            throw new IllegalStateException("Invalid provider repository found '%s'"
                    .formatted(scmProviderRepository.getClass().getSimpleName()));
        } catch (IOException | GitAPIException e) {
            throw new SCMException(e);
        }
    }
}
