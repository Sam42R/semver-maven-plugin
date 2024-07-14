package io.github.sam24r.semver.release.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReleaseInfo {

    private String tagName;
    private String name;
    private String description;
    private LocalDateTime time;
}
