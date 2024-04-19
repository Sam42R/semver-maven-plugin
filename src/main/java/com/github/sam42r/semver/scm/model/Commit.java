package com.github.sam42r.semver.scm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Commit {

    private String id;
    private Instant timestamp;
    private String author;
    private String message;
}
