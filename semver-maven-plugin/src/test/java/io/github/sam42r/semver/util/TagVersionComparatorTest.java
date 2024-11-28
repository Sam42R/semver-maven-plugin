package io.github.sam42r.semver.util;

import io.github.sam42r.semver.SemanticReleaseMojo;
import io.github.sam42r.semver.scm.model.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TagVersionComparatorTest {

    private TagVersionComparator uut;

    @BeforeEach
    void setUp() {
        uut = new TagVersionComparator(SemanticReleaseMojo.VERSION_NUMBER_PATTERN_DEFAULT);
    }

    @Test
    void shouldGetMax() {
        var tags = List.of(
                Tag.builder().name("v0.10.0").build(),
                Tag.builder().name("v0.9.0").build(),
                Tag.builder().name("v0.1.0").build(),
                Tag.builder().name("v0.0.10").build(),
                Tag.builder().name("v0.0.9").build(),
                Tag.builder().name("v0.0.1").build()
        );

        var alphabeticMax = tags.stream().max(Comparator.comparing(Tag::getName));
        assertThat(alphabeticMax).contains(Tag.builder().name("v0.9.0").build());

        var semanticVersionMax = tags.stream().max(uut);
        assertThat(semanticVersionMax).contains(Tag.builder().name("v0.10.0").build());
    }
}
