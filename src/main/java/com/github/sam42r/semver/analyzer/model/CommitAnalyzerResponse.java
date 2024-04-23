package com.github.sam42r.semver.analyzer.model;

import com.github.sam42r.semver.scm.model.Commit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommitAnalyzerResponse {

    private List<Commit> breaking;
    private List<Commit> features;
    private List<Commit> fixes;
}
