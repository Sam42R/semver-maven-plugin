package io.github.sam42r.semver.util;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class BundleHelperTest {

    @Test
    @Disabled("local testing only")
    void shouldCreateBundle(@TempDir Path tempDir) {
        var actual = tempDir.resolve("bundle.zip");

        BundleHelper.main(new String[]{
                        Path.of(".").toAbsolutePath().toString(),
                        "v1.2.2",
                        tempDir.toString()
                }
        );
        assertThat(actual).isNotEmptyFile();
    }
}
