package io.github.sam42r.semver.release.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.List;

@Data
@Builder
@NonNull
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GitlabAssets {

    /**
     * An array of assets links.
     */
    private List<GitlabLinks> links;
}
