package com.cdmtech.sol.maven.plugin.importer;

import java.io.File;
import java.util.List;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import com.cdmtech.proj.sol.importer.marad.marad_1_0_0.dwg.SOMShipDbDatabaseImporter;
import com.cdmtech.proj.sol.utility.dwg.DWGLoader;
import com.cdmtech.sol.maven.plugin.FileProcessingUtils;

/**
 * Loads a MARAD Ship (top-down) DWG CAD file into memory by first converting
 * to XML using an ODA-based (Open Design Alliance) tool that streams the xml
 * to the process' stdout. The streamed XML is then parsed using a StAX parser
 * creating CAD database objects in memory which are in turn processed into
 * SOM objects.
 * 
 * @goal import-marad-ship-dwg
 * @phase generate-resources
 * @author csleight
 */
public class MARADShipDWGToSOMMojo extends AbstractMojo {
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
   
   public void execute() throws MojoExecutionException {
      try {
         List<String> includedFilePaths = FileProcessingUtils.getIncludedFilePaths(includesDirectory, includes, excludes);
         for (String includedFilePath : includedFilePaths) {
            File inFile = new File(includesDirectory, includedFilePath);
            
            File outFile = null;
            if (outputFilename != null) {
               outFile = new File(outputDirectory, (prependToOutputFilename != null ? prependToOutputFilename : "") 
            		   + outputFilename + (appendToOutputFilename != null ? appendToOutputFilename : ""));
            } else {
               outFile = new File(outputDirectory, (prependToOutputFilename != null ? prependToOutputFilename : "") 
            		   + inFile.getName() + (appendToOutputFilename != null ? appendToOutputFilename : ""));
            }
            
            DWGLoader loader = new DWGLoader();
            loader.load(inFile, outFile);
            SOMShipDbDatabaseImporter importer = new SOMShipDbDatabaseImporter();
            importer.importDb(loader.getDatabase());
         }
      } catch (Exception e) {
         throw new MojoExecutionException("Failed processing include(s)" 
        		 + (e.getMessage() != null ? ": " + e.getMessage() : ""), e);
      }
   }
}
