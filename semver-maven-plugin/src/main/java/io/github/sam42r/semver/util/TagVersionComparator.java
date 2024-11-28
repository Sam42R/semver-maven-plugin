package io.github.sam42r.semver.util;

import io.github.sam42r.semver.model.Version;
import io.github.sam42r.semver.scm.model.Tag;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;

@RequiredArgsConstructor
public class TagVersionComparator implements Comparator<Tag> {

    private final String versionPattern;

    @Override
    public int compare(Tag o1, Tag o2) {
        return Comparator.comparingInt(Version::getMajor)
                .thenComparingInt(Version::getMinor)
                .thenComparingInt(Version::getPatch)
                .compare(
                        Version.of(o1.getName(), versionPattern),
                        Version.of(o2.getName(), versionPattern)
                );
    }
}
