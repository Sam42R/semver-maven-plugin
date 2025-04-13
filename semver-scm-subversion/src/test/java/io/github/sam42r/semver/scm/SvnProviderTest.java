package io.github.sam42r.semver.scm;

import io.github.sam42r.semver.scm.model.Commit;
import io.github.sam42r.semver.scm.model.Remote;
import io.github.sam42r.semver.scm.model.Tag;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.manager.BasicScmManager;
import org.apache.maven.scm.provider.svn.svnjava.SvnJavaScmProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tmatesoft.svn.core.wc.SVNClientManager;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;

import static io.github.sam42r.semver.scm.StatusScmResultAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

class SvnProviderTest extends AbstractScmProviderTest {

    @BeforeEach
    void setup() throws Exception {
        var repository = tempDirectory.resolve("repository");
        var workingCopy = tempDirectory.resolve("working-copy");
        initializeLocal(repository, workingCopy);

        uut = new SvnProviderFactory().getInstance(workingCopy, null, null);
    }

    @Test
    void shouldReadCommits() throws Exception {
        var actual = uut.readCommits(null);

        assertThat(actual).usingRecursiveFieldByFieldElementComparatorIgnoringFields("timestamp").containsExactly(
                Commit.builder()
                        .id("1")
                        .timestamp(Instant.EPOCH) // ignored
                        .author("r_ric")
                        .message("chore: initial project setup")
                        .build(),
                Commit.builder()
                        .id("2")
                        .timestamp(Instant.EPOCH)
                        .author("r_ric")
                        .message("release: v0.0.1")
                        .build()
        );
    }

    @Test
    void shouldReadTags() throws Exception {
        var actual = uut.readTags();

        assertThat(actual).containsExactly(
                Tag.builder()
                        .name("v0.0.1")
                        .commitId("2")
                        .build()
        );
    }

    @Test
    void shouldAddAndCommitFiles() throws Exception {
        // update file
        var readme = ((AbstractScmProvider) uut).getFileBase().resolve("README.md");
        Files.writeString(readme, "JUnit test", StandardOpenOption.APPEND);
        // create file (e.g. Changelog.md)
        var changelog = ((AbstractScmProvider) uut).getFileBase().resolve("Changelog.md");
        Files.writeString(changelog, "# Changelog", StandardOpenOption.CREATE_NEW);

        uut.addFile(readme);
        uut.addFile(changelog);

        assertThat(status())
                .isSuccess()
                .containsChangedFilesExactlyInAnyOrder(
                        new ScmFile("trunk/Changelog.md", ScmFileStatus.ADDED),
                        new ScmFile("trunk/README.md", ScmFileStatus.MODIFIED)
                );

        var actual = uut.commit("test: commit files to repository");
        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getMessage()).isEqualTo("test: commit files to repository");

        assertThat(status())
                .isSuccess()
                .hasNoChangedFiles();
    }

    @Test
    void shouldTag() throws SCMException {
        var actual = uut.readTags();
        assertThat(actual).hasSize(1);

        uut.tag("v0.0.2");

        actual = uut.readTags();

        assertThat(actual)
                .hasSize(2)
                .contains(Tag.builder()
                        .name("v0.0.2")
                        .commitId("3")
                        .build()
                );
    }

    @Test
    void shouldGetRemote() throws Exception {
        var workingCopy = tempDirectory.resolve("working-copy-remote");
        checkoutWorkingCopyRemote(URI.create("https://svn.code.sf.net/p/keepass/code/trunk"), workingCopy);

        uut = new SvnProviderFactory().getInstance(workingCopy, null, null);

        var actual = uut.getRemote();

        assertThat(actual).isEqualTo(
                Remote.builder()
                        .url("https://svn.code.sf.net/p/keepass/code")
                        .scheme("https")
                        .host("svn.code.sf.net")
                        .group("p/keepass")
                        .project("code")
                        .build()
        );
    }

    private void initializeLocal(Path repository, Path workingCopy) throws Exception {
        initializeRepositoryLocal(repository);
        checkoutWorkingCopyLocal(repository, workingCopy);
    }

    private void initializeRepositoryLocal(Path repository) throws Exception {
        var svnAdminClient = SVNClientManager.newInstance().getAdminClient();
        try (var inputStream = SvnProviderTest.class.getClassLoader().getResourceAsStream("junit.dmp")) {
            svnAdminClient.doCreateRepository(repository.toFile(), null, true, true);
            svnAdminClient.doLoad(repository.toFile(), inputStream);
        }
    }

    private void checkoutWorkingCopyLocal(Path repository, Path workingCopy) throws Exception {
        var scmManager = new BasicScmManager();
        scmManager.setScmProvider("javasvn", new SvnJavaScmProvider());

        var scmRepository = scmManager.makeScmRepository("scm:javasvn:%s".formatted(repository.toUri()));

        var checkOutResult = scmManager.checkOut(scmRepository, new ScmFileSet(workingCopy.toFile()));
        assertThat(checkOutResult.isSuccess()).isTrue();
    }

    private void checkoutWorkingCopyRemote(URI repository, Path workingCopy) throws Exception {
        var scmManager = new BasicScmManager();
        scmManager.setScmProvider("javasvn", new SvnJavaScmProvider());

        var scmRepository = scmManager.makeScmRepository("scm:javasvn:%s".formatted(repository));

        var checkOutResult = scmManager.checkOut(scmRepository, new ScmFileSet(workingCopy.toFile()));
        assertThat(checkOutResult.isSuccess()).isTrue();
    }
}
