package com.cdmtech.sol.maven.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import com.cdmtech.proj.sol.utility.DateTimeStamp;

import java.lang.IllegalArgumentException;
import java.text.ParseException;

/**
 * Initializes a maven project property for the icodes component version.
 * This version is a combination of the maven project version along with the
 * build date in the following format: x.x.x.YYYYDDD 
 * 
 * @goal initialize-icodes-component-version
 * @phase initialize
 * @author csleight
 */
public class InitializeICODESComponentVersionPropertyMojo extends AbstractMojo {
   /**
    * @parameter expression="${project}"
    * @required
    * @readonly
    * @since 1.0
    */
   protected MavenProject project;
   
   /**
    * The property name to initialize with the icodes component version.
    * 
    * @parameter default-value="icodes.component.version" expression="icodes.component.version"
    */
   protected String propertyName;
   
   /**
    * The Maven property containing the SCM revision value at build time.
    *  
    * @parameter default-value="buildNumber" expression="buildNumber"
    */
   protected String revisionProperty;
   
   /**
    * The Maven property containing the build timestamp value at build time.
    *  
    * @parameter default-value="timestamp" expression="timestamp"
    */
   protected String timestampProperty;
   
   /**
    * Property containing target system schema number.
    *  
    * @parameter default-value="0" expression="0"
    */   
   protected String targetSystemSchemaVersion;
   
   public void execute() throws MojoExecutionException {
	   try {
		  String schemaVersion = formatSchemaVersion(targetSystemSchemaVersion);
		  String revision = project.getProperties().getProperty(revisionProperty);
	      String timestamp = project.getProperties().getProperty(timestampProperty);
	
	      String icodesVersion = "6." + schemaVersion + "." + revision + "." + timestamp;
	      project.getProperties().put(propertyName, icodesVersion);
	   }
	   catch (ParseException e) {
		   throw new MojoExecutionException(e.getMessage());
	   }
    
   }

   
   // original schema version is something like: 6.0.3.19
   // we need to strip the dots so and give each spot 2 digits
   // so the above would be translated to 6000319
   private String formatSchemaVersion(String originalSchemaVersion) throws ParseException {
	   String formattedVersion = null;
	   if (originalSchemaVersion != null && originalSchemaVersion.length() > 0) {
		   String parts[] = originalSchemaVersion.split("\\.");

		   if (parts.length == 4) {
			   
			   formattedVersion = parts[0] + formatPart(parts[1]) + formatPart(parts[2]) + formatPart(parts[3]);
		   }
		   else {
			   throw new ParseException("Schema should have 4 parts: " + originalSchemaVersion, -1);
		   }
	   }
	   else {
		   throw new IllegalArgumentException("Schema Version being passed to method is NULL");
	   }

	   return formattedVersion;
	   
   }
   private String formatPart(String origValue) throws ParseException {
	   String newValue;

	   if (origValue.length() == 1) {
		   newValue = "0" + origValue;
	   }
	   else if (origValue.length() != 2) {
		   throw new ParseException("Expected schema version to have 1 or 2 digits: " + origValue, -1);
	   }
	   else {
		   newValue = origValue;
	   }
	   
	   return newValue;
   }
}
