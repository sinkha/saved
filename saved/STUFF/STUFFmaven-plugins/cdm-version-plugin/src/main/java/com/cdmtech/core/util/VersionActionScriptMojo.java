package com.cdmtech.core.util;

import java.text.DateFormat;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

/**
 * Goal which generates an ActionScript version source file
 *
 * @goal generate-as-version
 * @phase generate-sources
 */
public class VersionActionScriptMojo extends AbstractVersionMojo {    
	
	/**
	 * This function uses a generic structure to create
	 * an ActionScript version source file.
	 * 
	 * @return String the generated ActionScript source file.
	 */
	protected String composeVersion() {
		String stringVersion = version;
		String externalVersion = "";
		
		if(hasExternalVersion) {
			externalVersion = stringVersion.substring(0, stringVersion.indexOf('.'));
			stringVersion = stringVersion.substring(stringVersion.indexOf('.')+1);
		}
		
    	ArtifactVersion artifactVersion = new DefaultArtifactVersion( stringVersion );
		
        String pkg = composePackage();
        String dateTimeStamp = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(now);

        return "package "+ pkg +"{\n"
		+ "\n "
		+ "public class Version {\n"
		+ "        public static const external:String = \"" + externalVersion+ "\";\n"
		+ "        public static const major:String = \"" + artifactVersion.getMajorVersion()+ "\";\n"
		+ "        public static const minor:String = \"" + artifactVersion.getMinorVersion()+ "\";\n"
		+ "        public static const incremental:String = \"" + artifactVersion.getIncrementalVersion()+ "\";\n"
		+ "        public static const build:String = \"" + artifactVersion.getBuildNumber() + "\";\n"
		+ "        public static const scmBuildNumber:String = \"" + scmBuildId + "\";\n"
		+ "        public static const ciBuildNumber:String = \"" + ciBuildId + "\";\n"
		+ "        public static const qualifier:String = \"" + artifactVersion.getQualifier()+ "\";\n"
		+ "        public static const builddate:String = \"" + dateTimeStamp + "\";\n"
		+ "        public static const versionString:String = \"" + version + "\";\n"
		+ "    }\n" + "}\n";		
	}
	
	protected String versionFileName() {
		return "Version.as";
	}
}
