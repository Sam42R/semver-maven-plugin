package io.github.sam42r.semver.release.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
@NonNull
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GitlabLinks {

    /**
     * The name of the link.
     * Link names must be unique within the release.
     */
    private String name;
    /**
     * The URL of the link.
     * Link URLs must be unique within the release.
     */
    private String url;
    /**
     * Optional path for a
     * <a href="https://docs.gitlab.com/ee/user/project/releases/release_fields.html#permanent-links-to-release-assets">
     * direct asset link</a>.
     */
    @JsonProperty("direct_asset_path")
    private String directAssetPath;
    /**
     * The type of the link:
     * <ul>
     *     <li>other</li>
     *     <li>runbook</li>
     *     <li>image</li>
     *     <li>package</li>
     * </ul>
     * Defaults to <code>other</code>.
     */
    @JsonProperty("link_type")
    private String linkType;
}
