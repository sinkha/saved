package com.cdmtech.core.xml;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Validates XML against a schema using a SAX parser.
 * 
 * @goal validate
 * @phase validate
 * @author csleight
 */
public class ValidatorMojo extends AbstractMojo {
   /**
    * A list of document file patterns to include for validation.
    * Can contain ant-style wildcards and double wildcards.
    * 
    * @parameter
    */
   protected String[] includes;
   
   /**
    * A list of document file patterns to exclude for validation.
    * Can contain ant-style wildcards and double wildcards.
    * 
    * @parameter
    */
   protected String[] excludes;
   
   /**
    * Root directory of the included document files to be validated.
    * 
    * @parameter expression="${basedir}"
    */
   protected File includesDirectory;
   
   /**
    * Path to schema. 
    * 
    * @parameter
    */
   protected String schema;
   
   private List<SAXParseException> errors = new ArrayList<SAXParseException>();
   private List<SAXParseException> fatalErrors = new ArrayList<SAXParseException>();
   
   public void execute() throws MojoExecutionException {
      List<String> includedFilePaths;
      try {
         includedFilePaths = FileProcessingUtils.getIncludedFilePaths(includesDirectory, includes, excludes);
      } catch (Exception e) {
         throw new MojoExecutionException("Failed validation." + (e.getMessage() != null ? ": " + e.getMessage() : ""), e);
      }
      
      if (includedFilePaths == null || includedFilePaths.size() == 0) {
         throw new MojoExecutionException("No includes were found for validation.");
      }
      else {
         for (String includedFilePath : includedFilePaths) {
            File includedFile = null;
            
            try {
               includedFile = new File(includesDirectory, includedFilePath);
               validate(includedFile);
            } catch (Exception e) {
               e.printStackTrace();
               throw new MojoExecutionException("Failed validation for '"+includedFile.getAbsolutePath()+"'" + (e.getMessage() != null ? ": " + e.getMessage() : ""), e);
            }
         }
      }
   }
   
   protected void validate(File file) throws Exception {
      SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      schemaFactory.setFeature("http://apache.org/xml/features/honour-all-schemaLocations", true);
      schemaFactory.setResourceResolver(new SchemaResourceResolver(schema, getLog()));
      schemaFactory.setErrorHandler(new ErrorHandler());
      
      // load a WXS schema, represented by a Schema instance
      getLog().info("Parsing schema: " + this.schema);
      
      InputStream schemaStream = FileProcessingUtils.getResourceAsStream(getClass(), schema);
      Source schemaSource = new StreamSource(schemaStream);
      Schema schema = schemaFactory.newSchema(schemaSource);
      
      // parse the document
      SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
      saxParserFactory.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);
      saxParserFactory.setSchema(schema);
      saxParserFactory.setNamespaceAware(true);
      SAXParser parser = saxParserFactory.newSAXParser();
      
      getLog().info("Validating " + file.getAbsolutePath() + " against " + this.schema);
      parser.parse(file, new ErrorHandler());
      
      if (errors.size() > 0 || fatalErrors.size() > 0) {
         throw new Exception(errors.size() + fatalErrors.size() + " validation/parser errors" + (fatalErrors.size() > 0 ? "; " + fatalErrors.size() + " fatal." : "."));
      }
   }
   
   private class ErrorHandler extends DefaultHandler {
      public void warning(SAXParseException e) throws SAXException {
         getLog().warn(e);
         getLog().warn("Public ID:     "+e.getPublicId());
         getLog().warn("System ID:     "+e.getSystemId());
         getLog().warn("Line number:   "+e.getLineNumber());
         getLog().warn("Column number: "+e.getColumnNumber());
         getLog().warn("Message:       "+e.getMessage());
      }
      
      public void error(SAXParseException e) throws SAXException {
         errors.add(e);
         getLog().error(e);
         getLog().error("Public ID:     "+e.getPublicId());
         getLog().error("System ID:     "+e.getSystemId());
         getLog().error("Line number:   "+e.getLineNumber());
         getLog().error("Column number: "+e.getColumnNumber());
         getLog().error("Message:       "+e.getMessage());
      }
      
      public void fatalError(SAXParseException e) throws SAXException {
         fatalErrors.add(e);
         getLog().error("Fatal Error", e);
         getLog().error("Public ID:     "+e.getPublicId());
         getLog().error("System ID:     "+e.getSystemId());
         getLog().error("Line number:   "+e.getLineNumber());
         getLog().error("Column number: "+e.getColumnNumber());
         getLog().error("Message:       "+e.getMessage());
      }
   }

}
