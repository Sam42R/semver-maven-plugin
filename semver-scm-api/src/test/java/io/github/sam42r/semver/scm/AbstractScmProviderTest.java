package io.github.sam42r.semver.scm;

import io.github.sam42r.semver.scm.AbstractScmProvider;
import io.github.sam42r.semver.scm.SCMException;
import io.github.sam42r.semver.scm.SCMProvider;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

public class AbstractScmProviderTest {

    @TempDir
    protected Path tempDirectory;
    protected SCMProvider uut;

    protected StatusScmResult status(SCMProvider scmProvider) throws SCMException {
        if (scmProvider instanceof AbstractScmProvider abstractScmProvider) {
            var path = abstractScmProvider.getPath();
            var scmManager = abstractScmProvider.getScmManager();
            var scmRepository = abstractScmProvider.getScmRepository();

            try {
                return scmManager.status(scmRepository, new ScmFileSet(path.toFile()));
            } catch (ScmException e) {
                throw new SCMException(e);
            }
        }
        throw new IllegalArgumentException("Invalid SCMProvider '%s'".formatted(scmProvider.getClass()));
    }
}
