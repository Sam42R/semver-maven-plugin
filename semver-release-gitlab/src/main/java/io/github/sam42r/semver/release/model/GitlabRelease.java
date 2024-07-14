package io.github.sam42r.semver.release.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Payload for create release request.<br/>
 *
 * @see <a href="https://docs.gitlab.com/ee/api/releases/#create-a-release">
 * Create a release</a>
 */
@Data
@Builder
@NonNull
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GitlabRelease {

    /**
     * The ID or <a href="https://docs.gitlab.com/ee/api/rest/index.html#namespaced-path-encoding">
     * URL-encoded path of the project</a>.
     */
    private String id;
    /**
     * The release name.
     */
    private String name;
    /**
     * The tag where the release is created from.
     */
    @JsonProperty("tag_name")
    private String tagName;
    /**
     * Message to use if creating a new annotated tag.
     */
    @JsonProperty("tag_message")
    private String tagMessage;
    /**
     * The description of the release. You can use <a href="https://docs.gitlab.com/ee/user/markdown.html">
     * Markdown</a>.
     */
    private String description;
    /**
     * If a tag specified in <code>tag_name</code> doesn’t exist, the release is created from <code>ref</code>
     * and tagged with <code>tag_name</code>.
     * It can be a commit SHA, another tag name, or a branch name.
     */
    private String ref;
    /**
     * The title of each milestone the release is associated with.
     * <a href="https://about.gitlab.com/pricing/">GitLab Premium</a> customers can specify group milestones.
     */
    private List<String> milestones;

    private GitlabAssets assets;
    /**
     * Date and time for the release. Defaults to the current time. Expected in ISO 8601 format
     * (<code>2019-03-15T08:00:00Z</code>).
     * Only provide this field if creating an
     * <a href="https://docs.gitlab.com/ee/user/project/releases/index.html#upcoming-releases">upcoming</a> or
     * <a href="https://docs.gitlab.com/ee/user/project/releases/index.html#historical-releases">historical</a> release.
     */
    @JsonProperty("released_at")
    private LocalDateTime releasedAt;
}
