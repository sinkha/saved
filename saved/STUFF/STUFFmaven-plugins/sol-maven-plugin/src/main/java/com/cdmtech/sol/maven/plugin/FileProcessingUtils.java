package com.cdmtech.sol.maven.plugin;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.codehaus.plexus.util.DirectoryScanner;

public class FileProcessingUtils {
   
   /**
    * Gets a resource URL
    * 
    * Note: The leading slash of the path will be removed so
    * that it can work with classes in a JAR file
    * 
    * @return A URL object that links to the resource.
	*/
   public static URL getResource(Class<?> claz, String resourcePath) {
      URL result = null;
      String path = resourcePath;
      
      while (path.startsWith("/")) {
         path = path.substring(1);
      }
      
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      
      if (classLoader == null) {
         classLoader = claz.getClassLoader();
         result = classLoader.getResource(path);
      } else {
         result = classLoader.getResource(path);
         
         if (result == null) {
            classLoader = claz.getClassLoader();
            if (classLoader != null) {
               result = classLoader.getResource(path);
            }
         }
      }
      
      return result;
   }
   
   /**
    * Gets a resource stream
    * 
    * Note: The leading slash of the path will be removed so
    * that it can work with classes in a JAR file
    * 
    * @return An InputStream object that links to the resource.
	*/  
   
   //TODO: Should not be throwing a general exception, needs to throw specific exceptions
   public static InputStream getResourceAsStream(Class<?> claz, String resourcePath) throws Exception {
      InputStream result = null;
      String path = resourcePath;
      
      File resourceFile = new File(resourcePath);
      if (resourceFile.exists() && resourceFile.canRead()) {
         result = new BufferedInputStream(new FileInputStream(resourceFile));
      } else {
         while (path.startsWith("/")) {
            path = path.substring(1);
         }
         
         ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
         
         if (classLoader == null) {
            classLoader = claz.getClassLoader();
            result = classLoader.getResourceAsStream(path);
         }
         else {
            result = classLoader.getResourceAsStream(path);
            if (result == null) {
               classLoader = claz.getClassLoader();
               if (classLoader != null) {
                  result = classLoader.getResourceAsStream(path);
               }
            }
         }
      }
      
      return result;
   }
   
   public static List<String> getIncludedFilePaths(File basedir, String[] includes, String[] excludes) throws FileNotFoundException {
      DirectoryScanner scanner = new DirectoryScanner();
      
      scanner.setBasedir(basedir);
      if (includes != null && includes.length != 0) {
          scanner.setIncludes(includes);
      }
      
      if (excludes != null && excludes.length != 0) {
          scanner.setExcludes(excludes);
      }
      
      scanner.addDefaultExcludes();
      scanner.scan();

      return Arrays.asList(scanner.getIncludedFiles());
   }
}
