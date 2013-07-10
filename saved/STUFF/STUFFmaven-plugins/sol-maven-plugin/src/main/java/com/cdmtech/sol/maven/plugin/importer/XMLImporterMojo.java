package com.cdmtech.sol.maven.plugin.importer;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.w3c.dom.Document;

import com.cdmtech.core.client.xml.XMLToBeanImport;
import com.cdmtech.core.util.xml.XMLImportInterface;
import com.cdmtech.sol.maven.plugin.FileProcessingUtils;

/**
 * Imports objects from XML file(s).
 * 
 * @goal import-xml
 * @phase generate-resources
 * @author csleight
 */
public class XMLImporterMojo extends AbstractMojo {
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
    * The path to the model schema file.
    * 
    * @parameter
    */
   protected String schemaPath;
   
   /**
    * Indicates whether the xml parser should support xml namespaces.
    * 
    * @parameter default-value=true expression=true
    */
   protected boolean namespaceAware;
   
   protected XMLImportInterface importer;
   
   public void execute() throws MojoExecutionException {
      boolean error = false;
      
      List<String> includedFilePaths = null;
      try {
         includedFilePaths = FileProcessingUtils.getIncludedFilePaths(includesDirectory, includes, excludes);
      } catch (Exception e) {
         throw new MojoExecutionException("Could not determine included files.", e);
      }
      
      if (includedFilePaths == null || includedFilePaths.size() == 0) {
         getLog().error("No includes specified.");
         error = true;
      }
      
      if (!error) {
         for (String includedFilePath : includedFilePaths) {
            File includeFile = null;
            
            try {
               includeFile = new File(includesDirectory, includedFilePath);
               importFromFile(includeFile);
            } catch (Exception e) {
               e.printStackTrace();
               throw new MojoExecutionException("Failed processing include '"+includeFile.getAbsolutePath()+"'" + (e.getMessage() != null ? ": " + e.getMessage() : ""), e);
            }
         }
         
      }
      else {
         throw new MojoExecutionException("Import Failed");
      }
   }
   
   protected void importFromFile(File file) throws Exception {
      // TODO: Add a configuration to conditionally keep around the same
      //       importer instance for each file processed by a single execution.
      importer = new XMLToBeanImport(schemaPath);
      
      InputStream resourceStream = FileProcessingUtils.getResourceAsStream(getClass(), file.getAbsolutePath());
      Document dom = parse(resourceStream);
      
      importer.importObjects(dom);
   }
   
   private Document parse(InputStream inputStream) throws Exception {
      Document document = null;
      
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(namespaceAware);
      DocumentBuilder builder = factory.newDocumentBuilder();
      
      document = builder.parse(inputStream);

      return document;
   }
}
