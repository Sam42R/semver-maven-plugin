package io.github.sam42r.semver;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Analyzer {

    @Builder.Default
    private String specificationName = "Conventional";
    private String configurationPath;
}
