package io.github.sam42r.semver.model;

import lombok.*;
import org.apache.commons.text.StringSubstitutor;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Sam42R
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Version {

    public enum Type {
        MAJOR, MINOR, PATCH;
    }

    private static final String PLACEHOLDER_PREFIX = "@";
    private static final String PLACEHOLDER_NAME = "version";
    private static final String PLACEHOLDER_SUFFIX = "@";
    private static final String VERSION_PLACEHOLDER = "%s%s%s".formatted(
            PLACEHOLDER_PREFIX, PLACEHOLDER_NAME, PLACEHOLDER_SUFFIX);

    public static final String TAG_FORMAT_DEFAULT = "v@version@";

    private static final String NAMED_NUMBER_GROUP = "(?<%s>[0-9]*)";
    private static final String MAJOR_GROUP = NAMED_NUMBER_GROUP.formatted(Type.MAJOR.name());
    private static final String MINOR_GROUP = NAMED_NUMBER_GROUP.formatted(Type.MINOR.name());
    private static final String PATCH_GROUP = NAMED_NUMBER_GROUP.formatted(Type.PATCH.name());

    private int major;
    private int minor;
    private int patch;

    private String tagFormat;

    public static Version of(@NonNull String version) {
        return of(version, TAG_FORMAT_DEFAULT);
    }

    public static Version of(@NonNull String version, @NonNull String tagFormat) {
        if (!tagFormat.contains(VERSION_PLACEHOLDER)) {
            throw new IllegalArgumentException(
                    "Given tag format '%s' does not contain required version placeholder '%s'".formatted(
                            tagFormat, VERSION_PLACEHOLDER));
        }

        var regex = StringSubstitutor.replace(
                tagFormat,
                Map.of(PLACEHOLDER_NAME, "%s.%s.%s".formatted(MAJOR_GROUP, MINOR_GROUP, PATCH_GROUP)),
                PLACEHOLDER_PREFIX,
                PLACEHOLDER_SUFFIX);

        var pattern = Pattern.compile(regex);
        var matcher = pattern.matcher(version);
        if (matcher.find()) {
            return new Version(
                    Integer.parseInt(matcher.group(Type.MAJOR.name())),
                    Integer.parseInt(matcher.group(Type.MINOR.name())),
                    Integer.parseInt(matcher.group(Type.PATCH.name())),
                    tagFormat);
        }
        throw new IllegalArgumentException("Could not create version for '%s' with tag format '%s'".formatted(version, tagFormat));
    }

    public static Version of(@NonNull int major, int minor, int patch) {
        return of(major, minor, patch, TAG_FORMAT_DEFAULT);
    }

    public static Version of(@NonNull int major, int minor, int patch, String tagFormat) {
        return new Version(major, minor, patch, tagFormat);
    }

    public void increment(@NonNull Type type) {
        if (Type.MAJOR.equals(type)) {
            this.major++;
            this.minor = 0;
            this.patch = 0;
        } else if (Type.MINOR.equals(type)) {
            this.minor++;
            this.patch = 0;
        } else if (Type.PATCH.equals(type)) {
            this.patch++;
        }
    }

    @Override
    public String toString() {
        return "%d.%d.%d".formatted(major, minor, patch);
    }

    public String toTag() {
        return StringSubstitutor.replace(
                tagFormat, Map.of(PLACEHOLDER_NAME, toString()), PLACEHOLDER_PREFIX, PLACEHOLDER_SUFFIX);
    }
}
