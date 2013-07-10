package com.cdmtech.sol.maven.plugin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import com.cdmtech.proj.sol.exporter.icodes.railcar.icodes_railcar.RailcarFilter;

/**
 * Removes rows in the csv file that are considered duplicates. A duplicate is a
 * row whose attributes are the same as another rows attributes except for the
 * first comma separated value (Equipment ID).
 * 
 * @goal filter-invalid-railcars
 * @phase generate-resources
 * @author jundesser
 */
@SuppressWarnings("unchecked")
public class UMLERCSVFilterInvalidRailcarsMojo extends AbstractMojo {
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
   
   public void execute() throws MojoExecutionException {
      try {
         List<String> includedFilePaths = FileProcessingUtils.getIncludedFilePaths(includesDirectory, includes, excludes);

         for (String includedFilePath : includedFilePaths) {
            File csvFile = new File(includesDirectory, includedFilePath);
            File outFile = new File(outputDirectory, csvFile.getName());
            
            File parentDir = outFile.getParentFile();
            if (parentDir != null) {
               parentDir.mkdirs();
            }
            
            outFile.createNewFile();
            
            RailcarFilter.filterCSVFile(csvFile, outFile);
         }
      } catch (Exception e) {
         throw new MojoExecutionException("Failed processing include.", e);
      }
   }

   
   
}
