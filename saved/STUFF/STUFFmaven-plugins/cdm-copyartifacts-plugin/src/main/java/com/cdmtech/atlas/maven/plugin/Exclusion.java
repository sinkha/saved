package com.cdmtech.atlas.maven.plugin;

public class Exclusion {
	
    /**
     * Group Id of Artifact
     * 
     * @parameter
     * @required
     */
    private String groupId;

    /**
     * Name of Artifact
     * 
     * @parameter
     * @required
     */
    private String artifactId;

    /**
     * @return Returns the artifactId.
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * @param artifactId The artifactId to set.
     */
    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    /**
     * @return Returns the groupId.
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * @param groupId The groupId to set.
     */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String toString() {
        return groupId + ":" + artifactId;
    }
}
