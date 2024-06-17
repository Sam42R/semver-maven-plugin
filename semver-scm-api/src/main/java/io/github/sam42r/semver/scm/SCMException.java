package io.github.sam42r.semver.scm;

public class SCMException extends Exception {

    public SCMException(Throwable cause) {
        super(cause);
    }

    public SCMException(String message) {
        super(message);
    }

    public SCMException(String message, Throwable cause) {
        super(message, cause);
    }
}
