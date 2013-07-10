package com.cdmtech.sol.maven.plugin;

import java.io.File;
import java.util.List;
import java.util.Map;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import com.cdmtech.proj.sol.importer.marad.marad_1_0_0.vgp.VGPParser;

/**
 * Imports SOM objects from VGP file(s).
 * 
 * @goal import-vgp
 * @phase generate-resources
 * @author csleight
 */
public class VGPImportMojo extends AbstractMojo {
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
    * Configures the properties passed to the VGP parser.
    * 
    * @parameter
    */
   protected Map<String, String> properties;
   
   public void execute() throws MojoExecutionException {
      boolean error = false;
      
      List<String> includedFilePaths = null;
      try {
         includedFilePaths = FileProcessingUtils.getIncludedFilePaths(includesDirectory, includes, excludes);
      } catch (Exception e) {
         throw new MojoExecutionException("Import Failed: Could not determine included files.", e);
      }
      
      if (includedFilePaths == null || includedFilePaths.size() == 0) {
         throw new MojoExecutionException("Import Failed: No includes specified.");
      }
      
      for (String includedFilePath : includedFilePaths) {
         File includeFile = null;
            
         try {
            includeFile = new File(includesDirectory, includedFilePath);
            VGPParser parser = properties != null ? new VGPParser(properties) : new VGPParser();
            parser.processShip(includeFile);
         } catch (Exception e) {
            e.printStackTrace();
            throw new MojoExecutionException("Failed processing include '"+includeFile.getAbsolutePath()+"'" 
            		+ (e.getMessage() != null ? ": " + e.getMessage() : ""), e);
         }
      }
   }
}
