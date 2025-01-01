package io.github.sam42r.semver;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Changelog {

    @Builder.Default
    private String rendererName = "Markup";
    @Builder.Default
    private String template = "changelog";
}
