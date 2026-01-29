package io.github.sam42r.semver.scm.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Commit {

    private String id;
    private Instant timestamp;
    private String author;
    private String message;
}
