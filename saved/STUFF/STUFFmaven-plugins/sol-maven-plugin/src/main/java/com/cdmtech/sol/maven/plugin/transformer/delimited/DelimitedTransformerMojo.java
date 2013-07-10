package com.cdmtech.sol.maven.plugin.transformer.delimited;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.stream.StreamResult;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.xml.sax.InputSource;

import com.cdmtech.proj.sol.utility.delimited.DelimitedToXMLFileConverter;
import com.cdmtech.proj.sol.utility.delimited.DelimitedToXMLFileConverterOld;
import com.cdmtech.sol.maven.plugin.FileProcessingUtils;

/**
 * Transforms a delimited text file into xml using a configuration file.
 * 
 * @goal delimited-to-xml
 * @phase generate-resources
 * @author csleight
 *
 */
public class DelimitedTransformerMojo extends AbstractMojo {
   /**
    * Collection of TransformationSet to transform. (TransformationSet
    * contains sources, sourcesDirectory, configs, outputDirectory,
    * outputFilename and properties)
    * 
    * @parameter
    */
   protected List<TransformationSet> transformationSets;
   
   /**
    * Use the old translation code.
    * 
    * @parameter
    */
   protected boolean useOldTranslationCode;
   
   public void execute() throws MojoExecutionException {
      if (transformationSets == null)
         throw new MojoExecutionException("No transformation sets specified.");
      
      for (int i = 0; i < transformationSets.size(); i++) {
         TransformationSet transformationSet = transformationSets.get(i);
         try {
            processTransformationSet(transformationSet, i);
         } catch(Exception e) {
            throw new MojoExecutionException("Failed processing transformation set " + (i + 1) + (e.getMessage() != null ? ": " + e.getMessage() : ""), e);
         }
      }
   }
   
   protected void processTransformationSet(TransformationSet transformationSet, int transformationSetIndex) throws Exception {
      List<InputStream> sourceStreams = new ArrayList<InputStream>();
      List<InputStream> configStreams = new ArrayList<InputStream>();
      
      try {
         if (transformationSet.sources == null || transformationSet.configs == null || transformationSet.sources.length != transformationSet.configs.length) {
            if (transformationSet.sources == null)
               throw new Exception("No sources were specified.");
            if (transformationSet.configs == null)
               throw new Exception("No configs were specified.");
            if (transformationSet.sources.length != transformationSet.configs.length)
               throw new Exception("The number of sources ("+transformationSet.sources.length+") does not match the number of configs ("+transformationSet.configs.length+") specified.");
         }
         else {
            DelimitedToXMLFileConverter converter = new DelimitedToXMLFileConverter();
            DelimitedToXMLFileConverterOld oldConverter = new DelimitedToXMLFileConverterOld();
            
            List<InputSource> sources = new ArrayList<InputSource>();
            List<String> sourcePaths = new ArrayList<String>();
            for (int i = 0; i < transformationSet.sources.length; i++) {
               File sourceFile = new File(transformationSet.sourcesDirectory, transformationSet.sources[i]);
               if (!sourceFile.exists()) {
                  throw new Exception("The source file at '"+sourceFile.getAbsolutePath()+"' could not be found for transformation set "+(transformationSetIndex+1)+".");
               }
               
               InputStream sourceStream = new BufferedInputStream(new FileInputStream(sourceFile));
               sourceStreams.add(sourceStream);
               
               InputSource source = new InputSource(sourceStream);
               
               FileReader fileReader = new FileReader(sourceFile);
               source.setCharacterStream(fileReader);
               
               sources.add(source);
               sourcePaths.add(sourceFile.getAbsolutePath());
            }
            
            List<InputSource> configs = new ArrayList<InputSource>();
            for (int i = 0; i < transformationSet.sources.length; i++) {
               InputStream configStream = FileProcessingUtils.getResourceAsStream(this.getClass(), transformationSet.configs[i]);
               if (configStream == null) {
                  throw new Exception("The configuration file at '"+transformationSet.configs[i]+"' could not be found for transformation set "+(transformationSetIndex+1)+".");
               }
               else {
                  configStreams.add(configStream);
               }
               
               InputSource config = new InputSource(configStream);
               
               configs.add(config);
            }
            
            File resultFile = new File(transformationSet.outputDirectory, transformationSet.outputFilename);
            
            createFilePath(resultFile);
            StreamResult result = new StreamResult(resultFile);
            
            if (useOldTranslationCode)
               oldConverter.translate(sources.toArray(new InputSource[sources.size()]), configs.toArray(new InputSource[configs.size()]), result, transformationSet.rootElementName);
            else
               converter.translate(sources.toArray(new InputSource[sources.size()]), configs.toArray(new InputSource[configs.size()]), sourcePaths.toArray(new String[sourcePaths.size()]), result, transformationSet.rootElementName, transformationSet.properties);
         }
      } catch (Exception e) {
         throw new MojoExecutionException("Failed processing transformation set" + (e.getMessage() != null ? ": " + e.getMessage() : ""), e);
      } finally {
         for (InputStream sourceStream : sourceStreams) {
            sourceStream.close();
         }
         
         for (InputStream configStream : configStreams) {
            configStream.close();
         }
      }
   }
   
   private static void createFilePath(File file) throws IOException {
      if (!file.exists()) {
         File parentDir = file.getParentFile();
         
         if (parentDir != null)
            parentDir.mkdirs();
         
         file.createNewFile();
      }
   }
}
