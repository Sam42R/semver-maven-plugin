package io.github.sam42r.semver.changelog.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Release {

    private String version;
    private String date;
    private String message;
}
