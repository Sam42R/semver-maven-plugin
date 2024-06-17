package io.github.sam42r.semver.model;

import lombok.*;

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

    private static final String MAJOR_GROUP = "(?<%s>[0-9]*)".formatted(Type.MAJOR.name());
    private static final String MINOR_GROUP = "(?<%s>[0-9]*)".formatted(Type.MINOR.name());
    private static final String PATCH_GROUP = "(?<%s>[0-9]*)".formatted(Type.PATCH.name());
    private static final String DEFAULT_REGEX = "v%s.%s.%s".formatted(MAJOR_GROUP, MINOR_GROUP, PATCH_GROUP);

    private int major;
    private int minor;
    private int patch;

    private String regex;

    public static Version of(@NonNull String version) {
        return of(version, DEFAULT_REGEX);
    }

    public static Version of(@NonNull String version, @NonNull String regex) {
        if (!regex.contains(MAJOR_GROUP) || !regex.contains(MINOR_GROUP) || !regex.contains(PATCH_GROUP)) {
            throw new IllegalArgumentException(
                    "Regular expression '%s' does not contain required capture groups ('%s', '%s', '%s')".formatted(
                            regex, MAJOR_GROUP, MINOR_GROUP, PATCH_GROUP));
        }

        var pattern = Pattern.compile(regex);
        var matcher = pattern.matcher(version);
        if (matcher.find()) {
            return new Version(
                    Integer.parseInt(matcher.group(Type.MAJOR.name())),
                    Integer.parseInt(matcher.group(Type.MINOR.name())),
                    Integer.parseInt(matcher.group(Type.PATCH.name())),
                    regex);
        }
        throw new IllegalArgumentException("Could not create version for '%s' with regex '%s'".formatted(version, regex));
    }

    public static Version of(@NonNull int major, int minor, int patch) {
        return of(major, minor, patch, DEFAULT_REGEX);
    }

    public static Version of(@NonNull int major, int minor, int patch, String regex) {
        return new Version(major, minor, patch, regex);
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
        var format = regex
                .replace(MAJOR_GROUP, "%d")
                .replace(MINOR_GROUP, "%d")
                .replace(PATCH_GROUP, "%d");
        return format.formatted(major, minor, patch);
    }
}
