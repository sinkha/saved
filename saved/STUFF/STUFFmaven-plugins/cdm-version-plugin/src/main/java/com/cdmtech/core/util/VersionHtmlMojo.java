package com.cdmtech.core.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Goal which generates an html file with version information.
 *
 * @goal generate-html-version
 * @phase generate-sources
 */
public class VersionHtmlMojo extends AbstractMojo {
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
	 * Html page title string.
	 * 
	 * @parameter expression="${pageTitle}" default-value="Version"
	 */
	protected String pageTitle;
	
	/**
	 * This function uses a generic structure to create
	 * a html version file.
	 * 
	 * @return String the generated java source file.
	 */
	protected String composeVersion() {
    
		String dateTimeStamp = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(now);
		
        return 
			"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n" +
			"<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">\n" +
			"\t<head>\n" +
			"\t\t<title>" + pageTitle + "</title>\n\n" +
			"\t\t<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n\n" +
			"\t\t<link rel=\"stylesheet\" media=\"screen\" href=\"./content/main.css\" />\n" +
			"\t</head>\n" +
			"\t<body>\n" +
			"\t\t <div id=\"header\">\n" +
			"\t\t\t<h1>" + pageTitle +"</h1><br/><br/>\n" +
			"\t\t\tVersion -- " + version + "<br/>\n" +
			"\t\t\tTimestamp -- " + dateTimeStamp + "<br/>\n" +
			"\t\t</div>\n" +
			"\t\t<div id=\"content\">\n" +
			"\t\t\t<br/>\n" +
			"\t\t</div>\n" +
			"\t\t<div id=\"footer\">\n" +
			"\t\t\t<p>All content &copy; 2011 CDM Technologies Inc. Contact ICODES Customer Support at <a href=\"mailto:icodes-support@cdmtech.com\">icodes-support@cdmtech.com</a>.</p>\n" +
			"\t\t</div>\n" +
			"\t</body>\n" +
			"</html>\n";		
	}
	
	protected String versionFileName() {
		return "Version.html";
	}

    public void execute() throws MojoExecutionException {
    	if(!skipVersion) { 
    		now = Calendar.getInstance().getTime();
	        String tpl = composeVersion();
	
	        File output = new File(outputDirectory.getAbsolutePath() + "/" + versionFileName());
	
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
	        
        
    	} else {
    		getLog().debug("skipVersion flag has been set: " +
    				"Version class file has not been generated.");
    	}
    }

}
