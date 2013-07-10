package com.cdmtech.sol.maven.plugin.exporter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.w3c.dom.Document;
import com.cdmtech.core.client.bean.AbstractView;
import com.cdmtech.core.client.bean.BeanCriteria;
import com.cdmtech.core.client.bean.BeanManager;
import com.cdmtech.core.client.bean.View;
import com.cdmtech.core.client.xml.BeanToXMLExport;
import com.cdmtech.core.util.xml.XMLExportInterface;
import com.cdmtech.proj.sol.utility.xml.BeanToXMLStreamExport;

/**
 * Exports objects to an XML file.
 * 
 * @goal export-objects
 * @phase generate-resources
 * @author csleight
 */
public class XMLExporterMojo extends AbstractMojo {
   private static TransformerFactory transformerFactory;
   
   static {
      transformerFactory = TransformerFactory.newInstance();
   }
   
   /**
    * The view used by the exporter throughout.
    */
   protected static View<?> view;

   static {
      try {
         view = AbstractView.getDefaultView();
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
   
   /**
    * The path to the model schema file.
    * 
    * @parameter
    */
   protected String schemaPath;
   
   /**
    * The file name for the exported xml. Ignored if exportPerResult is true.
    * 
    * @parameter default-value="export.xml" expression="export.xml"
    */
   protected String exportFilename;
   
   /**
    * The file name prefix for the exported xml. Ignored if exportPerResult is false.
    * 
    * @parameter default-value="" expression=""
    */
   protected String exportFilenamePrefix = "";
   
   /**
    * The file name suffix for the exported xml (i.e. "xml"). Ignored if exportPerResult is false.
    * 
    * @parameter default-value="xml" expression="xml"
    */
   protected String exportFilenameExt;
   
   /**
    * The directory into which the exported xml files will be output.
    * 
    * @parameter default-value="${project.build.directory}/exported-xml" expression="${project.build.directory}/exported-xml"
    */
   protected File exportDirectory;
   
   /**
    * Creates an export file per query result instead of a single file with exportFilename.
    * 
    * @parameter default-value="false" expression="false"
    */
   protected boolean exportPerResult;
   
   /**
    * If exportPerResult is true, names the exported file using the value of
    * the property specified prepended by exportFilenamePrefix. If a second
    * object is encountered with the same property value, a "-" followed by
    * the count will be appended to help ensure each export in a single
    * execution results in a different filename. 
    * 
    * @parameter
    */
   protected String exportFilenameProperty;
   
   /**
    * Query criteria used to retrieve specific objects when exporting.
    * 
    * @parameter
    */
   protected QueryCriteria queryCriteria;
   
   /**
    * Configures the transformer's output indentation amount.
    * 
    * @parameter default-value=2 expression=2
    */
   protected int indentAmount;
   
   /**
    * If true, uses an exporter that streams objects to xml. This is useful when
    * the number of objects and attributes is so large that the xml produced
    * cannot be held in memory all at once. Currently the streamed xml is
    * unformatted (i.e. no "pretty print" indentation).
    * 
    * @parameter default-value=false expression=false
    */
   protected boolean streamResults;
   
   public void execute() throws MojoExecutionException {
      try {
         BeanToXMLExport exporter = null;
         if (!streamResults) {
            exporter = new BeanToXMLExport(schemaPath);
         }
         
         BeanToXMLStreamExport streamExporter = null;
         if (streamResults) {
            streamExporter = new BeanToXMLStreamExport(schemaPath);
         }
         
         BeanManager<?> beanManager = view.getBeanManager(null);
         List<?> results;
         if (queryCriteria != null) {
            BeanCriteria criteria = queryCriteria.getBeanCriteria(view);
            results = beanManager.query(criteria);
         } else {
            results = beanManager.getAllInstances();
         }
         
         if (exportPerResult) {
            HashMap<String, Integer> prefixCounts = new HashMap<String, Integer>();
            for (int i = 0; i < results.size(); i++) {
               Object result = results.get(i);
               String prefix = exportFilenamePrefix;
               if (exportFilenameProperty != null) {
                  try {
                     beanManager = view.getBeanManager(result.getClass().getName());
                     Object value = beanManager.getBeanUtility().getValue(result, exportFilenameProperty);
                     prefix += value.toString();
                  } catch (Exception e) {
                     getLog().warn("Failed to get property value for '"+exportFilenameProperty+"' on object of type " 
                    		 + result.getClass().getName() + ".", e);
                  }
               }
               
               Integer count = prefixCounts.get(prefix);
               
               if (count == null) {
                  count = 0;
               }
               
               count++;
               prefixCounts.put(prefix, count);
               
               String filename = prefix + (count > 1 ? "-" + count : "") 
               		+ (exportFilenameExt != null && exportFilenameExt.length() > 0 ? "." + exportFilenameExt : "");
               File outFile = new File(exportDirectory, filename);
               
               if (exporter != null) {
                  exportDOM(exporter, result, outFile);
               } else if (streamExporter != null) {
                  exportStream(streamExporter, result, outFile);
               }
            }
         } else {
            File outFile = new File(exportDirectory, exportFilename);
            
            if (exporter != null) {
               exportDOM(exporter, results, outFile);
            } else if (streamExporter != null) {
               exportStream(streamExporter, results, outFile);
            }
         }
      } catch (Exception e) {
         throw new MojoExecutionException("Export Failed.", e);
      }
   }
   
   //TODO: Don't throw just a general exception, needs to be more specific
   private void exportDOM(XMLExportInterface exporter, Object toExport, File outFile) throws Exception {
      createFilePath(outFile);
      
      Document dom = null;
      if (toExport instanceof List<?>) {
         dom = exporter.export(((List<?>) toExport).toArray(new Object[((List<?>) toExport).size()]));
      } else {
         dom = exporter.export(new Object[] { toExport });
      }
      
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.METHOD, "xml");
      transformer.setOutputProperty(OutputKeys.INDENT, indentAmount > 0 ? "yes" : "no");
      
      if (indentAmount > 0) {
         transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "" + indentAmount);
      }
      
      transformer.transform(new DOMSource(dom), new StreamResult(outFile.getAbsolutePath()));
   }
   
   //TODO: Don't throw just a general exception, needs to be more specific
   private void exportStream(BeanToXMLStreamExport exporter, Object toExport, File outFile) throws Exception {
      createFilePath(outFile);
      
      OutputStream outputStream = null;
      try {
         outputStream = new FileOutputStream(outFile);
         if (toExport instanceof List<?>) {
            exporter.export(outputStream, ((List<?>) toExport).toArray(new Object[((List<?>) toExport).size()]), null);
         } else {
            exporter.export(outputStream, new Object[] { toExport }, null);
         }
      } catch (Exception e) {
         throw new MojoExecutionException("Stream exporting failed for file: '"+outFile.getAbsolutePath()+"'.", e);
      } finally {
         if (outputStream != null) {
            try {
               outputStream.close();
            } catch (IOException ioe) {
               throw new MojoExecutionException("Failed to close Stream.", ioe);
            }
         }
      }
   }
   
   private static void createFilePath(File file) throws IOException {
      if (!file.exists()) {
         File parentDir = file.getParentFile();
         
         if (parentDir != null) {
            parentDir.mkdirs();
         }
         
         file.createNewFile();
      }
   }
}
