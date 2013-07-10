package com.cdmtech.sol.maven.plugin;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import org.apache.batik.apps.rasterizer.DestinationType;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import com.tapestrysolutions.mars.svg.SVGConverter;
import com.tapestrysolutions.mars.svg.SVGConverterException;
import com.tapestrysolutions.mars.svg.SVGUtility;

/**
 * Exports SOM ship object to an ICODES Ship Info XML file.
 * 
 * @goal convert-svg
 * @phase generate-resources
 * @author jundesse
 */
public class SVGConverterMojo extends AbstractMojo {

	public enum IMAGE_TASK {
		UNKNOWN,
		RESIZE_TO_HEIGHT,
		RESIZE_TO_WIDTH,
		RESIZE_TO_RESOLUTION,
		SCALE_DIMENSIONS,
		SCALE_RESOLUTION,
		SCALE_RESOLUTION_WITH_CAP;
	}	

	/**
	 * A list of source file patterns to include. Can contain ant-style
	 * wildcards and double wildcards.
	 * 
	 * @parameter
	 */
	protected String[] includes;

	/**
	 * A list of source file patterns to exclude. Can contain ant-style
	 * wildcards and double wildcards.
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
	 * Indicates whether the build should fail if no includes are found.
	 * 
	 * @parameter default-value="true" expression="${failOnNoIncludes}"
	 */
	protected boolean failOnNoIncludes;

	/**
	 * The file name for the exported xml.
	 * 
	 * @parameter default-value="thumbnail.png" expression="${exportFile}"
	 */
	protected File exportFile;

	/**
	 * Configures which direction to scale to. Width or Height
	 * 
	 * @parameter default-value=UNKNOWN expression="${imageTask}"
	 */
	protected IMAGE_TASK imageTask;

	/**
	 * Resizes the image to a specific height, width, or resolution
	 * 
	 * If IMAGE_TASK == 
	 * 		RESIZE_TO_MAX_HEIGHT,
	 *      RESIZE_TO_MAX_WIDTH,
	 *      RESIZE_TO_MAX_RESOLUTION,
	 *      
	 *      Then this value MUST be set.
	 * 
	 * @parameter default-value=0 expression="${resizeValue}"
	 */
	protected double resizeValue;

	
	/**
	 * Scales the dimensions or resolution of the image
	 * 
	 * If IMAGE_TASK ==
	 * 		SCALE_DIMENSIONS,
	 *      SCALE_RESOLUTION;
	 * 
	 *      Then this value MUST be set
	 * 
	 * 
	 * @parameter default-value=0 expression="${scaleFactor}"
	 */
	protected double scaleFactor;
	
	/**
	 * 
	 * @parameter default-value=0 expression="${maxResolution}"
	 */
	protected double maxResolution;
	
	/**
	 * @parameter expression="${maxWidth}"
	 */
	protected float maxWidth;
	
	/**
	 * @parameter expression="${maxHeight}"
	 */
	protected float maxHeight;
	
	/**
	 * Overrides automatic detection of <code>DestinationType</code> for <code>SVGConverter</code>
	 * <p>
	 * Accepted values
	 * <ul>
	 * 	<li>PNG</li>
	 *  <li>JPEG</li>
	 *  <li>TIFF</li>
	 *  <li>PDF</li>
	 * </ul>
	 * </p>
	 * @parameter expression="${outputType}"
	 */
	protected String outputType;

	public void execute() throws MojoExecutionException {
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
		    
	    	DestinationType destinationType;
	    	
	    	if (outputType != null)
	    	{
	    		outputType = outputType.toLowerCase();
	    		
	    		if (outputType.equals("png")) {
	    			destinationType = DestinationType.PNG;
	    		}
	    		else if(outputType.equals("jpeg")) {
	    			destinationType = DestinationType.JPEG;
	    		}
	    		else if(outputType.equals("tiff")) {
	    			destinationType = DestinationType.TIFF;
	    		}
	    		else if(outputType.equals("pdf")) {
	    			destinationType = DestinationType.PDF;
	    		}
	    		else {
	    			throw new MojoExecutionException("Specified output type is not valid, accepted values are: png, jpeg, tiff, or pdf");
	    		}
	    	}
	    	else {
	    		destinationType = SVGUtility.getDestinationType(exportFile.getName());
	    		
	    		if (destinationType == null) {
	    			throw new MojoExecutionException("Could not determine output type from filename");
	    		}
	    	}
	    		    	
		    try {				
		    	SVGConverter converter;
		    	ByteArrayOutputStream output = null;

		    	includeFile = new File(includesDirectory, includedFilePath);
		    	
		    	try {
					converter = new SVGConverter(new FileInputStream(includeFile), destinationType);
					
					if (maxWidth > 0) {
						converter.setMaxWidth(maxWidth);
					}
					if (maxHeight > 0) {
						converter.setMaxHeight(maxHeight);
					}
				}
				catch(SVGConverterException e) {
					throw new MojoExecutionException("SVG source width or height could not be determined");
				}
		        
		        if (imageTask != IMAGE_TASK.UNKNOWN) {
					if (imageTask.equals(IMAGE_TASK.RESIZE_TO_HEIGHT)) {
						if (resizeValue > 0) {
							output = converter.resizeToHeight(resizeValue);
						}
						else {
							throw new MojoExecutionException("The <resizeValue> required element has not been specified which is needed by the 'RESIZE_TO_HEIGHT' <imageTask> element");
						}
					}
					else if (imageTask.equals(IMAGE_TASK.RESIZE_TO_WIDTH)) {
						if (resizeValue > 0) {
							output = converter.resizeToWidth(resizeValue);
						}
						else {
							throw new MojoExecutionException("The <resizeValue> required element has not been specified which is needed by the 'RESIZE_TO_WIDTH' <imageTask> element");
						}
					}
					else if (imageTask.equals(IMAGE_TASK.RESIZE_TO_RESOLUTION)) {
						if (resizeValue > 0) {
							output = converter.resizeToResolution(resizeValue);
						}
						else {
							throw new MojoExecutionException("The <resizeValue> required element has not been specified which is needed by the 'RESIZE_TO_RESOLUTION' <imageTask> element");
						}
					}
					else if (imageTask.equals(IMAGE_TASK.SCALE_DIMENSIONS)) {
						if (scaleFactor > 0) {
							output = converter.scaleDimensions(scaleFactor);
						}
						else {
							throw new MojoExecutionException("The <scaleFactor> required element has not been specified which is needed by the 'SCALE_DIMENSIONS' <imageTask> element");
						}
					}
					else if (imageTask.equals(IMAGE_TASK.SCALE_RESOLUTION)) {
						if (scaleFactor > 0) {
							output = converter.scaleDimensions(scaleFactor);
						}
						else {
							throw new MojoExecutionException("The <scaleFactor> required element has not been specified which is needed by the 'SCALE_RESOLUTION' <imageTask> element");
						}	
					}
					else if (imageTask.equals(IMAGE_TASK.SCALE_RESOLUTION_WITH_CAP)) {
						if (scaleFactor > 0) {
							if (maxResolution > 0) {
								output = converter.scaleDimensions(scaleFactor);
							}
							else {
								throw new MojoExecutionException("The <maxResolution> required element has not been specified which is needed by the 'SCALE_RESOLUTION_WITH_CAP' <imageTask> element");
							}
						}
						else {
							throw new MojoExecutionException("The <scaleFactor> required element has not been specified which is needed by the 'SCALE_RESOLUTION_WITH_CAP' <imageTask> element");
						}
					}
		        }
				else {
					String msg = "\nThe <imageTask> element property was not specified and is a required paramenter.   Value Options are: \n" +
						"RESIZE_TO_MAX_HEIGHT \n" +
						"RESIZE_TO_MAX_WIDTH \n" +
						"RESIZE_TO_MAX_RESOLUTION \n" +
						"SCALE_DIMENSIONS \n" +
						"SCALE_RESOLUTION \n" +
						"SCALE_RESOLUTION_WITH_CAP \n";
					
					throw new MojoExecutionException(msg);
				}
		        
		        if (output != null) {
		        	FileUtils.writeByteArrayToFile(exportFile, output.toByteArray());
		        }
		        else {
		        	throw new MojoExecutionException("File output is null");
		        }
		        
		   	} catch (Exception e) {
			   	e.printStackTrace();
		      	throw new MojoExecutionException("Failed processing include '"+includeFile.getAbsolutePath()
		      			+"'" + (e.getMessage() != null ? ": " + e.getMessage() : ""), e);
		 	}
		}

   }
}
