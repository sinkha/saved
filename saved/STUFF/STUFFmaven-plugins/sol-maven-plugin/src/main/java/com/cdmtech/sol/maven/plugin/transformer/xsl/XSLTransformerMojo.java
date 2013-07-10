package com.cdmtech.sol.maven.plugin.transformer.xsl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.cdmtech.sol.maven.plugin.FileProcessingUtils;

/**
 * Transforms XML using an XSLT.
 * 
 * @goal apply-xslt
 * @phase generate-resources
 * @author csleight
 */
public class XSLTransformerMojo extends AbstractMojo {
   /**
    * Collection of TransformationSets to transform. (TransformationSet
    * contains includes, excludes, includesDirectory, outputDirectory,
    * outputFilename, stylesheet, outputMethod, indentAmount, useSAX, and
    * batchIncludes.)
    * 
    * @parameter
    */
   protected List<TransformationSet> transformationSets;

   public void execute() throws MojoExecutionException {
      if (transformationSets == null)
         throw new MojoExecutionException("No transformation sets specified.");
      
      for (int i = 0; i < transformationSets.size(); i++) {
         TransformationSet transformationSet = transformationSets.get(i);
         
         try {
            List<String> includedFilePaths = FileProcessingUtils.getIncludedFilePaths(transformationSet.includesDirectory, transformationSet.includes, transformationSet.excludes);
            if (includedFilePaths == null || includedFilePaths.size() == 0) {
               getLog().warn("No includes were found for transformation set "+(i+1)+".");
            }
            else {
               if (transformationSet.batchIncludes) {
                  ArrayList<File> files = new ArrayList<File>();
                  for (String includedFilePath : includedFilePaths) {
                     File f = new File(transformationSet.includesDirectory, includedFilePath);
                     files.add(f);
                  }
                  
                  batchTransform(files, transformationSet);
               }
               else {
                  for (String includedFilePath : includedFilePaths) {
                     File includedFile = null;
                     try {
                        includedFile = new File(transformationSet.includesDirectory, includedFilePath);
                        transform(includedFile, transformationSet, includedFilePaths.size() == 1);
                     } catch (Exception e) {
                        e.printStackTrace();
                        throw new MojoExecutionException("Failed processing include '"+includedFile.getAbsolutePath()+"'" + (e.getMessage() != null ? ": " + e.getMessage() : ""), e);
                     }
                  }
               }
            }
         } catch (Exception e) {
            throw new MojoExecutionException("Failed processing transformation set" + (e.getMessage() != null ? ": " + e.getMessage() : ""), e);
         }
      }
   }
   
   protected void transform(File file, TransformationSet transformationSet, boolean useTransformationSetFilename) throws Exception {
      File outFile;
      if (useTransformationSetFilename && transformationSet.outputFilename != null)
         outFile = new File(transformationSet.outputDirectory, (transformationSet.prependToOutputFilename != null ? transformationSet.prependToOutputFilename : "") + transformationSet.outputFilename + (transformationSet.appendToOutputFilename != null ? transformationSet.appendToOutputFilename : ""));
      else
         outFile = new File(transformationSet.outputDirectory, (transformationSet.prependToOutputFilename != null ? transformationSet.prependToOutputFilename : "") + file.getName() + (transformationSet.appendToOutputFilename != null ? transformationSet.appendToOutputFilename : ""));
      
      File parentDir = outFile.getParentFile();
      if (parentDir != null)
         parentDir.mkdirs();
      
      outFile.createNewFile();
      
      Source source;
      if (transformationSet.useSAX) {
         getLog().debug("Parsing source XML using SAX.");
         String parserClass = "org.apache.xerces.parsers.SAXParser";
         XMLReader reader = XMLReaderFactory.createXMLReader(parserClass);
         source = new SAXSource(reader, new InputSource(file.getAbsolutePath()));
      }
      else {
         getLog().debug("Parsing source XML using DOM.");
         DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
         documentBuilderFactory.setNamespaceAware(true);
         DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
         InputStream src = new BufferedInputStream(new FileInputStream(file));
         Document document = builder.parse(src);
         src.close();
         source = new DOMSource(document);
      }
      
      Result result = new StreamResult(outFile);
      
      if (transformationSet.stylesheet == null)
         throw new MojoExecutionException("No stylesheet specified.");
      
      InputStream stylesheetInputStream = FileProcessingUtils.getResourceAsStream(getClass(), transformationSet.stylesheet);
      
      if (stylesheetInputStream == null)
         throw new MojoExecutionException("Stylesheet: '"+transformationSet.stylesheet+"' could not be found.");
      
      Source stylesheetSource = new StreamSource(stylesheetInputStream);
      
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer(stylesheetSource);
      
      if (transformationSet.properties != null) {
         for (Map.Entry<String, String> entry : transformationSet.properties.entrySet()) {
            transformer.setParameter(entry.getKey(), entry.getValue());
         }
      }
      
      transformer.setOutputProperty(OutputKeys.METHOD, transformationSet.outputMethod);
      transformer.setOutputProperty(OutputKeys.INDENT, transformationSet.indentAmount > 0 ? "yes" : "no");
      
      if (transformationSet.indentAmount > 0)
         transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "" + transformationSet.indentAmount);
      
      getLog().info("Applying xslt '"+transformationSet.stylesheet+"' to '"+file.getAbsolutePath()+"' resulting in '"+outFile.getAbsolutePath()+"'.");
      transformer.transform(source, result);
   }
   
   protected void batchTransform(List<File> files, TransformationSet transformationSet) throws Exception {
      File outFile = null;
      if (transformationSet.outputFilename != null)
         outFile = new File(transformationSet.outputDirectory, (transformationSet.prependToOutputFilename != null ? transformationSet.prependToOutputFilename : "") + transformationSet.outputFilename + (transformationSet.appendToOutputFilename != null ? transformationSet.appendToOutputFilename : ""));
      else
         outFile = new File(transformationSet.outputDirectory, (transformationSet.prependToOutputFilename != null ? transformationSet.prependToOutputFilename : "") + files.get(0).getName() + (transformationSet.appendToOutputFilename != null ? transformationSet.appendToOutputFilename : ""));
      
      File parentDir = outFile.getParentFile();
      if (parentDir != null)
         parentDir.mkdirs();
      
      outFile.createNewFile();
      
      Source source;
      String filepaths = "";
      for (File file : files) {
         filepaths += (filepaths.length() > 0 ? ", " : "") + "'"+file.getAbsolutePath()+"'";
      }
      
      if (transformationSet.useSAX) {
         // TODO: support batching includes using SAX such that all included xml
         //       files are combined into a single temporary file before being
         //       transformed.
         boolean b = true;
         if (b)
            throw new MojoExecutionException("Batching includes using SAX is not currently supported. Try setting XSLTransformationSet.useSAX to false.");
         
         getLog().debug("Batch parsing source XML using SAX.");
         if (transformationSet.tempDirectory != null)
            transformationSet.tempDirectory.mkdirs();
         File tempFile = File.createTempFile("temp", ".xml", transformationSet.tempDirectory);
         getLog().debug("Created temporary file '"+tempFile.getAbsolutePath()+"'.");
         
         TransformerFactory transformerFactory = TransformerFactory.newInstance();
         Transformer transformer = transformerFactory.newTransformer();
         transformer.setOutputProperty(OutputKeys.METHOD, transformationSet.outputMethod);
         transformer.setOutputProperty(OutputKeys.INDENT, transformationSet.indentAmount > 0 ? "yes" : "no");
         
         if (transformationSet.indentAmount > 0)
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "" + transformationSet.indentAmount);
         
         getLog().debug("Combining "+filepaths+" into temporary file '"+tempFile.getAbsolutePath()+"'.");
         for (File file : files) {
            Result result = new StreamResult(tempFile);
            String parserClass = "org.apache.xerces.parsers.SAXParser";
            XMLReader reader = XMLReaderFactory.createXMLReader(parserClass);
            Source tmpSource = new SAXSource(reader, new InputSource(file.getAbsolutePath()));
            transformer.transform(tmpSource, result);
         }
         filepaths = "'"+tempFile.getAbsolutePath()+"'";
         
         String parserClass = "org.apache.xerces.parsers.SAXParser";
         XMLReader reader = XMLReaderFactory.createXMLReader(parserClass);
         source = new SAXSource(reader, new InputSource(tempFile.getAbsolutePath()));
      }
      else {
         getLog().debug("Batch parsing source XML using DOM.");
         DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
         documentBuilderFactory.setNamespaceAware(true);
         DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
         Document document = builder.newDocument();
         Element root = document.createElement("root");
         document.appendChild(root);
   
         getLog().debug("Combining "+filepaths+" into single document (in-memory).");
         for (File file : files) {
            InputStream src = new BufferedInputStream(new FileInputStream(file));
            Document d = builder.parse(src);
            src.close();
            Node dNode = d.getDocumentElement().cloneNode(true);
            document.adoptNode(dNode);
            root.appendChild(dNode);
         }
         
         source = new DOMSource(document);
      }
      
      Result result = new StreamResult(outFile);
      
      if (transformationSet.stylesheet == null)
         throw new MojoExecutionException("No stylesheet specified.");
      
      InputStream stylesheetInputStream = FileProcessingUtils.getResourceAsStream(getClass(), transformationSet.stylesheet);
      
      if (stylesheetInputStream == null)
         throw new MojoExecutionException("Stylesheet: '"+transformationSet.stylesheet+"' could not be found.");
      
      Source stylesheetSource = new StreamSource(stylesheetInputStream);
      
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer(stylesheetSource);
      
      if (transformationSet.properties != null) {
         for (Map.Entry<String, String> entry : transformationSet.properties.entrySet()) {
            transformer.setParameter(entry.getKey(), entry.getValue());
         }
      }
      
      transformer.setOutputProperty(OutputKeys.METHOD, transformationSet.outputMethod);
      transformer.setOutputProperty(OutputKeys.INDENT, transformationSet.indentAmount > 0 ? "yes" : "no");
      
      if (transformationSet.indentAmount > 0)
         transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "" + transformationSet.indentAmount);
      
      if (transformationSet.useSAX)
         getLog().info("Applying xslt '"+transformationSet.stylesheet+"' to "+filepaths+" resulting in '"+outFile.getAbsolutePath()+"'.");
      else
         getLog().info("Applying xslt '"+transformationSet.stylesheet+"' to combined document resulting in '"+outFile.getAbsolutePath()+"'.");
      transformer.transform(source, result);
   }
}
