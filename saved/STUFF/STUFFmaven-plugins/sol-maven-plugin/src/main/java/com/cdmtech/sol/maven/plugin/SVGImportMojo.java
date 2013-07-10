package com.cdmtech.sol.maven.plugin;

import java.io.File;
import java.util.List;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import com.cdmtech.proj.sol.importer.marad.marad_1_0_0.svg.sideProfile.SVGSideProfileParser;
import com.cdmtech.proj.sol.importer.marad.marad_1_0_0.svg.topProfile.SVGTopProfileParser;

/**
 * Imports SOM objects from SVG file(s).
 * 
 * @goal import-svg
 * @phase generate-resources
 * @author csleight
 */
public class SVGImportMojo extends AbstractMojo {
   /**
    * A list of source file patterns to include. Can contain ant-style wildcards
    * and double wildcards.
    * 
    * @parameter
    */
	protected String[] includes;
   
   /**
    * A list of source file patterns to exclude. Can contain ant-style wildcards
    * and double wildcards.
    * 
    * @parameter
    */
   	protected String[] excludes;
   
   /**
    * Root directory of the included source files to be used during the
    * transformation.
    * 
    * @parameter expression="${basedir}"
    */
   	protected File includesDirectory;
   
   /**
    * Indicates whether the type of graphics contained in the svg. Valid values are "top" or "side".
    * 
    * @parameter default-value="top" expression="top"
    */
    protected String type;
   
   /**
    * Indicates whether the build should fail if no includes are found.
    * 
    * @parameter default-value="true" expression="true"
    */
   	protected boolean failOnNoIncludes;   
   
   
   	public void execute() throws MojoExecutionException {
		boolean error = false;
		  
		List<String> includedFilePaths = null;
		try {
			includedFilePaths = FileProcessingUtils.getIncludedFilePaths(includesDirectory, includes, excludes);
		} catch (Exception e) {
			throw new MojoExecutionException("Import Failed: Could not determine included files.", e);
		}
		  
		if ((includedFilePaths == null || includedFilePaths.size() == 0) && failOnNoIncludes) {
			throw new MojoExecutionException("Import Failed: No includes specified.");
		}
	  
		for (String includedFilePath : includedFilePaths) {
		    File includeFile = null;
		    
		    try {
		        includeFile = new File(includesDirectory, includedFilePath);
		       
			    if ("side".equalsIgnoreCase(type)) {
			       SVGSideProfileParser parser = new SVGSideProfileParser();
			       parser.processShip(includeFile);
			    } else if ("top".equalsIgnoreCase(type)) {
			       SVGTopProfileParser parser = new SVGTopProfileParser();
			       parser.processShip(includeFile);
			    }
		   	} catch (Exception e) {
			   	e.printStackTrace();
		      	throw new MojoExecutionException("Failed processing include '"+includeFile.getAbsolutePath()
		      			+"'" + (e.getMessage() != null ? ": " + e.getMessage() : ""), e);
		 	}
		}
   	}
}
