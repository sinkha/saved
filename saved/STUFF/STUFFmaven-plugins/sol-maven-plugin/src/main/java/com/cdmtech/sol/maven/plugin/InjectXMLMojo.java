package com.cdmtech.sol.maven.plugin;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Injects root element children from a source XML document into the root
 * element of a target XML document.
 * 
 * @goal inject-xml
 * @phase process-resources
 * @author csleight
 */
public class InjectXMLMojo extends AbstractMojo {
   /**
    * Source XML file.
    * 
    * @parameter
    */
   protected File source;
   
   /**
    * Target XML file.
    * 
    * @parameter
    */
   protected File target;
   
   /**
    * Result XML file.
    * 
    * @parameter
    */
   protected File result;
   
   /**
    * Configures the output indentation amount.
    * 
    * @parameter default-value=2 expression=2
    */
   protected int indentAmount = 2;
   
   /**
    * Indicates whether this goal should fail if the source file isn't found.
    * 
    * @parameter default-value=true expression=true
    */
   protected boolean failIfNotFound = true;
   
   public void execute() throws MojoExecutionException {
      try {
         if (!source.exists() && !failIfNotFound) {
            return;
         }

         DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
         documentBuilderFactory.setNamespaceAware(true);
         DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
         
         InputStream sourceStream = new BufferedInputStream(new FileInputStream(source));
         Document sourceDocument = builder.parse(sourceStream);
         sourceStream.close();
         
         InputStream targetStream = new BufferedInputStream(new FileInputStream(target));
         Document targetDocument = builder.parse(targetStream);
         targetStream.close();
         Node dNode = targetDocument.getDocumentElement();
         Node firstChild = dNode.getFirstChild();
         
         NodeList sourceNodes = sourceDocument.getDocumentElement().getChildNodes();
         for (int i = 0; i < sourceNodes.getLength(); i++) {
            Node sourceNode = sourceNodes.item(i);
            Node cNode = sourceNode.cloneNode(true);
            targetDocument.adoptNode(cNode);
            
            if (firstChild != null) {
               dNode.insertBefore(cNode, firstChild);
            } else {
               dNode.appendChild(cNode);
            }
         }
         
         //TODO: write out targetDocument
         
         File parentDir = this.result.getParentFile();
         if (parentDir != null) {
            parentDir.mkdirs();
         }
         
         this.result.createNewFile();
         
         DOMSource domSource = new DOMSource(targetDocument);
         Result result = new StreamResult(this.result);
         
         TransformerFactory transformerFactory = TransformerFactory.newInstance();
         Transformer transformer = transformerFactory.newTransformer();
         transformer.setOutputProperty(OutputKeys.METHOD, "xml");
         transformer.setOutputProperty(OutputKeys.INDENT, indentAmount > 0 ? "yes" : "no");
         
         if (indentAmount > 0) {
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "" + indentAmount);
         }
         
         transformer.transform(domSource, result);
      } catch (Exception e) {
         throw new MojoExecutionException("Failed xml injection" + (e.getMessage() != null ? ": " + e.getMessage() : ""), e);
      }
   }
}
