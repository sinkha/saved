package com.cdmtech.atlas.maven.plugins.filemerge;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.IOUtil;

/**
 * Mojo for merging a set of files into a single file.
 * 
 * @goal merge-files
 */
public class FileMergeMojo extends AbstractMojo {
	private static final String[] DEFAULT_INCLUDES = new String[] { "**" };

	/**
	 * File to be used as destination of aggregated content. 
	 * 
	 * @parameter expression="${project.build.directory}/merge/Merge.txt"
	 * @required
	 */
	protected File mergeFile;

	/**
	 * Specify whether to include the contents of a 'header' file at
	 * the beginning of the merged file.
	 * 
	 * @parameter expression="${includeHeader}" default-value="false"
	 */
	protected boolean includeHeader;

	/**
	 * Contents to be included as header in merged file.
	 * 
	 * @parameter expression="src/merge/merge-header.txt"
	 * @required
	 */
	protected File headerFile;

	/**
	 * Specify whether to include the contents of a 'footer' file at
	 * the end of the merged file.
	 * 
	 * @parameter expression="${includeFooter}" default-value="false"
	 */
	protected boolean includeFooter;
	
	/**
	 * Contents to be included as footer in merged file.
	 * 
	 * @parameter expression="src/merge/merge-footer.txt"
	 * @required
	 */
	protected File footerFile;

	/**
	 * Root directory of the files being aggregated.
	 * 
	 * @parameter
	 * @required
	 */
	protected File baseDirectory;

	/**
	 * A list of files to include. Can contain ant-style wildcards and double
	 * wildcards. By default, everything in the imagesDirectory is included.
	 * <code>**</code>
	 * 
	 * @parameter
	 * @since 1.0-alpha-5
	 */
	private String[] includes;

	/**
	 * A list of files to exclude. Can contain ant-style wildcards and double
	 * wildcards.
	 * 
	 * @parameter
	 * @since 1.0-alpha-5
	 */
	private String[] excludes;
	
	

	/**
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 * @since 1.0
	 */
	protected MavenProject project;

	public void execute() throws MojoExecutionException
    {  
        if ( (mergeFile.getParentFile() != null) &&
	             (!mergeFile.getParentFile().exists()) ) {
	            if(!(true == mergeFile.getParentFile().mkdirs()))
	            	getLog().info("Error creating outputDirectory.");
	        } 
        
	
	        FileWriter w = null;
	
	        try
	        {
	            w = new FileWriter( mergeFile );

		    	List<String> filesToMerge = getFilesToMerge();
		    	
		    	StringWriter aggregateFileWriter = new StringWriter();
		    	
		    	StringWriter writer = null;
		    	InputStream in = null;
		    	Reader reader = null;
		    	
		    	if(includeHeader) {		    		
		    		writer = new StringWriter();
		            reader = null;
		            in = new FileInputStream(headerFile);
		            try
		            {
		                // FIXME if it is a properties file, encoding should be ISO-8859-1
		                reader = new InputStreamReader( in ); // platform encoding
		
		                IOUtil.copy( reader, writer );
		            }
		            finally
		            {
		                IOUtil.close( reader );
		            }
		
		            final String content = writer.toString();

		            aggregateFileWriter.write( content );	 
		    	}		    	
		    			    	
		    	for(String s : filesToMerge) {
		    		writer = new StringWriter();
		            reader = null;
		            in = new FileInputStream(baseDirectory.getAbsolutePath() + baseDirectory.separator + s);
		            try
		            {
		                // FIXME if it is a properties file, encoding should be ISO-8859-1
		                reader = new InputStreamReader( in ); // platform encoding
		
		                IOUtil.copy( reader, writer );
		            }
		            finally
		            {
		                IOUtil.close( reader );
		            }
		
		            final String content = writer.toString();
		
		            aggregateFileWriter.write( "\n" );
		            aggregateFileWriter.write( content );	    		
		    	}
		    	
		    	if(includeFooter) {		    		
		    		writer = new StringWriter();
		            reader = null;
		            in = new FileInputStream(footerFile);
		            try
		            {
		                // FIXME if it is a properties file, encoding should be ISO-8859-1
		                reader = new InputStreamReader( in ); // platform encoding
		
		                IOUtil.copy( reader, writer );
		            }
		            finally
		            {
		                IOUtil.close( reader );
		            }
		
		            final String content = writer.toString();
		
		            aggregateFileWriter.write( "\n" );
		            aggregateFileWriter.write( content );	 
		    	}	
		    	
	            w.write( aggregateFileWriter.toString() );
	        }
	        catch ( IOException e )
	        {
	            throw new MojoExecutionException( "Error creating file " + mergeFile, e );
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
    }


	private List<String> getFilesToMerge() throws FileNotFoundException {
		DirectoryScanner scanner = new DirectoryScanner();

		scanner.setBasedir(baseDirectory);
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
