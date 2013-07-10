
public class VersionTester {

	public static void main(String[] argv) {
		
		/*
		 * Print out the values when retrieving the version class statically.
		 */
		try {
			com.cdmtech.core.version.AbstractVersion versionFromStatic = com.cdmtech.core.version.AbstractVersion.getVersion(
	                "com.cdmtech.atlas.maven.cdm_version_plugin_ev_java_test.Version");
			System.out.println("Static retrieval of Version: ");
			System.out.println("\tExternal Version: " + versionFromStatic.getExternalVersion());
			System.out.println("\tMajor Version: " + versionFromStatic.getMajorVersion());
			System.out.println("\tMinor Version: " + versionFromStatic.getMinorVersion());
			System.out.println("\tIncremental Version: " + versionFromStatic.getIncrementalVersion());
			System.out.println("\tBuild Number: " + versionFromStatic.getBuildNumber());
			System.out.println("\tSCM Build Number: " + versionFromStatic.getSCMBuildNumber());
			System.out.println("\tCI Build Number: " + versionFromStatic.getCIBuildNumber());
			System.out.println("\tDateTimeStamp: " + versionFromStatic.getDateTimeStamp());
			System.out.println("\tDateTimeStampString: " + versionFromStatic.getDateTimeStampString());
			System.out.println("\tQualifier: " + versionFromStatic.getQualifier());
			System.out.println("\ttoString: " + versionFromStatic.toString());
		} catch(Exception e) {
			System.out.println("Failed to retrieve Version class statically: \n" );
			e.printStackTrace();
		}

		/*
		 * Print out the values when manually instantiating the version class.
		 */
		try {
			com.cdmtech.atlas.maven.cdm_version_plugin_ev_java_test.Version versionDirect = new com.cdmtech.atlas.maven.cdm_version_plugin_ev_java_test.Version();
			System.out.println("\nInstantation of Version: ");
			System.out.println("\tExternal Version: " + versionDirect.getExternalVersion());
			System.out.println("\tMajor Version: " + versionDirect.getMajorVersion());
			System.out.println("\tMinor Version: " + versionDirect.getMinorVersion());
			System.out.println("\tIncremental Version: " + versionDirect.getIncrementalVersion());
			System.out.println("\tBuild Number: " + versionDirect.getBuildNumber());
			System.out.println("\tSCM Build Number: " + versionDirect.getSCMBuildNumber());
			System.out.println("\tCI Build Number: " + versionDirect.getCIBuildNumber());
			System.out.println("\tDateTimeStamp: " + versionDirect.getDateTimeStamp());
			System.out.println("\tDateTimeStampString: " + versionDirect.getDateTimeStampString());
			System.out.println("\tQualifier: " + versionDirect.getQualifier());
			System.out.println("\ttoString: " + versionDirect.toString());
			
		} catch(Exception e) {
			System.out.println("Failed to retrieve Version class: \n" );
			e.printStackTrace();
		}
		
		/*
		 * Print out the method calls used to retrieve the above values.
		 */
		System.out.println("\nJava code: ");
		System.out.println("\tExternal Version: version.getExternalVersion()");
		System.out.println("\tMajor Version: version.getMajorVersion()");
		System.out.println("\tMinor Version: version.getMinorVersion()");
		System.out.println("\tIncremental Version: version.getIncrementalVersion()");
		System.out.println("\tBuild Number: version.getBuildNumber()");
		System.out.println("\tSCM Build Number: version.getSCMBuildNumber()");
		System.out.println("\tCI Build Number: version.getCIBuildNumber()");
		System.out.println("\tDateTimeStamp: version.getDateTimeStamp()");
		System.out.println("\tDateTimeStampString: version.getDateTimeStampString()");
		System.out.println("\tQualifier: version.getQualifier()");
		System.out.println("\ttoString: version.toString()");
	}
}
