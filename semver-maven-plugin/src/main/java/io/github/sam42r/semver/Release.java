package io.github.sam42r.semver;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Release {

    @Builder.Default
    private String publisherName = "GitHub";
    @Builder.Default
    private boolean publish = false;

    private String username;
    private String password;
}
