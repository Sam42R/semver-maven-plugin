package io.github.sam42r.semver.util;

import io.github.sam42r.semver.model.Version;
import io.github.sam42r.semver.model.scm.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TagVersionComparatorTest {

    private TagVersionComparator uut;

    @BeforeEach
    void setUp() {
        uut = new TagVersionComparator(Version.TAG_FORMAT_DEFAULT);
    }

    @Test
    void shouldGetMax() {
        var tags = List.of(
                new Tag("v0.10.0", null),
                new Tag("v0.9.0", null),
                new Tag("v0.1.0", null),
                new Tag("v0.0.10", null),
                new Tag("v0.0.9", null),
                new Tag("v0.0.1", null)
        );

        var alphabeticMax = tags.stream().max(Comparator.comparing(Tag::name));
        assertThat(alphabeticMax).contains(new Tag("v0.9.0", null));

        var semanticVersionMax = tags.stream().max(uut);
        assertThat(semanticVersionMax).contains(new Tag("v0.10.0", null));
    }
}
