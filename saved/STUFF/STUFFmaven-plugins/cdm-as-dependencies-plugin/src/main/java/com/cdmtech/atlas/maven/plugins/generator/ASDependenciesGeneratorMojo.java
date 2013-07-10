package com.cdmtech.atlas.maven.plugins.generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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
 * @goal generate-as-dependencies
 * @phase process-sources
 */
public class ASDependenciesGeneratorMojo extends AbstractMojo
{
    private static final String[] DEFAULT_INCLUDES = new String [] { "**" };
    
    /**
     * Build root directory for generated class.
     * 
     * @parameter expression="${project.build.directory}/generated-sources"
     * @required
     */
    protected File outputDirectory;
    
    /**
     * Package to be used for the class. 
     * 
     * @parameter expression="${classPackage}"
     */
    protected String classPackage;    
    

    /**
     * Name used for class (Not including extension). 
     * 
     * @parameter expression="${className}"
     * @required
     */
    protected String className;
    
    /**
     * Root directory files to be included.
     * 
     * @parameter expression="${basedir}/src/main/flex"
     */
    protected File filesDirectory;
    
    /**
     * A list of files to include. Can contain ant-style wildcards and double wildcards.
     * By default, everything in the imagesDirectory is included.
     * <code>**</code>
     *
     * @parameter
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
	 * Sets of source include information. If defined, the fileDirectory, includes, 
	 * and excludes properties will be ignored.
	 * 
	 * Ex --
	 *  <sourceIncludes>
	 *    <sourceInclude>
	 *      <filesDirectory>${basedir}/src/main/flex</filesDirectory>
	 *      <excludes>
	 *        <exclude>*.xml</include>
	 *      </excludes>
	 *    </sourceInclude>
	 *    <sourceInclude>
	 *      <filesDirectory>${project.build.directory}/generated-sources</filesDirectory>
	 *      <includes>
	 *        <include>*.as</include>
	 *        <include>*.mxml</include>
	 *      </includes>
	 *    </sourceInclude>
	 *  </sourceIncludes>
	 *
	 * @parameter
	 */
	private SourceInclude[] sourceIncludes;

	
    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     * @since 1.0
     */
    protected MavenProject project;

    
    public void execute() throws MojoExecutionException
    {    
    	
    	getLog().info("ClassName: " + className);
    	getLog().info("filesDirectory: " + filesDirectory);
    	getLog().info("classPackage: " + classPackage);
    	        
	        String tpl = composeASDepFile();
	        
	        getLog().info("File name: " + className + ".as");
	
	        File output =
	            new File(outputDirectory.getAbsolutePath() + "/" +
	            		composePath() + className + ".as");
	
	        if ( (output.getParentFile() != null) &&
	             (!output.getParentFile().exists()) ) {
	            if(!(true == output.getParentFile().mkdirs()))
	            	getLog().info("Error creating outputDirectory.");
	        } 
	
	        FileWriter w = null;
	
	        try
	        {
	            w = new FileWriter( output );
	
	            w.write( tpl );
	        }
	        catch ( IOException e )
	        {
	            throw new MojoExecutionException( "Error creating file " + output, e );
	        }
	        finally
	        {
	            if ( w != null )
	            {
	                try
	                {
	                    w.close();
	                }
	                catch ( IOException e )
	                {
	                    // ignore
	                }
	            }
	        }
            if ( project.getExecutionProject() != null )
            {
                project.getExecutionProject().addCompileSourceRoot(outputDirectory.getAbsolutePath());
            }

            project.addCompileSourceRoot(outputDirectory.getAbsolutePath());
        
	        getLog().debug("Adding dependencies source path (" + outputDirectory.getAbsolutePath() +
	        		") to project: " + project.getArtifactId());

    }
    
	protected String composeASDepFile() {
		StringBuffer raContent = new StringBuffer();
		raContent.append( 
				"//////////////////////////////////////////////////////////////////////////////////\n"
				+ "// CDM Technologies, Inc. Copyright 2011  All Rights Reserved.\n"
				+ "// 2975 McMillan Ave. Suite 272, San Luis Obispo, CA. 93401\n"
				+ "// "
				+ "// This software is the proprietary information of CDM Technologies, Inc.\n"
				+ "// You shall use it only in accordance with the terms of the license agreement.\n"
				+ "//////////////////////////////////////////////////////////////////////////////////\n"
				+ "package "+ ((classPackage!=null)? classPackage : "") 
				+"\n{\n");

		List<String> files = null;
		
		raContent.append("\timport " + ((classPackage!=null)? classPackage : "") + "." + className + ";\n");
		
		try {
			files = getFileList();
			
			for(String f : files) {				
				raContent.append( composeImport(f));
			}
		} catch (FileNotFoundException e) {
			getLog().error("Directory for images to embed not found!");
			getLog().error(e);
		}
		
		raContent.append("\n\t/**\n");
		raContent.append("\t* Class that imposes static dependencies to all classes in the ICDM project\n");
		raContent.append("\t* @author generator\n");
		raContent.append("\t*/\n");
		raContent.append("\tpublic class " + className + "\n\t{\n\n");
		
		raContent.append("\t\t// Static initialization block for the class\n\t\t{\n");		
		raContent.append("\t\t" + ((classPackage!=null)? classPackage : "") + "." +  className + ";\n");
		if(files!=null) {
			for(String f : files) {				
				raContent.append( composeDeps(f));
			}
		}
		raContent.append("\t\t}\n");
		
		raContent.append("\t}\n" + "}\n");	
		
		return raContent.toString();
	}

	private String composeFileId(String file) {
		String s = file;
		if(file.endsWith(".as"))
			s=file.substring(0, file.length()-3);
		if(file.endsWith(".mxml"))
			s=file.substring(0, file.length()-5);		
			
		return s.replace(File.separatorChar, '.');
	}
	
	private String composeImport(String file) {
		return "\timport " +composeFileId(file) + ";\n";
	}   
	
	private String composeDeps(String file) {
		return "\t\t" +composeFileId(file) + ";\n";
	} 
   
    /*
     * Compose the path to be used to output the Resource Anchor class. 
     * Path is created by taking the value of the resourceAnchorPackage
     * and replacing all '.' with '/'.
     * @return String Output path for Resource Anchor class relative to the outputDirectory
     */
    private String composePath() {
    	if(classPackage!=null && !"".equals(classPackage))
    		return classPackage.replace('.', '/') + "/";
    	return "";
    } 
    

	private List<String> getFileList() throws FileNotFoundException {
		ArrayList<String> filesList = new ArrayList<String>();
		
        if(sourceIncludes!=null && sourceIncludes.length>0) {
        	for(SourceInclude sourceInc : sourceIncludes) {
        		filesList.addAll(
        				scanDirectory(
        						sourceInc.filesDirectory, 
        						sourceInc.includes, 
        						sourceInc.excludes));
        	}
        	return filesList;
        } else {
	        return scanDirectory(filesDirectory, includes, excludes);
        }
	}
	
	private List<String> scanDirectory(File directory, String[] incs, String[] excs) throws FileNotFoundException {
		DirectoryScanner scanner = new DirectoryScanner();
		
        scanner.setBasedir( directory );
        if ( incs != null && incs.length != 0 )
        {
            scanner.setIncludes( incs );
        }
        else
        {
            scanner.setIncludes( DEFAULT_INCLUDES );
        }

        if ( excs != null && excs.length != 0 )
        {
            scanner.setExcludes( excs );
        }

        scanner.addDefaultExcludes();
        scanner.scan();

        return Arrays.asList( scanner.getIncludedFiles() );		
	}

}
