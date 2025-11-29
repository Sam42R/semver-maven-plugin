package io.github.sam42r.semver.scm;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

public class StatusScmResultAssert extends AbstractAssert<StatusScmResultAssert, StatusScmResult> {

    protected StatusScmResultAssert(StatusScmResult statusScmResult) {
        super(statusScmResult, StatusScmResultAssert.class);
    }

    public static StatusScmResultAssert assertThat(StatusScmResult statusScmResult) {
        return new StatusScmResultAssert(statusScmResult);
    }

    public StatusScmResultAssert isSuccess() {
        if (!actual.isSuccess()) {
            failWithMessage("Expected result to be successful");
        }
        return this;
    }

    public StatusScmResultAssert hasChangedFiles() {
        if (actual.getChangedFiles().isEmpty()) {
            failWithMessage("Expected changed files to be not empty");
        }
        return this;
    }

    public StatusScmResultAssert hasNoChangedFiles() {
        if (!actual.getChangedFiles().isEmpty()) {
            failWithMessage("Expected changed files to be empty");
        }
        return this;
    }

    public StatusScmResultAssert containsChangedFilesExactlyInAnyOrder(ScmFile ... changedFiles) {
        Assertions.assertThat(actual.getChangedFiles()).containsExactlyInAnyOrder(changedFiles);
        return this;
    }
}
