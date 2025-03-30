package io.github.sam42r.semver.analyzer.model;

/**
 * The semantic versioning change level.
 */
public enum SemVerChangeLevel {
    /**
     * No change.
     */
    NONE,
    /**
     * A patch level change: <code>x.x.?</code>
     */
    PATCH,
    /**
     * A minor level change: <code>x.?.0</code>
     */
    MINOR,
    /**
     * A major level change: <code>?.0.0</code>
     */
    MAJOR;
}
