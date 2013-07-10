package com.cdmtech.atlas.maven.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.dependency.utils.DependencyUtil;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.artifact.InvalidDependencyVersionException;
import org.apache.maven.shared.artifact.filter.collection.ArtifactFilterException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

/**
 * Goal that copies a list of artifacts with runtime transitive dependencies
 * from the repository to defined locations.
 * 
 * @goal copy
 */
public class CopyArtifactMojo extends AbstractMojo {

    /**
     * Default location used for mojo unless overridden in ArtifactItem
     * 
     * @optional
     * @parameter expression="${outputDirectory}"
     *            default-value="${project.build.directory}/dependency"
     */
    private File outputDirectory;

    /**
     * Overwrite release artifacts
     * 
     * @optional
     * @parameter expression="${mdep.overWriteReleases}" default-value="false"
     */
    private boolean overWriteReleases;

    /**
     * Overwrite snapshot artifacts
     * 
     * @optional
     * @parameter expression="${mdep.overWriteSnapshots}" default-value="false"
     */
    private boolean overWriteSnapshots;

    /**
     * Overwrite if newer
     * 
     * @optional
     * @parameter expression="${mdep.overIfNewer}" default-value="true"
     */
    private boolean overWriteIfNewer;

    /**
     * Collection of ArtifactItems to work on.
     * <pre>
     * &lt;artifactItems&gt;
     *   &lt;artifactItem&gt;
     *     &lt;groupId&gt;&lt;/groupId&gt;
     *     &lt;artifactId&gt;&lt;/artifactId&gt;
     *     &lt;version&gt;&lt;/version&gt;
     *     &lt;type&gt;&lt;/type&gt;
     *     &lt;classifier&gt;&lt;/classifier&gt;
     *     &lt;outputDirectory&gt;&lt;/outputDirectory&gt;
     *     &lt;destFileName&gt;&lt;/destFileName&gt;
     *     &lt;exclusions&gt;
     *       &lt;exclusion&gt;
     *         &lt;groupId&gt;&lt;/groupId&gt;
     *         &lt;artifactId&gt;&lt;/artifactId&gt;
     *       &lt;/exclusion&gt;
     *     &lt;/exclusions&gt;
     *   &lt;/artifactItem&gt;
     * &lt;/artifactItems&gt;
     * </pre>
     * 
     * @required
     * @parameter
     */
    private ArrayList<ArtifactItem> artifactItems;

    /**
     * Strip artifact version during copy
     * 
     * @optional
     * @parameter expression="${mdep.stripVersion}" default-value="false"
     */
    private boolean stripVersion = false;

    /**
     * @component role="org.apache.maven.project.MavenProjectBuilder"
     * @required
     * @readonly
     */
    private MavenProjectBuilder mavenProjectBuilder;

    /**
     * Used to look up Artifacts in the remote repository.
     *
     * @component
     */
    private org.apache.maven.artifact.factory.ArtifactFactory factory;

    /**
     * Used to look up Artifacts in the remote repository.
     *
     * @component
     */
    private org.apache.maven.artifact.resolver.ArtifactResolver resolver;

    /**
     * Location of the local repository.
     *
     * @parameter expression="${localRepository}"
     * @readonly
     * @required
     */
    private org.apache.maven.artifact.repository.ArtifactRepository local;

    /**
     * List of Remote Repositories used by the resolver
     *
     * @parameter expression="${project.remoteArtifactRepositories}"
     * @readonly
     * @required
     */
    private java.util.List remoteRepos;

    /**
     * POM
     *
     * @parameter expression="${project}"
     * @readonly
     * @required
     */
    private MavenProject project;

    /**
     * Main entry into mojo. This method gets the ArtifactItems and iterates
     * through each one passing it to copyArtifact.
     * 
     * @throws MojoExecutionException with a message if an error occurs.
     */
    public void execute() throws MojoExecutionException {
        List theArtifactItems = getProcessedArtifactItems(this.stripVersion);
        Iterator iter = theArtifactItems.iterator();
        
        while (iter.hasNext()) {
            ArtifactItem artifactItem = (ArtifactItem) iter.next();
            if (artifactItem.isNeedsProcessing()) {
                copyArtifact(artifactItem);
            } else {
                this.getLog().info(artifactItem + " already exists in " + artifactItem.getOutputDirectory());
            }
        }
    }

    /**
     * Resolves the artifact from the repository and copies it to the specified
     * location.
     * 
     * @param artifactItem containing the information about the Artifact to copy.
     * @throws MojoExecutionException with a message if an error occurs.
     */
    protected void copyArtifact(ArtifactItem artifactItem) throws MojoExecutionException {
        Artifact artifact = artifactItem.getArtifact();
        File destFile = new File(artifactItem.getOutputDirectory(), artifactItem.getDestFileName());
        copyFile(artifact.getFile(), destFile);
    }

    /**
     * Does the actual copy of the file and logging.
     *
     * @param artifact represents the file to copy.
     * @param destFile file name of destination file.
     * @throws MojoExecutionException with a message if an error occurs.
     */
    protected void copyFile (File artifact, File destFile) throws MojoExecutionException {
        Log theLog = this.getLog();
        try {
            theLog.info("Copying " + artifact.getName() + " to " + destFile);
            FileUtils.copyFile(artifact, destFile);
        } catch (Exception e) {
            throw new MojoExecutionException("Error copying artifact from " + artifact + " to " + destFile, e);
        }
    }

    protected ArtifactItemFilter getMarkedArtifactFilter(ArtifactItem item) {
        ArtifactItemFilter destinationNameOverrideFilter = new DestFileFilter(this.isOverWriteReleases(), 
        		this.isOverWriteSnapshots(), this.isOverWriteIfNewer(), false, false, false, 
        		this.stripVersion, item.getOutputDirectory());
        return destinationNameOverrideFilter;
    }

    /**
     * @return Returns the stripVersion.
     */
    public boolean isStripVersion() {
        return this.stripVersion;
    }

    /**
     * @param stripVersion The stripVersion to set.
     */
    public void setStripVersion(boolean stripVersion){
        this.stripVersion = stripVersion;
    }

    /**
     * Preprocesses the list of ArtifactItems. This method defaults the
     * outputDirectory if not set and creates the output Directory if it doesn't
     * exist.
     * 
     * @param removeVersion remove the version from the filename.
     * @return A List of preprocessed ArtifactItems
     * @throws MojoExecutionExceptionwith a message if an error occurs.
     * @see ArtifactItem
     */
    protected List getProcessedArtifactItems(boolean removeVersion) throws MojoExecutionException {
        ArrayList<ArtifactItem> processedItems = new ArrayList<ArtifactItem>();

        if (this.artifactItems == null || this.artifactItems.size() < 1) {
            throw new MojoExecutionException("There are no artifactItems configured.");
        }

        Iterator iter = this.artifactItems.iterator();
        while (iter.hasNext()) {
            ArtifactItem artifactItem = (ArtifactItem) iter.next();
            processedItems.addAll(processArtifactItem(new ArrayList<ArtifactItem>(), artifactItem, 
            		artifactItem.getExclusions(), removeVersion));
        }
        return processedItems;
    }

    private ArrayList<ArtifactItem> processArtifactItem(ArrayList<ArtifactItem> processedItems, 
    		ArtifactItem artifactItem, ArrayList<Exclusion> exclusions, boolean removeVersion)
    		throws MojoExecutionException {
        if (processedItems.contains(artifactItem)) {
        	return processedItems;
        }

        this.getLog().info("Configured Artifact: " + artifactItem.toString());

        if (artifactItem.getOutputDirectory() == null) {
            artifactItem.setOutputDirectory(this.outputDirectory);
        }
        artifactItem.getOutputDirectory().mkdirs();

        if (StringUtils.isEmpty(artifactItem.getVersion())) {
            fillMissingArtifactVersion(artifactItem);
        }

        Artifact artifact = this.getArtifact(artifactItem);
        artifactItem.setArtifact(artifact);

        if (StringUtils.isEmpty(artifactItem.getDestFileName())) {
            artifactItem.setDestFileName(DependencyUtil.getFormattedFileName(
            		artifactItem.getArtifact(), removeVersion));
        }

        try {
            artifactItem.setNeedsProcessing(checkIfProcessingNeeded(artifactItem));
        } catch (ArtifactFilterException e){
            throw new MojoExecutionException(e.getMessage(), e);
        }
        
        processedItems.add(artifactItem);
        processDependencies(processedItems, artifactItem, exclusions);
        return processedItems;
    }

    private void processDependencies(ArrayList<ArtifactItem> processedItems, ArtifactItem artifactItem, 
    		ArrayList<Exclusion> exclusions) throws MojoExecutionException {
        Artifact pomArtifact = getFactory().createArtifact(artifactItem.getGroupId(), 
        		artifactItem.getArtifactId(), artifactItem.getVersion(), "", "pom");
        try {
        	MavenProject pomProject = this.mavenProjectBuilder.buildFromRepository(pomArtifact, 
        			this.remoteRepos, this.local);
            Set<Artifact> dependencies = pomProject.createArtifacts(getFactory(), 
            		Artifact.SCOPE_RUNTIME, new ExclusionFilter(exclusions));

            for (Artifact dependency : dependencies) {
                ArtifactItem addDependency = new ArtifactItem(dependency);

                addDependency.setOutputDirectory(artifactItem.getOutputDirectory());
                processArtifactItem(processedItems, addDependency, exclusions, false);
            }
        } catch (ProjectBuildingException pbe) {
            throw new MojoExecutionException(pbe.getMessage(), pbe);
        } catch (InvalidDependencyVersionException idve) {
            throw new MojoExecutionException(idve.getMessage(), idve);
        }
    }

    private boolean checkIfProcessingNeeded(ArtifactItem item)
    	throws MojoExecutionException, ArtifactFilterException
    {
        if (StringUtils.equalsIgnoreCase(item.getOverWrite(), "true")) {
            return true;
        } else {
            return getMarkedArtifactFilter(item).isArtifactIncluded(item);
        }
    }

    /**
     * Resolves the Artifact from the remote repository if nessessary. If no
     * version is specified, it will be retrieved from the dependency list or
     * from the DependencyManagement section of the pom.
     * 
     * @param artifactItem containing information about artifact from plugin 
     * 		  configuration.
     * @return Artifact object representing the specified file.
     * @throws MojoExecutionException with a message if the version can't be 
     *         found in DependencyManagement.
     */
    protected Artifact getArtifact(ArtifactItem artifactItem) throws MojoExecutionException {
        Artifact artifact;
        VersionRange vr;
        
        try {
            vr = VersionRange.createFromVersionSpec(artifactItem.getVersion());
        } catch (InvalidVersionSpecificationException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            vr = VersionRange.createFromVersion(artifactItem.getVersion());
        }

        if (StringUtils.isEmpty(artifactItem.getClassifier())) {
            artifact = getFactory().createDependencyArtifact(artifactItem.getGroupId(),
            		artifactItem.getArtifactId(), vr, artifactItem.getType(), null,
                    Artifact.SCOPE_RUNTIME);
        } else {
            artifact = getFactory().createDependencyArtifact(artifactItem.getGroupId(),
                    artifactItem.getArtifactId(), vr, artifactItem.getType(), 
                    artifactItem.getClassifier(), Artifact.SCOPE_RUNTIME);
        }

        try {
            resolver.resolve(artifact, this.remoteRepos, this.local);
        } catch (ArtifactResolutionException e) {
            throw new MojoExecutionException("Unable to resolve artifact.", e);
        } catch (ArtifactNotFoundException e) {
            throw new MojoExecutionException("Unable to find artifact.", e);
        }

        return artifact;
    }

    /**
     * Tries to find missing version from dependancy list and dependency
     * management. If found, the artifact is updated with the correct version.
     * 
     * It will first look for an exact match on
     * artifactId/groupId/classifier/type and if it doesn't find
     * a match, it will try again looking for artifactId and groupId only.
     * 
     * @param artifact representing configured file.
     * @throws MojoExecutionException
     */
    private void fillMissingArtifactVersion(ArtifactItem artifact) throws MojoExecutionException {
        if (!findDependencyVersion(artifact, project.getDependencies(), false)
            && (project.getDependencyManagement() == null || !findDependencyVersion(artifact, 
            		project.getDependencyManagement().getDependencies(), false))
            && !findDependencyVersion(artifact, project.getDependencies(), true)
            && (project.getDependencyManagement() == null || !findDependencyVersion(artifact,
                    project.getDependencyManagement().getDependencies(), true))) {
            throw new MojoExecutionException( "Unable to find artifact version of " + 
            		artifact.getGroupId() + ":" + artifact.getArtifactId() + 
            		" in either dependency list or in project's dependency management." );
        }
    }

    /**
     * Tries to find missing version from a list of dependencies. If found, the
     * artifact is updated with the correct version.
     * 
     * @param artifact representing configured file.
     * @param list list of dependencies to search.
     * @param looseMatch only look at artifactId and groupId
     * @return the found dependency
     */
    private boolean findDependencyVersion(ArtifactItem artifact, List list, boolean looseMatch) {
        for (int i = 0; i < list.size(); i++) {
            Dependency dependency = (Dependency) list.get(i);
            if (StringUtils.equals(dependency.getArtifactId(), artifact.getArtifactId())
                && StringUtils.equals(dependency.getGroupId(), artifact.getGroupId())
                && (looseMatch || StringUtils.equals(dependency.getClassifier(), artifact.getClassifier()))
                && (looseMatch || StringUtils.equals(dependency.getType(), artifact.getType()))) 
            {
                artifact.setVersion(dependency.getVersion());
                return true;
            }
        }

        return false;
    }

    /**
     * @return Returns the artifactItems.
     */
    public ArrayList<ArtifactItem> getArtifactItems() {
        return this.artifactItems;
    }

    /**
     * @param theArtifactItems The artifactItems to set.
     */
    public void setArtifactItems(ArrayList<ArtifactItem> theArtifactItems) {
        this.artifactItems = theArtifactItems;
    }

    /**
     * @return Returns the outputDirectory.
     */
    public File getOutputDirectory() {
        return this.outputDirectory;
    }

    /**
     * @param theOutputDirectory The outputDirectory to set.
     */
    public void setOutputDirectory(File theOutputDirectory) {
        this.outputDirectory = theOutputDirectory;
    }

    /**
     * @return Returns the overWriteIfNewer.
     */
    public boolean isOverWriteIfNewer() {
        return this.overWriteIfNewer;
    }

    /**
     * @param theOverWriteIfNewer The overWriteIfNewer to set.
     */
    public void setOverWriteIfNewer(boolean theOverWriteIfNewer) {
        this.overWriteIfNewer = theOverWriteIfNewer;
    }

    /**
     * @return Returns the overWriteReleases.
     */
    public boolean isOverWriteReleases() {
        return this.overWriteReleases;
    }

    /**
     * @param theOverWriteReleases The overWriteReleases to set.
     */
    public void setOverWriteReleases(boolean theOverWriteReleases) {
        this.overWriteReleases = theOverWriteReleases;
    }

    /**
     * @return Returns the overWriteSnapshots.
     */
    public boolean isOverWriteSnapshots() {
        return this.overWriteSnapshots;
    }

    /**
     * @param theOverWriteSnapshots The overWriteSnapshots to set.
     */
    public void setOverWriteSnapshots(boolean theOverWriteSnapshots) {
        this.overWriteSnapshots = theOverWriteSnapshots;
    }

    /**
     * @return Returns the factory.
     */
    public org.apache.maven.artifact.factory.ArtifactFactory getFactory () {
        return this.factory;
    }

    /**
     * @param factory The factory to set.
     */
    public void setFactory (org.apache.maven.artifact.factory.ArtifactFactory factory) {
        this.factory = factory;
    }

    /**
     * @return Returns the project.
     */
    public MavenProject getProject () {
        return this.project;
    }

    /**
     * @return Returns the local.
     */
    public org.apache.maven.artifact.repository.ArtifactRepository getLocal () {
        return this.local;
    }

    /**
     * @param local The local to set.
     */
    public void setLocal (org.apache.maven.artifact.repository.ArtifactRepository local) {
        this.local = local;
    }

    /**
     * @return Returns the remoteRepos.
     */
    public java.util.List getRemoteRepos () {
        return this.remoteRepos;
    }

    /**
     * @param remoteRepos The remoteRepos to set.
     */
    public void setRemoteRepos (java.util.List remoteRepos) {
        this.remoteRepos = remoteRepos;
    }

    /**
     * @return Returns the resolver.
     */
    public org.apache.maven.artifact.resolver.ArtifactResolver getResolver () {
        return this.resolver;
    }

    /**
     * @param resolver The resolver to set.
     */
    public void setResolver (org.apache.maven.artifact.resolver.ArtifactResolver resolver) {
        this.resolver = resolver;
    }
}
