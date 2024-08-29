package io.github.sam42r.semver.scm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Remote {

    private String url;
    private String scheme;
    private String host;
    private String group;
    private String project;
}
