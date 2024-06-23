package io.github.sam42r.semver;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Scm {

    @Builder.Default
    private String providerName = "Git";
    @Builder.Default
    private boolean push = true;
}
