package com.cdmtech.core.util;

/**
 * Goal which generates a Java version source file
 *
 * @goal generate-java-version
 * @phase generate-sources
 */
public class VersionJavaMojo extends AbstractVersionMojo {
	
	/**
	 * This function uses a generic structure to create
	 * a Java version source file.
	 * 
	 * @return String the generated java source file.
	 */
	protected String composeVersion() {

        String pkg = composePackage();
    
        return "package "+ pkg +";\n\n" +
            "import java.util.Date;\n\n" +
            "/**\n" +
            " * Version class for "+ pkg +"\n" +
            " */\n" +
            "public class Version extends com.cdmtech.core.version.AbstractVersion {\n" +
            "    public Version() throws InstantiationException {\n" +
            "        super(\"" + version + "\",\n" +
            "              new Date( " + now.getTime() +"L ), \"" + scmBuildId + "\", \"" + ciBuildId + "\", "+ hasExternalVersion + ");\n" +
            "    }\n" +
            "}\n";
        /*
    	"\n" +
    	"    static {\n" +
    	"        try {\n" +
    	"            Version.getVersion(\""+ pkg +".Version\");\n" +
    	"        } catch (IllegalAccessException iae) {\n" +
    	"           //iae.printStackTrace();\n" +
    	"        } catch (InstantiationException ie) {\n" +
    	"           //ie.printStackTrace();\n" +
    	"        } catch (ClassNotFoundException cnfe) {\n" +
    	"           //cnfe.printStackTrace();\n" +
    	"        }\n" +    	
    	"    }\n" +
        */
	}
	
	protected String versionFileName() {
		return "Version.java";
	}
}
