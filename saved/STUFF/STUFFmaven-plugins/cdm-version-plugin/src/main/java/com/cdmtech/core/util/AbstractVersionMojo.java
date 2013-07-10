package com.cdmtech.core.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * Mojo for generating a version class file. This will not be
 * executed if the skipVersion flag is set to true.
 */
abstract class AbstractVersionMojo extends AbstractMojo {
    protected String revision;
    protected Date now;
 
	/**
	 * String ID indicating the SCM version used for this build. In SVN
	 * this would be the SVN revision number. Due to difficulties extracting
	 * this value from the working copy, this value is instead provided in 
	 * the plugin configuration. The value provided in a number of ways: manually, 
	 * using a plugin such as the maven-buildnumber-plugin, or by a continuous 
	 * integration tool such as hudson.
	 * 
	 * @parameter expression="${scmBuildId}" default-value="UNKNOWN"
	 */
	protected String scmBuildId;
	
	/**
	 * String ID indicating the CI build id for this build. This can be a simple
	 * numeric value, job id name, or combination of such values. In the Hudson
	 * CI this is taken from the Hudson environment variables.
	 * 
	 * @parameter expression="${ciBuildId}" default-value="UNKNOWN"
	 */
	protected String ciBuildId;
	
	/**
	 * Option to skip the generation of a version class. Useful when projects are
	 * set up to inherit plugin configuration from parent, but you need to selectively
	 * exclude certain modules from using this plugin.
	 * 
	 * @parameter expression="${skipVersion}" default-value="false"
	 */
	protected boolean skipVersion;

	/**
	 * Indicates that project is using the extended ICODES version
	 * comprised of an external, major, minor, and incremental/patch
	 * version in the format X.X.X.X.
	 * 
	 * @parameter expression="${hasExternalVersion}" default-value="false"
	 * @since 2.1.0
	 */
	protected boolean hasExternalVersion;

    /**
     * Build root directory for generated version class.
     * 
     * @parameter expression="${project.build.directory}/generated-version"
     * @required
     */
    protected File outputDirectory;
    
    /**
     * Package to be used for the version class. By default the groupId and
     * artifactId are concatenated together. Before use, all '-' are replaced
     * with '_'. This property is also used to determine the output path of the 
     * version file using the outputDirectory property as the root.
     * 
     * @parameter expression="${project.groupId}.${project.artifactId}"
     * @required
     */
    protected String versionPackage;    
    
    /**
     * Version of the project.
     * 
     * @parameter expression="${project.version}"
     * @readonly
     * @required
     */
    protected String version;

    /**
     * Artifact ID of the project.
     * 
     * @parameter expression="${project.artifactId}"
     * @readonly
     * @required
     */
    private String artifactId;
    
    /**
     * Group ID of the project.
     * 
     * @parameter expression="${project.groupId}"
     * @readonly
     * @required
     */
    private String groupId;
    
    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     * @since 1.0
     */
    private MavenProject project;

    /** 
     * Compose and return the contents of the generated Version file.
     * 
     * @return String Contents of Version file.
     */
    abstract String composeVersion();
    
    /** 
     * Return the name of the file to be written. Ex- Version.java
     * 
     * @return String Contents of Version file.
     */
    abstract String versionFileName();   

    /**
     * Compose the package to be used for the Version class. Created by
     * replacing all '-' with '_' in the versionPackage value.
     * @return String Package of the Version class
     */
    protected String composePackage() {
        return versionPackage.replaceAll("[-]", "_");
    }
    
    /**
     * Compose the path to be used to output the Version class. Path is 
     * created by taking the value returned by composePackage() and 
     * replacing all '.' with '/'.
     * @return String Output path for Version class relative to the outputDirectory
     */
    private String composePath() {
    	String pkg = composePackage();
		return pkg.replace('.', '/');
    }
    
    public void execute() throws MojoExecutionException {
    	if(!skipVersion) { 
    		now = Calendar.getInstance().getTime();
			String path = composePath();
	        String tpl = composeVersion();
	
	        File output = new File(outputDirectory.getAbsolutePath() + "/" + 
	        		path + "/" + versionFileName());
	
	        if ((output.getParentFile() != null) && (!output.getParentFile().exists())) {
	            if(!output.getParentFile().mkdirs()) {
	            	getLog().info("Error creating outputDirectory.");
	            }
	        } 
	
	        FileWriter w = null;
	
	        try {
	            w = new FileWriter(output);
	            w.write( tpl );
	        } catch (IOException e) {
	            throw new MojoExecutionException("Error creating file " + output, e);
	        } finally {
	            if (w != null) {
	                try {
	                    w.close();
	                } catch (IOException e) {
	                    // ignore
	                }
	            }
	        }
	        
            if (project.getExecutionProject() != null) {
                project.getExecutionProject().addCompileSourceRoot(outputDirectory.getAbsolutePath());
            }

            project.addCompileSourceRoot(outputDirectory.getAbsolutePath());
        
	        getLog().debug("Adding version source path (" + outputDirectory.getAbsolutePath() + 
	        		") to project: " + project.getArtifactId());
    	} else {
    		getLog().debug("skipVersion flag has been set: " +
    				"Version class file has not been generated.");
    	}
    }
}
