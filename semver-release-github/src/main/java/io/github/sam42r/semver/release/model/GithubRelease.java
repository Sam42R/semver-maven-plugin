package io.github.sam42r.semver.release.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

/**
 * Payload for create release request.<br/>
 *
 * @see <a href="https://docs.github.com/de/rest/releases/releases?apiVersion=2022-11-28#create-a-release">
 * Create a release</a>
 */
@Data
@Builder
@NonNull
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GithubRelease {

    /**
     * The name of the tag.
     */
    @JsonProperty("tag_name")
    private String tagName;
    /**
     * Specifies the commitish value that determines where the Git tag is created from. Can be any branch or commit SHA.
     * Unused if the Git tag already exists. Default: the repository's default branch.
     */
    @JsonProperty("target_commitish")
    private String targetCommitish;
    /**
     * The name of the release.
     */
    private String name;
    /**
     * Text describing the contents of the tag.
     */
    private String body;
    /**
     * <code>true</code> to create a draft (unpublished) release, <code>false</code> to create a published one.
     */
    @Builder.Default
    private boolean draft = false;
    /**
     * <code>true</code> to identify the release as a prerelease. <code>false</code> to identify the release as a full release.
     */
    @Builder.Default
    private boolean prerelease = false;
    /**
     * If specified, a discussion of the specified category is created and linked to the release.
     * The value must be a category that already exists in the repository. For more information, see
     * <a href="https://docs.github.com/discussions/managing-discussions-for-your-community/managing-categories-for-discussions-in-your-repository">
     * Managing categories for discussions in your repository</a>.
     */
    @JsonProperty("discussion_category_name")
    private String discussionCategoryName;
    /**
     * Whether to automatically generate the name and body for this release. If <code>name</code> is specified,
     * the specified name will be used; otherwise, a name will be automatically generated. If <code>body</code> is specified,
     * the body will be pre-pended to the automatically generated notes.
     */
    @Builder.Default
    @JsonProperty("generate_release_notes")
    private boolean generateReleaseNotes = false;
    /**
     * Specifies whether this release should be set as the latest release for the repository. Drafts and prereleases
     * cannot be set as latest. Defaults to <code>true</code> for newly published releases. <code>legacy</code> specifies
     * that the latest release should be determined based on the release creation date and higher semantic version.<br/>
     * Kann eine der Folgenden sein: <code>true</code>, <code>false</code>, <code>legacy</code>
     */
    @Builder.Default
    @JsonProperty("make_latest")
    private String makeLatest = "true";

}
