package com.cdmtech.sol.maven.plugin.importer;

import java.io.File;
import java.util.List;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import com.cdmtech.proj.sol.importer.attla.dwg.SOMAircraftDbDatabaseImporter;
import com.cdmtech.proj.sol.utility.dwg.DWGLoader;
import com.cdmtech.sol.maven.plugin.FileProcessingUtils;

/**
 * Loads a DWG CAD file into memory by first converting to XML using an
 * ODA-based (Open Design Alliance) tool that streams the xml to the process'
 * stdout. The streamed XML is then parsed using a StAX parser creating CAD
 * database objects in memory which are in turn processed into SOM objects.
 * 
 * @goal attla-dwg-to-som
 * @phase generate-resources
 * @author csleight
 */
public class ATTLADWGToSOMMojo extends AbstractMojo {
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
    * The directory into which transformed files will be output.
    * 
    * @parameter expression="${project.build.directory}"
    */
   protected File outputDirectory;
   
   /**
    * The filename for the resulting transformed output file.
    * 
    * @parameter
    */
   protected String outputFilename;
   
   /**
    * A string to prepend to the explicit or derived output filename.
    * 
    * @parameter
    */
   protected String prependToOutputFilename;
   
   /**
    * A string to append to the explicit or derived output filename.
    * 
    * @parameter
    */
   protected String appendToOutputFilename;
   
   /**
    * Indicates whether this goal should fail if the source file isn't found.
    * 
    * @parameter default-value=false expression=true
    */
   protected boolean failIfNotFound = false;

   /**
    * Absolute path to the dwg2xml executable. This is needed if the 
 	* executable is not in the source path.
    * 
    * @parameter
    */
   protected File dwg2xmlAbsoluteFolderPath = null;
   
   public void execute() throws MojoExecutionException {
      try {
         if(!includesDirectory.exists()){
            return;
         }
         
         List<String> includedFilePaths = FileProcessingUtils.getIncludedFilePaths(includesDirectory, includes, excludes);
         for (String includedFilePath : includedFilePaths) {
            File inFile = new File(includesDirectory, includedFilePath);
            
            File outFile = null;
            if (outputFilename != null) {
               outFile = new File(outputDirectory, (prependToOutputFilename != null ? prependToOutputFilename : "") + outputFilename 
            		   + (appendToOutputFilename != null ? appendToOutputFilename : ""));
            } else {
               outFile = new File(outputDirectory, (prependToOutputFilename != null ? prependToOutputFilename : "") + inFile.getName() 
            		   + (appendToOutputFilename != null ? appendToOutputFilename : ""));
            }
            
            DWGLoader loader = new DWGLoader(dwg2xmlAbsoluteFolderPath);
            loader.load(inFile, outFile);
            SOMAircraftDbDatabaseImporter importer = new SOMAircraftDbDatabaseImporter();
            importer.importDb(loader.getDatabase());
         }
      } catch (Exception e) {
         e.printStackTrace();
         throw new MojoExecutionException("Failed processing include(s)" + (e.getMessage() != null ? ": " + e.getMessage() : ""), e);
      }
   }
}
