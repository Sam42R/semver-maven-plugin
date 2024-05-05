package com.github.sam42r.semver.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

class PomHelperTest {

    @Test
    void shouldChangeVersion(@TempDir Path tempDir) throws IOException {
        var pom = tempDir.resolve("pom.xml");
        Files.writeString(
                pom,
                """
                        <project>
                            <groupId>org.junit</groupId>
                            <artifactId>test</artifactId>
                            <version>0.0.1-SNAPSHOT</version>
                            <dependencies>
                                <dependency>
                                    <groupId>org.junit</groupId>
                                    <artifactId>sample</artifactId>
                                    <version>1.2.3</version>
                                </dependency>
                            </dependencies>
                        </project>
                        """,
                StandardOpenOption.CREATE
        );

        PomHelper.changeVersion(pom, "v0.8.15");

        var actual = Files.readString(pom);
        Assertions.assertThat(actual).isEqualToIgnoringWhitespace(
                """
                        <project>
                            <groupId>org.junit</groupId>
                            <artifactId>test</artifactId>
                            <version>v0.8.15</version>
                            <dependencies>
                                <dependency>
                                    <groupId>org.junit</groupId>
                                    <artifactId>sample</artifactId>
                                    <version>1.2.3</version>
                                </dependency>
                            </dependencies>
                        </project>
                        """
        );
    }
}
