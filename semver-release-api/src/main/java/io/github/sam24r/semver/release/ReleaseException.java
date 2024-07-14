package io.github.sam24r.semver.release;

public class ReleaseException extends Exception {

    public ReleaseException(Throwable cause) {
        super(cause);
    }

    public ReleaseException(String message) {
        super(message);
    }

    public ReleaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
