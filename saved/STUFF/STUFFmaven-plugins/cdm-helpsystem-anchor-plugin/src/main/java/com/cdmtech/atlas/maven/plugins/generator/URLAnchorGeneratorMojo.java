package com.cdmtech.atlas.maven.plugins.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Properties;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * Mojo for generating a url resource anchor class file.
 *
 * @goal generate-url-anchor
 * @phase generate-resources
 */
public class URLAnchorGeneratorMojo extends AbstractMojo {  
    /**
     * Build root directory for generated resource anchor class.
     * 
     * @parameter expression="${project.build.directory}/url_anchor"
     * @required
     */
    private File outputDirectory;
    
    /**
     * Package to be used for the anchor class. 
     * 
     * @parameter expression=""
     */
    private String urlAnchorPackage;    

    /**
     * Name used for URL Anchor class. By default the class name 
     * is "HelpSystemResource" 
     * 
     * @parameter expression="${urlAnchorName}" default-value="HelpSystemResource"
     * @required
     */
    private String urlAnchorName;
        
    /**
     * Name of mapping file. This name will have '.properties' appended
     * to complete the url mapping file name.
     * 
     * @parameter expression="${urlMappingName}"
     * @required
     */
    private String urlMappingName;
    
    /**
     * Directory in which to find mapping file. 
     * 
     * @parameter expression="${urlMappingPath}" default-value="src/main/locales/en_US"
     */
    private File urlMappingPath;

	/**
	 * Specify whether to include a redirect string as part of the
	 * composite URL.
	 * 
	 * @parameter expression="${includeRedirect}" default-value="false"
	 */
	private boolean includeRedirect;
	
    /**
     * String inserted between base URL and relative path to file to facilitate
     * a redirect from the destination page to a page embedded in a frame (or
     * other such use cases).
     * 
     * @parameter expression="${redirectString}" default-value="/hh_goto.htm#"
     */
    private String redirectString; 
	
    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     * @since 1.0
     */
    private MavenProject project;
    
    public void execute() throws MojoExecutionException {      	
    	getLog().info("URLAnchorName: " + urlAnchorName);
    	getLog().info("URLAnchorName formatted: " + composeURLAnchorName());
     	getLog().info("urlAnchorPackage: " + urlAnchorPackage);
    	        
        String tpl = composeURLAnchor();
        
        getLog().info("URLAnchor file name: " + composeURLAnchorFileName());

        File output = new File(outputDirectory.getAbsolutePath() + "/" + 
        		composePath() + composeURLAnchorFileName());

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
                } catch ( IOException e ) {
                    // ignore
                }
            }
        }
        
        if (project.getExecutionProject() != null) {
            project.getExecutionProject().addCompileSourceRoot(outputDirectory.getAbsolutePath());
        }

        project.addCompileSourceRoot(outputDirectory.getAbsolutePath());
    
        getLog().debug("Adding resource anchor source path (" + outputDirectory.getAbsolutePath() +
        		") to project: " + project.getArtifactId());

    }
    
	private String composeURLAnchor() {
		StringBuffer raContent = new StringBuffer();
		String raClassName = composeURLAnchorName();
		
		raContent.append( 
				"package "+ ((urlAnchorPackage!=null)? urlAnchorPackage : "") +"{\n"
				+ "\n"
				+ "\timport flash.events.Event;\n"
				+ "\timport flash.events.EventDispatcher;\n"
				+ "\timport flash.events.IEventDispatcher;\n"
				+ "\n"
				+ "\timport mx.resources.ResourceManager;\n"
				+ "\n"
				+ "\t[ResourceBundle('" + urlMappingName + "')]\n"
				+ "\t[Bindable(\"landerUrlUpdated\")]\n"
				+ "\tpublic class "+ raClassName +" implements IEventDispatcher {\n"
				+ "\n"
				+ "\t\tprivate static var _resource:"+ raClassName +" = new "+ raClassName +"();\n"
				+ "\t\tpublic var _landerUrl:String = \"\";\n\n");

		Hashtable urlMap = getURLs();
		
		if(!urlMap.isEmpty()) {
			for(Object key : urlMap.keySet()){
				raContent.append(composeGetMethod((String)key));
			}
		}
				
		raContent.append( 				
		        "\t\tpublic function get landerUrl():String {\n"
				+ "\t\t\treturn _landerUrl;\n"
				+ "\t\t}\n"
				+ "\n"
		        + "\t\tpublic function set landerUrl(landerUrl:String):void {\n"
				+ "\t\t\t_landerUrl = landerUrl;\n"
				+ "\t\t\tthis.dispatchEvent(new Event(\"landerUrlUpdated\"));\n"
		        + "\t\t}\n\n"
				+ "\t\t// *******************************************\n"
				+ "\t\t// Support Functions\n"
				+ "\t\t// *******************************************\n\n"   
				+ "\t\tprivate function setupDispatacher():void {\n"         
				+ "\t\t\t_ed = new EventDispatcher(this);\n"
				+ "\t\t}\n\n"		        
				+ "\t\tpublic static function get resource():"+ raClassName +" {\n"
				+ "\t\t\tif(_resource == null) {\n"
				+ "\t\t\t\t_resource = new "+ raClassName +"();\n"
				+ "\t\t\t\t_resource.setupDispatacher();\n"
				+ "\t\t}\n"
				+ "\t\t\treturn _resource;\n"
				+ "\t\t}\n\n"		        
				+ "\t\tprivate var _ed:EventDispatcher = new EventDispatcher();\n\n"		        
				+ "\t\t// *******************************************\n"
				+ "\t\t//IEventDispatcher Implementation\n"
				+ "\t\t// *******************************************\n\n"		        
				+ "\t\tpublic function addEventListener(type:String, listener:Function, useCapture:Boolean=false, priority:int=0, useWeakReference:Boolean=false):void {\n"
				+ "\t\t\t_ed.addEventListener(type, listener, useCapture, priority, useWeakReference);\n"
				+ "\t\t}\n\n"		        
				+ "\t\tpublic function removeEventListener(type:String, listener:Function, useCapture:Boolean=false):void {\n"
				+ "\t\t\t_ed.removeEventListener(type, listener, useCapture);\n"
				+ "\t\t}\n\n"		        
				+ "\t\tpublic function dispatchEvent(event:Event):Boolean {\n"
				+ "\t\t\treturn _ed.dispatchEvent(event);\n"
				+ "\t\t}\n\n"		    
				+ "\t\tpublic function hasEventListener(type:String):Boolean {\n"
				+ "\t\t\treturn _ed.hasEventListener(type);\n"
				+ "\t\t}\n\n"		       
				+ "\t\tpublic function willTrigger(type:String):Boolean {\n"
				+ "\t\t\treturn _ed.willTrigger(type);\n"
				+ "\t\t}\n\n"
				+ "\t}\n"
				+ "}\n");
		
		return raContent.toString();
	}
	
	private Hashtable getURLs() {
		Properties mappingProps = new Properties();
		try {
			File urlMappingFile = new File(urlMappingPath.getAbsolutePath() + "/" + urlMappingName + ".properties");
			FileInputStream in = new FileInputStream(urlMappingFile);
			mappingProps.load(in);
			in.close();		
		} catch(Exception e) {
			getLog().error("Mapping file not found!");
			getLog().error(e);
		}
		
		return mappingProps;
	}
	
	
	private String composeGetMethod(String key) {
		if(includeRedirect) {
			return "\t\tpublic function get "+key+"():String {\n" 
					+ "\t\t\treturn _landerUrl + \"" 
					+ redirectString 
					+ "\" + ResourceManager.getInstance().getString('" 
					+ urlMappingName + "', '"
					+ key
					+ "');\n" 
					+ "\t\t}\n\n";
		} 
		
		return "\t\tpublic function get "+key+"():String {\n" 
		+ "\t\t\treturn _landerUrl + ResourceManager.getInstance().getString('" 
		+ urlMappingName + "', '"
		+ key
		+ "');\n" 
		+ "\t\t}\n\n";
	}       
   
    /**
     * Compose the path to be used to output the Resource Anchor class. 
     * Path is created by taking the value of the resourceAnchorPackage
     * and replacing all '.' with '/'.
     * 
     * @return String Output path for Resource Anchor class relative to the outputDirectory
     */
    private String composePath() {
    	if(urlAnchorPackage!=null && !"".equals(urlAnchorPackage))
    		return urlAnchorPackage.replace('.', '/') + "/";
    	return "";
    } 
    
    private String composeURLAnchorFileName() {
    	return composeURLAnchorName() + ".as";
    }
    
    /** 
     * Assumes that artifactId is alphanumeric with only '-' or '_'.
     */
    private String composeURLAnchorName() {
    	String newRA = urlAnchorName;

    	if(newRA.contains("-")) {
    		for(int idx=newRA.indexOf("-"); idx!=-1; idx=newRA.indexOf("-")) {
    			newRA = newRA.substring(0,idx) + newRA.substring(idx+1,idx+2).toUpperCase() 
    			+ newRA.substring(idx+2);
    		}
    	} 
    	if(newRA.contains("_")) {
    		for(int idx=newRA.indexOf("_"); idx!=-1; idx=newRA.indexOf("_")) {
    			newRA = newRA.substring(0,idx-1) + newRA.substring(idx+1,idx+2).toUpperCase() 
    			+ newRA.substring(idx+2);
    		}
    	}
    	
    	return newRA.substring(0,1).toUpperCase() + newRA.substring(1);
    }

}
