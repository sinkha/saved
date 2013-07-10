package com.cdmtech.sol.maven.plugin;

import java.io.File;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.w3c.dom.Document;
import com.cdmtech.core.client.bean.AbstractView;
import com.cdmtech.core.client.bean.View;

/**
 * Exports SOM ship object to an ICODES XML file.
 * 
 * @goal export-objects-to-icodes6-ship
 * @phase generate-resources
 * @author csleight
 */
public class SOMToICODESShipExportMojo extends AbstractMojo {
   
   /**
    * The file name for the exported xml.
    * 
    * @parameter default-value="export.xml" expression="export.xml"
    */
   protected File exportFile;
   
   /**
    * Configures the transformer's output indentation amount.
    * 
    * @parameter default-value=2 expression=2
    */
   protected int indentAmount;
   
   /**
    * Used to determine which Ship exporter to use.
    * A value starting with "6" indicates usage of the
    * 'com.cdmtech.proj.sol.exporter.icodes.ship.icodes_ship_6_0_0.ShipDocumentCreator',
    * all other values will use 'com.cdmtech.proj.sol.exporter.icodes.ship.icodes_ship_1_0_0.ShipDocumentCreator'
    * 
    * @parameter
    */
   protected String icodesVersion;
   
   public void execute() throws MojoExecutionException {
      try {
         View<?> view = AbstractView.getDefaultView();
         Document document = null;
         if (icodesVersion != null && icodesVersion.startsWith("6")) {
            document = com.cdmtech.proj.sol.exporter.icodes.ship.icodes_ship_6_0_0.ShipDocumentCreator.createShipDocument(view, false, true);
         } else {
            document = com.cdmtech.proj.sol.exporter.icodes.ship.icodes_ship_1_0_0.ShipDocumentCreator.createShipDocument(view, false, true);
         }
         
         export(document);
         
      //TODO: Should catch a more specific exception(s)
      } catch (Exception e) {
         throw new MojoExecutionException("Failed exporting ICODES v6 ship" + (e.getMessage() != null ? ": " + e.getMessage() : ""), e);
      }
   }
   
   private void export(Document dom) throws Exception {
      if (!exportFile.exists()) {
         File parentDir = exportFile.getParentFile();
         
         if (parentDir != null) {
            parentDir.mkdirs();
         }
         
         exportFile.createNewFile();
      }
      
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.METHOD, "xml");
      transformer.setOutputProperty(OutputKeys.INDENT, indentAmount > 0 ? "yes" : "no");
      
      if (indentAmount > 0) {
         transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "" + indentAmount);
      }
      
      transformer.transform(new DOMSource(dom), new StreamResult(exportFile.getAbsolutePath()));
   }
}
