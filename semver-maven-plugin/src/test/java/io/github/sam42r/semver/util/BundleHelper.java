package io.github.sam42r.semver.util;

import net.lingala.zip4j.ZipFile;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class BundleHelper {

    private static final String[] MODULES = new String[]{
            "semver-analyzer-api",
            "semver-analyzer-conventional",
            "semver-analyzer-gitmoji",
            "semver-changelog-api",
            "semver-changelog-html",
            "semver-changelog-markup",
            "semver-maven-plugin",
            "semver-scm-api",
            "semver-scm-git",
            "semver-scm-mecurial",
            "semver-scm-subversion"
    };

    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("Missing parameter");
            System.exit(1);
        }
        createBundle(Path.of(args[0]), args[1], args[2]);
    }

    private static void createBundle(Path projectBaseDir, String version, String directory) {
        Path tempDirectory = null;
        Path bundle = Path.of(directory).resolve("bundle.zip");
        try (var zipFile = new ZipFile(bundle.toFile())) {
            tempDirectory = Files.createTempDirectory("bundle");
            System.out.printf("Using temp directory %s%n", tempDirectory);

            var pluginDirectory = Files.createDirectories(tempDirectory.resolve("io").resolve("github").resolve("sam42r"));

            var parentDirectory = Files.createDirectories(pluginDirectory.resolve("semver-parent").resolve(version));

            var parentPom = projectBaseDir.resolve("..").resolve("target").resolve("semver-parent-%s.pom".formatted(version));
            addToBundle(parentPom, parentDirectory);

            for (String module : MODULES) {
                var moduleDirectory = Files.createDirectories(pluginDirectory.resolve(module).resolve(version));

                var pom = projectBaseDir.resolve("..").resolve(module).resolve("target").resolve("%s-%s.pom".formatted(module, version));
                var jar = projectBaseDir.resolve("..").resolve(module).resolve("target").resolve("%s-%s.jar".formatted(module, version));
                var sources = projectBaseDir.resolve("..").resolve(module).resolve("target").resolve("%s-%s-sources.jar".formatted(module, version));
                var javadoc = projectBaseDir.resolve("..").resolve(module).resolve("target").resolve("%s-%s-javadoc.jar".formatted(module, version));

                addToBundle(pom, moduleDirectory);
                addToBundle(jar, moduleDirectory);
                addToBundle(sources, moduleDirectory);
                addToBundle(javadoc, moduleDirectory);
            }

            zipFile.addFolder(tempDirectory.resolve("io").toFile());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            if (tempDirectory != null) {
                FileUtils.deleteQuietly(tempDirectory.toFile());
            }
        }
    }

    /**
     * Adds single file to bundle. Each file <i>FILENAME.EXT</i> has to provide:
     * <ul>
     *     <li>FILENAME.EXT<b>.ASC</b></li>
     *     <li>FILENAME.EXT<b>.MD5</b></li>
     *     <li>FILENAME.EXT<b>.SHA1</b></li>
     * </ul>
     *
     * @param path      file to add to bundle
     * @param directory bundle directory
     * @throws IOException exception
     */
    private static void addToBundle(Path path, Path directory) throws IOException {
        var signed = path.getParent().resolve("%s.asc".formatted(path.getFileName()));

        var md5 = path.getParent().resolve("%s.md5".formatted(path.getFileName()));
        Files.writeString(md5, md5(path));

        var sha1 = path.getParent().resolve("%s.sha1".formatted(path.getFileName()));
        Files.writeString(sha1, sha1(path));

        Files.copy(path, directory.resolve(path.getFileName()));
        Files.copy(signed, directory.resolve(signed.getFileName()));
        Files.copy(md5, directory.resolve(md5.getFileName()));
        Files.copy(sha1, directory.resolve(sha1.getFileName()));
    }


    private static String md5(Path path) throws IOException {
        try (var inputStream = Files.newInputStream(path, StandardOpenOption.READ)) {
            return DigestUtils.md5Hex(inputStream);
        }
    }

    private static String sha1(Path path) throws IOException {
        try (var inputStream = Files.newInputStream(path, StandardOpenOption.READ)) {
            return DigestUtils.sha1Hex(inputStream);
        }
    }
}
