package io.github.sam42r.semver;

import org.apache.maven.api.plugin.testing.InjectMojo;
import org.apache.maven.api.plugin.testing.MojoTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@MojoTest
class SemanticReleaseMojoIntegrationTest {

    @Test
    @InjectMojo(goal = "semantic-release", pom = "src/test/resources/test-project/pom.xml")
    void shouldRelease(SemanticReleaseMojo uut) {
        assertThat(uut).isNotNull();
        // TODO
    }
}
