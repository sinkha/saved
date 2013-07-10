package com.cdmtech.atlas.maven.plugin;

import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;

public class ExclusionFilter implements ArtifactFilter {
    private List<Exclusion> exclusions;

    public ExclusionFilter(List<Exclusion> exclusions) {
        this.exclusions = exclusions;
    }

    public boolean include(Artifact artifact) {
        if (this.exclusions != null) {
            for (Exclusion exclusion : this.exclusions) {
                if (exclusion.getGroupId().equals(artifact.getGroupId()) &&
                    exclusion.getArtifactId().equals(artifact.getArtifactId())) {
                    return false;
                }
            }
        }
        return true;
    }
}
