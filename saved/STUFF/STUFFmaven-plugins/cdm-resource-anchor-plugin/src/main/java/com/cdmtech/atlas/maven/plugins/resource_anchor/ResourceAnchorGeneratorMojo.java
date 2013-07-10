package com.cdmtech.atlas.maven.plugins.resource_anchor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;

/**
 * Mojo for generating a resource anchor class file.
 *
 * @goal generate-resource-anchor
 * @phase generate-resources
 */
public class ResourceAnchorGeneratorMojo extends AbstractMojo
{
    private static final String[] DEFAULT_INCLUDES = new String [] { "**" };
    private HashMap<String, String> imageNameSymbolMap;
    
    /**
     * Build root directory for generated resource anchor class.
     * 
     * @parameter expression="${project.build.directory}/resource_anchor"
     * @required
     */
    private File outputDirectory;
    
    /**
     * Package to be used for the anchor class. 
     * 
     * @parameter expression=""
     */
    private String resourceAnchorPackage;    
    
    /**
     * Name used for Resource Anchor class. By default the artifactId 
     * will be reformatted in camel case and "ResourceAnchor" will be
     * appended. For example, a project with artifactID 'my-project'
     * will become 'MyProjectResourceAnchor'.
     * 
     * @parameter expression="${project.artifactId}ResourceAnchor"
     * @required
     */
    private String resourceAnchorName;
    
    /**
     * Root directory of the image files to be used in resource anchor.
     * 
     * @parameter expression="${basedir}/src/main/resources"
     */
    private File imagesDirectory;
    
    /**
     * A list of files to include. Can contain ant-style wildcards and double wildcards.
     * By default, everything in the imagesDirectory is included.
     * <code>**</code>
     *
     * @parameter
     * @since 1.0-alpha-5
     */
    private String[] includes;

    /**
     * A list of files to exclude. Can contain ant-style wildcards and double wildcards.
     *
     * @parameter
     * @since 1.0-alpha-5
     */
    private String[] excludes;
    
	/**
	 * Option to convert the resource class names (taken from the image file names) to lower case.
	 * This setting overrides the convertToUpperCase setting. 
	 * 
	 * @parameter expression="${convertToLowerCase}" default-value="false"
	 */
	private boolean convertToLowerCase;
	
	/**
	 * Option to convert the resource class names (taken from the image file names) to upper case.
	 * 
	 * @parameter expression="${convertToUpperCase}" default-value="false"
	 */
	private boolean convertToUpperCase;
	
	/**
	 * If true, generate extended version of class which contains a mapping of symbols to 
	 * names and extra function to retrieve the resource.
	 * 
	 * @parameter expression="${extendedGeneration}" default-value="false"
	 */
	private boolean extendedGeneration;
	
	/**
	 * If false, suppress log output warning users about the usage
	 * of hyphens in the file names.
	 * 
	 * @parameter expression="${warnAboutHyphens}" default-value="true"
	 */
	private boolean warnAboutHyphens;
	
	/**
	 * If false, suppress log output warning users about the usage
	 * of spaces in the file names.
	 * 
	 * @parameter expression="${warnAboutSpaces}" default-value="true"
	 */
	private boolean warnAboutSpaces;
	
	/**
	 * If true, full paths are used in generating resource anchor variables 
	 * and will allow for same name files that are in different directories.
	 * 
	 * @parameter expression="${useFullPathNames}" default-value="false"
	 */
    private boolean useFullPathNames;
	
    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     * @since 1.0
     */
    private MavenProject project;

    public void execute() throws MojoExecutionException {    
    	imageNameSymbolMap = new HashMap<String, String>();
    	
    	getLog().info("ResourceAnchorName: " + resourceAnchorName);
    	getLog().info("ResourceAnchorName formatted: " + composeResourceAnchorName());
    	getLog().info("imagesDirectory: " + imagesDirectory);
    	getLog().info("ResourceAnchorPackage: " + resourceAnchorPackage);
    	getLog().info("ResourceAnchorFileName: " + composeResourceAnchorName() + ".as");
    	
    	if (useFullPathNames && extendedGeneration) {
    		getLog().warn("Full path name convention must be used for resource lookup.");
    	}
    	
        String fileContent = composeResourceAnchor();

        File outRef = new File(outputDirectory.getAbsolutePath() + "/" +
            		composePath() + composeResourceAnchorName() + ".as");

        if ((outRef.getParentFile() != null) && !outRef.getParentFile().exists()) {
            if(!outRef.getParentFile().mkdirs()) {
            	getLog().info("Error creating outputDirectory.");
            }
        } 

        FileWriter fileWriter = null;

        try {
        	fileWriter = new FileWriter(outRef);
        	fileWriter.write(fileContent);
        } catch (IOException exception) {
            throw new MojoExecutionException("Error creating file " + outRef, exception);
        } finally {
            if (fileWriter != null) {
            	try {
            		fileWriter.close();
            	} catch (IOException exception) {
            		// Do nothing
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
    
	private String composeResourceAnchor() throws MojoExecutionException{
		StringBuffer raContent = new StringBuffer();
		raContent.append("package " + ((resourceAnchorPackage != null) ? resourceAnchorPackage : "") 
			+ " {\n\tpublic class " + composeResourceAnchorName() + " {\n");

		try {
			List<String> imageFiles = getImageFiles();
			
			for (String img : imageFiles) {
				raContent.append(composeEmbed(composeResourcePath(img), composeResourceId(img)));
			}
		} catch (FileNotFoundException exception) {
			getLog().error("Directory for images to embed not found!");
			getLog().error(exception);
		}
		
		if(extendedGeneration && imageNameSymbolMap != null && !imageNameSymbolMap.isEmpty()) {
			raContent.append("\t\tprivate static const resourceMap:Object = {\n");
			
			for (String key : imageNameSymbolMap.keySet()) {
				raContent.append("\t\t\t'" + key + "':" + imageNameSymbolMap.get(key) + ",\n");
			}
			
			raContent.deleteCharAt(raContent.length() - 2);
			raContent.append("\t\t};\n\n\t\tpublic static function getResourceByName" +
					"(name:String):Class {\n\t\t\treturn resourceMap[name];\n\t\t}\n");
		}
		
		raContent.append("\t}\n" + "}\n");	
		
		return raContent.toString();
	}
	
	private String composeResourcePath(String imagePath) {
		return "/" + imagePath.replace('\\', '/');
	}
	
	private String composeResourceId(String imagePath) throws MojoExecutionException {
		String fileId;

		if (useFullPathNames) {
			fileId = imagePath.substring(0, imagePath.lastIndexOf("."));
			fileId = fileId.replaceAll("[\\./\\\\]", "_");
		} else {
			fileId = extractFileName(imagePath);
		}
		
		String resourceName = fileId;
		
    	if (resourceName.contains(" ")) {
    		if (warnAboutSpaces) {
    			getLog().warn("Embedded resource (" + resourceName + 
    				") contains spaces in the name! It is recommended " +
    				"that all spaces be removed from the image file names. " +
    				"By default, spaces will be replaced with underscores '_'.");
    		}
    		resourceName = resourceName.replace(" ", "_");
    	}
    	if (resourceName.contains("-")) {
    		if (warnAboutHyphens) {
    			getLog().warn("Embedded resource (" + resourceName + 
    				") contains hyphens in the name! It is recommended " +
    				"that all hyphens be removed from the image file names. " +
    				"By default, hyphens will be replaced with underscores '_'.");
    		}
    		resourceName = resourceName.replace("-", "_");
    	}
    	
		if(convertToLowerCase) {
			resourceName = resourceName.toLowerCase();
		}
		if(convertToUpperCase) {
			resourceName = resourceName.toUpperCase();
		}
		
		if(extendedGeneration || !useFullPathNames) {
			if(imageNameSymbolMap.containsKey(fileId)) {
				getLog().error("Multiple resources exist with the same file name! " +
						"This will cause problems when compiling the generated class.");
			}
			imageNameSymbolMap.put(fileId, resourceName);
		}
		
		return resourceName;
	}
	
	private String composeEmbed(String imageFile, String resourceName) {
		return "\t\t[Embed(source='" + imageFile + "')]" +
				"\n\t\tpublic static const " + resourceName +":Class;\n\n";
	}    
   
    private String composePath() {
    	if(resourceAnchorPackage != null && !resourceAnchorPackage.equals("")) {
    		return resourceAnchorPackage.replace('.', '/') + "/";
    	}
    	return "";
    } 
    
    /** 
     * Assumes that artifactId is alphanumeric with only '-' or '_'.
     */
    private String composeResourceAnchorName() {
    	String newRA = resourceAnchorName;

    	if (newRA.contains("-")) {
    		for(int idx = newRA.indexOf("-"); idx != -1; idx = newRA.indexOf("-")) {
    			newRA = newRA.substring(0, idx) + newRA.substring(idx + 1, idx + 2).toUpperCase() 
    				+ newRA.substring(idx + 2);
    		}
    	} 
    	if (newRA.contains("_")) {
    		for (int idx = newRA.indexOf("_"); idx != -1; idx = newRA.indexOf("_")) {
    			newRA = newRA.substring(0, idx - 1) + newRA.substring(idx + 1, idx + 2).toUpperCase() 
    				+ newRA.substring(idx + 2);
    		}
    	}
    	
    	return newRA.substring(0, 1).toUpperCase() + newRA.substring(1);
    }
 
	private String extractFileName(String imagePath) {
		String fileName = new File(imagePath).getName();
		
		return fileName.substring(0, fileName.lastIndexOf("."));
	}
	
	private List<String> getImageFiles() throws FileNotFoundException {
        DirectoryScanner scanner = new DirectoryScanner();

        scanner.setBasedir(imagesDirectory);
        if (includes != null && includes.length != 0) {
            scanner.setIncludes(includes);
        } else {
            scanner.setIncludes(DEFAULT_INCLUDES);
        }

        if (excludes != null && excludes.length != 0) {
            scanner.setExcludes(excludes);
        }

        scanner.addDefaultExcludes();
        scanner.scan();

        return Arrays.asList(scanner.getIncludedFiles());
	}
}
