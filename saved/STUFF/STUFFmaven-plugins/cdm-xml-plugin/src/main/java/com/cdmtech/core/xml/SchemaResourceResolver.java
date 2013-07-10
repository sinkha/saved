package com.cdmtech.core.xml;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;

import org.apache.maven.plugin.logging.Log;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

public class SchemaResourceResolver implements LSResourceResolver {
   protected String schemaPath;
   protected HashMap<String, LSInput> resolvedResources;
   protected Log log;
   protected boolean useClasspath = false; 
   
   public SchemaResourceResolver(String schemaPath, Log log) throws Exception {
      this.schemaPath = schemaPath;
      this.resolvedResources = new HashMap<String, LSInput>();
      this.log = log;
      
      File schemaFile = new File(schemaPath);
      if (!schemaFile.exists() || !schemaFile.canRead()) {
         URL schemaURL = FileProcessingUtils.getResource(getClass(), schemaPath);
         if (schemaURL == null) {
            throw new Exception("Unable to locate schema '"+schemaPath+"'.");
         }
         else {
            useClasspath = true;
         }
      }
   }
   
   public LSInput resolveResource(String type, String namespaceURI,
         String publicId, String systemId, String baseURI) {
      LSInput lsin = null;
      
      try {
         log.debug("resolveResource(type="+type+", namespaceURI="+namespaceURI+", publicId="+publicId+", systemId="+systemId+", baseURI="+baseURI+")");
         if (systemId != null) {
            String resourcePath = systemId.replaceAll("\\\\", "/");
            log.debug("resolveResource() - systemId = " + systemId);
            log.debug("resolveResource() - resourcePath = " + resourcePath);
            
            String currentPath;
            if (baseURI != null) {
               String modifiedBaseURI = baseURI.replaceFirst("file:[/]+", "");
               URI uri = new URI(modifiedBaseURI);
               
               currentPath = uri.toString().replaceAll("\\\\", "/");
               int index = currentPath.lastIndexOf("!");
               if (index >= 0) {
                  currentPath = currentPath.substring(index+1);
               }
               
               while (currentPath.endsWith("/")) {
                  currentPath = currentPath.substring(0, currentPath.length()-1);
               }
               index = currentPath.lastIndexOf("/");
               currentPath = currentPath.substring(0, index);
            }
            else {
               File schemaFile = new File(schemaPath);
               if (!schemaFile.exists() || !schemaFile.canRead()) {
                  URL schemaURL = FileProcessingUtils.getResource(getClass(), schemaPath);
                  if (schemaURL == null) {
                     throw new Exception("Unable to locate schema '"+schemaPath+"'.");
                  }
                  else {
                     currentPath = schemaURL.toString().replaceAll("\\\\", "/");
                     int index = currentPath.lastIndexOf("!");
                     if (index >= 0) {
                        currentPath = currentPath.substring(index+1);
                     }
                     
                     while (currentPath.endsWith("/")) {
                        currentPath = currentPath.substring(0, currentPath.length()-1);
                     }
                     index = currentPath.lastIndexOf("/");
                     currentPath = currentPath.substring(0, index);
                  }
               }
               else {
                  File currentDir = schemaFile.getParentFile();
                  currentPath = currentDir.getAbsolutePath().replaceAll("\\\\", "/");
               }
            }
            
            if (useClasspath && !currentPath.startsWith("/"))
               currentPath = "/" + currentPath;
               
            log.debug("resolveResource() - currentPath = " + currentPath);
            while (resourcePath.startsWith("../")) {
               resourcePath = resourcePath.substring(3);
               
               while (currentPath.endsWith("/")) {
                  currentPath = currentPath.substring(0, currentPath.length()-1);
               }
               int index = currentPath.lastIndexOf("/");
               currentPath = currentPath.substring(0, index);
            }
            
            String resourceLocation = currentPath + "/" + resourcePath;
            log.debug("resolveResource() - resourceLocation = "+resourceLocation);
            lsin = resolvedResources.get(resourceLocation);
            if (lsin == null) {
               InputStream resourceStream = FileProcessingUtils.getResourceAsStream(getClass(), resourceLocation);
               lsin = new LSInputImpl();
               lsin.setSystemId(resourceLocation);
               lsin.setByteStream(resourceStream);
               
               resolvedResources.put(resourceLocation, lsin);
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      
      return lsin;
   }
}
