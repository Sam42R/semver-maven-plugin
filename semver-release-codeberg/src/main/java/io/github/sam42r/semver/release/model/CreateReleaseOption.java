package io.github.sam42r.semver.release.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * CreateReleaseOption options when creating a release.
 */
@Data
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateReleaseOption {

    private String body;
    private boolean draft;
    @JsonProperty("hide_archive_links")
    private boolean hideArchiveLinks;
    private String name;
    private boolean prerelease;
    @JsonProperty("tag_name")
    private String tagName;
    @JsonProperty("target_commitish")
    private String targetCommitish;
}
