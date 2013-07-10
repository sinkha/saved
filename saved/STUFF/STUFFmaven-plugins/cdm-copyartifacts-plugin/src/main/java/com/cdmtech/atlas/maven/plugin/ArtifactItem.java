package com.cdmtech.atlas.maven.plugin;

import java.io.File;
import java.util.ArrayList;
import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.util.StringUtils;

public class ArtifactItem {
	
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
     * Version of Artifact
     * 
     * @parameter
     */
    private String version = null;

    /**
     * Type of Artifact (War,Jar,etc)
     * 
     * @parameter
     * @required
     */
    private String type = "jar";

    /**
     * Classifier for Artifact (tests,sources,etc)
     * 
     * @parameter
     */
    private String classifier;

    /**
     * Location to use for this Artifact. Overrides default location.
     * 
     * @parameter
     */
    private File outputDirectory;

    /**
     * Provides ability to change destination file name
     * 
     * @parameter
     */
    private String destFileName;

    /**
     * Collection of artifacts to exclude
     * <pre>
     * &lt;exclusions&gt;
     *   &lt;exclusion&gt;
     *     &lt;groupId&gt;&lt;/groupId&gt;
     *     &lt;artifactId&gt;&lt;/artifactId&gt;
     *   &lt;/exclusion&gt;
     * &lt;/exclusions&gt;
     * </pre>
     * 
     * @parameter
     */
    private ArrayList<Exclusion> exclusions;

    /**
     * Force Overwrite..this is the one to set in pom
     */
    private String overWrite;

    /**
     * Force Overwrite
     */
    private boolean needsProcessing;

    /**
     * Artifact Item
     */
    private Artifact artifact;

    public ArtifactItem(Artifact artifact) {
        this.setArtifact(artifact);
        this.setArtifactId(artifact.getArtifactId());
        this.setClassifier(artifact.getClassifier());
        this.setGroupId(artifact.getGroupId());
        this.setType(artifact.getType());
        this.setVersion(artifact.getVersion());
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        } else if (obj instanceof ArtifactItem) {
            ArtifactItem item = (ArtifactItem) obj;

            if (this.toString().equals(item.toString())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    private String filterEmptyString(String in) {
        if (in == null || in.equals("")) {
            return null;
        } else {
            return in;
        }
    }

    /**
     * @return Returns the artifactId.
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * @param artifactId
     *            The artifactId to set.
     */
    public void setArtifactId(String artifactId) {
        this.artifactId = filterEmptyString(artifactId);
    }

    /**
     * @return Returns the groupId.
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * @param groupId
     *            The groupId to set.
     */
    public void setGroupId(String groupId) {
        this.groupId = filterEmptyString(groupId);
    }

    /**
     * @return Returns the type.
     */
    public String getType() {
        return type;
    }

    /**
     * @param type The type to set.
     */
    public void setType(String type) {
        this.type = filterEmptyString(type);
    }

    /**
     * @return Returns the version.
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version The version to set.
     */
    public void setVersion(String version) {
        this.version = filterEmptyString(version);
    }

    /**
     * @return Classifier.
     */
    public String getClassifier() {
        return classifier;
    }

    /**
     * @param classifier Classifier.
     */
    public void setClassifier(String classifier) {
        this.classifier = filterEmptyString(classifier);
    }

    public String toString() {
        if (this.classifier == null) {
            return groupId + ":" + artifactId + ":" + 
            	StringUtils.defaultString(version, "?") + ":" + type;
        } else {
            return groupId + ":" + artifactId + ":" + classifier + ":" + 
            	StringUtils.defaultString(version, "?") + ":" + type;
        }
    }

    /**
     * @return Returns the location.
     */
    public File getOutputDirectory() {
        return outputDirectory;
    }

    /**
     * @param outputDirectory The outputDirectory to set.
     */
    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    /**
     * @return Returns the location.
     */
    public String getDestFileName() {
        return destFileName;
    }

    /**
     * @param destFileName The destFileName to set.
     */
    public void setDestFileName(String destFileName) {
        this.destFileName = filterEmptyString(destFileName);
    }

    /**
     * @return Returns the exclusions.
     */
    public ArrayList<Exclusion> getExclusions() {
        return this.exclusions;
    }

    /**
     * @param theExclusions The exclusions to set.
     */
    public void setExclusions(ArrayList<Exclusion> theExclusions) {
        this.exclusions = theExclusions;
    }

    /**
     * @return Returns the needsProcessing.
     */
    public boolean isNeedsProcessing() {
        return this.needsProcessing;
    }

    /**
     * @param needsProcessing The needsProcessing to set.
     */
    public void setNeedsProcessing(boolean needsProcessing) {
        this.needsProcessing = needsProcessing;
    }

    /**
     * @return Returns the overWrite.
     */
    public String getOverWrite() {
        return this.overWrite;
    }

    /**
     * @param overWrite The overWrite to set.
     */
    public void setOverWrite(String overWrite) {
        this.overWrite = overWrite;
    }

    /**
     * @return Returns the artifact.
     */
    public Artifact getArtifact() {
        return this.artifact;
    }

    /**
     * @param artifact The artifact to set.
     */
    public void setArtifact(Artifact artifact) {
        this.artifact = artifact;
    }
}
