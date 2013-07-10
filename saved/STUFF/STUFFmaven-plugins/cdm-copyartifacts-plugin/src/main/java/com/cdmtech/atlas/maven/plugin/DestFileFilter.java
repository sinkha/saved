package com.cdmtech.atlas.maven.plugin;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.dependency.utils.DependencyUtil;
import org.apache.maven.shared.artifact.filter.collection.AbstractArtifactsFilter;
import org.apache.maven.shared.artifact.filter.collection.ArtifactFilterException;
import org.codehaus.plexus.util.StringUtils;

public class DestFileFilter extends AbstractArtifactsFilter implements ArtifactItemFilter {
    boolean overWriteReleases;
    boolean overWriteSnapshots;
    boolean overWriteIfNewer;
    boolean useSubDirectoryPerArtifact;
    boolean useSubDirectoryPerType;
    boolean useRepositoryLayout;
    boolean removeVersion;
    File outputFileDirectory;

    public DestFileFilter(File outputFileDirectory) {
        this.outputFileDirectory = outputFileDirectory;
        overWriteReleases = false;
        overWriteIfNewer = false;
        overWriteSnapshots = false;
        useSubDirectoryPerArtifact = false;
        useSubDirectoryPerType = false;
        removeVersion = false;
    }

    public DestFileFilter(boolean overWriteReleases, boolean overWriteSnapshots, boolean overWriteIfNewer,
                          boolean useSubDirectoryPerArtifact, boolean useSubDirectoryPerType,
                          boolean useRepositoryLayout, boolean removeVersion, File outputFileDirectory) 
    {
        this.overWriteReleases = overWriteReleases;
        this.overWriteSnapshots = overWriteSnapshots;
        this.overWriteIfNewer = overWriteIfNewer;
        this.useSubDirectoryPerArtifact = useSubDirectoryPerArtifact;
        this.useSubDirectoryPerType = useSubDirectoryPerType;
        this.useRepositoryLayout = useRepositoryLayout;
        this.removeVersion = removeVersion;
        this.outputFileDirectory = outputFileDirectory;
    }

    public Set filter(Set artifacts) throws ArtifactFilterException {
        Set result = new HashSet();
        Iterator iter = artifacts.iterator();
        
        while (iter.hasNext()) {
            Artifact artifact = (Artifact) iter.next();
            if (isArtifactIncluded(new ArtifactItem(artifact))) {
                result.add(artifact);
            }
        }
        
        return result;
    }

    /**
     * @return Returns true if the overWriteReleases flag is set.
     */
    public boolean isOverWriteReleases() {
        return this.overWriteReleases;
    }

    /**
     * @param overWriteReleases The overWriteReleases to set.
     */
    public void setOverWriteReleases(boolean overWriteReleases) {
        this.overWriteReleases = overWriteReleases;
    }

    /**
     * @return Returns true if the overWriteSnapshots flag is set.
     */
    public boolean isOverWriteSnapshots() {
        return this.overWriteSnapshots;
    }

    /**
     * @param overWriteSnapshots The overWriteSnapshots to set.
     */
    public void setOverWriteSnapshots(boolean overWriteSnapshots) {
        this.overWriteSnapshots = overWriteSnapshots;
    }

    /**
     * @return Returns true if the overWriteIfNewer flag is set.
     */
    public boolean isOverWriteIfNewer() {
        return this.overWriteIfNewer;
    }

    /**
     * @param overWriteIfNewer The overWriteIfNewer to set.
     */
    public void setOverWriteIfNewer(boolean overWriteIfNewer) {
        this.overWriteIfNewer = overWriteIfNewer;
    }

    /**
     * @return Returns the outputFileDirectory.
     */
    public File getOutputFileDirectory() {
        return this.outputFileDirectory;
    }

    /**
     * @param outputFileDirectory The outputFileDirectory to set.
     */
    public void setOutputFileDirectory(File outputFileDirectory) {
        this.outputFileDirectory = outputFileDirectory;
    }

    /**
     * @return Returns true if the removeVersion flag is set.
     */
    public boolean isRemoveVersion() {
        return this.removeVersion;
    }

    /**
     * @param removeVersion The removeVersion to set.
     */
    public void setRemoveVersion(boolean removeVersion) {
        this.removeVersion = removeVersion;
    }

    /**
     * @return Returns true if the useSubDirectoryPerArtifact flag is set.
     */
    public boolean isUseSubDirectoryPerArtifact() {
        return this.useSubDirectoryPerArtifact;
    }

    /**
     * @param useSubDirectoryPerArtifact The useSubDirectoryPerArtifact to set.
     */
    public void setUseSubDirectoryPerArtifact(boolean useSubDirectoryPerArtifact) {
        this.useSubDirectoryPerArtifact = useSubDirectoryPerArtifact;
    }

    /**
     * @return Returns true if the useSubDirectoryPerType is set.
     */
    public boolean isUseSubDirectoryPerType() {
        return this.useSubDirectoryPerType;
    }

    /**
     * @param useSubDirectoryPerType The useSubDirectoryPerType to set.
     */
    public void setUseSubDirectoryPerType(boolean useSubDirectoryPerType) {
        this.useSubDirectoryPerType = useSubDirectoryPerType;
    }

    /**
     * 
     * @return Returns true if the useRepositoryLayout flag is set.
     */
    public boolean isUseRepositoryLayout() {
        return useRepositoryLayout;
    }

    /**
     * 
     * @param useRepositoryLayout the useRepositoryLayout to set.
     */
    public void setUseRepositoryLayout(boolean useRepositoryLayout) {
        this.useRepositoryLayout = useRepositoryLayout;
    }

    public boolean isArtifactIncluded(ArtifactItem item) {
        boolean overWrite = false;
        Artifact artifact = item.getArtifact();

        if ((artifact.isSnapshot() && this.overWriteSnapshots) 
        	|| (!artifact.isSnapshot() && this.overWriteReleases)) {
            overWrite = true;
        }

        File destFolder = item.getOutputDirectory();
        if (destFolder == null) {
            destFolder = DependencyUtil.getFormattedOutputDirectory(useSubDirectoryPerType, 
            		useSubDirectoryPerArtifact, useRepositoryLayout, removeVersion, 
            		this.outputFileDirectory, artifact);
        }

        File destFile = null;
        if (StringUtils.isEmpty(item.getDestFileName())) {
            destFile = new File(destFolder, DependencyUtil.getFormattedFileName(artifact, this.removeVersion));
        } else {
            destFile = new File(destFolder, item.getDestFileName());
        }

        if (overWrite || (!destFile.exists() || 
           (overWriteIfNewer && artifact.getFile().lastModified() > destFile.lastModified()))) {
            return true;
        }
        
        return false;
    }
}
