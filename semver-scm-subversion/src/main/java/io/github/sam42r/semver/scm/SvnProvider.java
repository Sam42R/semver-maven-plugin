package io.github.sam42r.semver.scm;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.provider.svn.svnjava.SvnJavaScmProvider;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * {@link SCMProvider} for <a href="https://subversion.apache.org/">subversion</a>.<br/>
 * Subversion's <a href="https://svnbook.red-bean.com/en/1.5/svn-book.html#svn.tour.importing.layout">Recommended Repository Layout</a>
 * is required (trunk, branches, tags).
 *
 * @author Sam42R
 */
@Slf4j
public class SvnProvider extends AbstractScmProvider {

    public SvnProvider(Path path, String username, String password) {
        super("javasvn", new SvnJavaScmProvider(), path, username, password);
    }

    @Override
    protected Path getFileBase() {
        return getPath().resolve("trunk");
    }

    @Override
    public void addFile(@NonNull Path file) throws SCMException {
        try {
            var statusResult = getScmManager().status(getScmRepository(), new ScmFileSet(getPath().toFile(), getPath().relativize(file).toFile()));
            var scmFileStatus = statusResult.getChangedFiles().stream()
                    .filter(v -> file.endsWith(Path.of(v.getPath())))
                    .map(ScmFile::getStatus)
                    .findAny()
                    .orElseThrow();
            if (ScmFileStatus.UNKNOWN.equals(scmFileStatus)) {
                super.addFile(file);
            }
        } catch (ScmException e) {
            throw new SCMException(e);
        }
    }

    @Override
    protected Optional<String> getRemoteUrl() throws SCMException {
        if (isWorkingCopy()) {
            try {
                var info = SVNClientManager.newInstance().getWCClient().doInfo(getPath().toFile(), SVNRevision.HEAD);
                return Optional.ofNullable(info.getRepositoryRootURL().toDecodedString());
            } catch (SVNException e) {
                throw new SCMException(e);
            }
        } else {
            return Optional.of("file://%s".formatted(getPath().toUri().getPath()));
        }
    }

    @Override
    public @NonNull String push(boolean force) throws SCMException {
        // SVN does NOT support push on demand (no distributed version control system)
        return "changes already pushed to repository";
    }

    private boolean isWorkingCopy() {
        return Files.exists(getPath().resolve(".svn"));
    }
}
