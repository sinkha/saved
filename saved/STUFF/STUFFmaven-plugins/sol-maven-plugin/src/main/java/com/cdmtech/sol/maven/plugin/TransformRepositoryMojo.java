package com.cdmtech.sol.maven.plugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import com.cdmtech.proj.sol.meta.cr.v1.crmetadata.ObjectFactory;
import com.cdmtech.proj.sol.meta.cr.v1.crmetadata.Metadata;



/**
 * Rename a set of JAR files files based on meta-data found in the JAR
 * 
 * @goal transform-repo
 * @author kjacobse
 * @author csleight
 */
public class TransformRepositoryMojo extends AbstractMojo {

//   public static final String META_PACKAGE = "com.cdmtech.sol.metadata";
   
   public static final String META_PACKAGE = ObjectFactory.class.getPackage().getName();
   
   private static final String[] DEFAULT_INCLUDES = new String[] { "**" };

   /**
    * A list of files to include. Can contain ant-style wildcards and double
    * wildcards. By default, everything in the repoDirectory is included.
    * <code>**</code>
    * 
    * @parameter
    * 
    */
   private String[] includes = DEFAULT_INCLUDES;

   /**
    * A list of files to exclude. Can contain ant-style wildcards and double
    * wildcards.
    * 
    * @parameter
    * 
    */
   private String[] excludes;

   /**
    * Root directory of the included source files to be used during the
    * transformation.
    * 
    * @parameter expression="${project.build.directory}/cr-repo-assembly"
    */
   protected File repoDirectory;

   /**
    * Output directory for processed files
    * 
    * @parameter expression="${project.build.directory}/cr-repo-final"
    */
   protected File outputDirectory;
   
   /**
    * Specifies the path to the metadata file as it should be located within
    * the zip/jar.
    * 
    * @parameter expression="${metadataPath}" default-value="doc/cr.metadata.xml"
    */
   protected String metadataPath;
   
   /**
    * Indicates whether "-SNAPSHOT", if any was found in the component metadata,
    * should be removed from the resulting archive name.
    * 
    * @parameter defaultValue=true expression=true
    */
   protected boolean removeSnapshotFromVersion;
   
   /**
    * Indicates whether the revision, if any was found in the component
    * metadata, should be included in the resulting archive name.
    * 
    * @parameter defaultValue="false"
    */
   protected boolean includeRevision;
   
   /**
    * Indicates whether the "created" timestamp, if any was found in the component
    * metadata, should be included in the resulting archive name. Optionally uses
    * timestampFormat.
    * 
    * @parameter defaultValue="false"
    */
   protected boolean includeTimestamp;

   /**
    * If true, flatten hierarchy. Source file directories will be ignored and the
    * transformed files will be added directly to the destination directory. By
    * default, the hierarchy will be preserved.
    * 
    * @parameter defaultValue="false"
    */
   protected boolean flatten;
   
   /**
    * Apply this java.text.MessageFormat to the timestamp.
    * 
    * @parameter
    */
   protected String timestampFormat;

   public void execute() throws MojoExecutionException {
      try {
         List<String> repoFiles = FileProcessingUtils.getIncludedFilePaths(repoDirectory, includes, excludes);

         for (String f : repoFiles) {
            transformFile(f);
         }
      } catch (FileNotFoundException e) {
         getLog().error("Directory for repository to transform not found!");
         getLog().error(e);
      }
   }

   private void transformFile(String fileName) {
      File srcFile = new File(repoDirectory.getAbsolutePath() + "/" + fileName);
      getLog().info("transforming file: " + fileName);
      
      File tmpFile = new File(outputDirectory.getAbsolutePath() + "/" + fileName);
      
      File destFile;
      
      if(flatten) {
    	  destFile = new File(outputDirectory.getAbsolutePath() + "/"
            + composeNewFileName(srcFile.getName(), srcFile.getAbsolutePath()));
      } else {
    	  destFile = new File(tmpFile.getParentFile().getAbsolutePath() + "/"
    	    + composeNewFileName(srcFile.getName(), srcFile.getAbsolutePath()));
      }

      if ((destFile.getParentFile() != null) && (!destFile.getParentFile().exists())) {
         if (!(true == destFile.getParentFile().mkdirs())) {
            getLog().info("Error creating outputDirectory.");
         }
      }

      try {
         FileUtils.copyFile(srcFile, destFile);
      } catch (Exception e) {
         getLog().error("Unable to move file: " + srcFile);
         getLog().error(e);
      }

   }

   /**
    * Assumes jar passed in end with .jar extension
    */
   private String composeNewFileName(String oldFileName, String oldFilePath) {
      String newFileName = oldFileName;

      try {
         ZipFile archive = new ZipFile(oldFilePath);
         ZipEntry entry = archive.getEntry(metadataPath);
         InputStream inputStream = archive.getInputStream(entry);

         JAXBContext jaxbContext = JAXBContext.newInstance(META_PACKAGE);
         Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
         
         Metadata metadata = (Metadata) unmarshaller.unmarshal(inputStream);
         String version = metadata.getArtifact().getVersion();
         
         // strip off the .jar
         newFileName = oldFileName.substring(0, oldFileName.lastIndexOf('.'));

         newFileName = newFileName + "-" + version + ".jar";
         
         
/*         
         Component component = (Component) unmarshaller.unmarshal(inputStream);
         
         inputStream.close();
         
         newFileName = newFileName.substring(0, newFileName.lastIndexOf('.'));
         if (component.getVersion() != null && component.getVersion().length() > 0) {
            newFileName = newFileName + "-" + (removeSnapshotFromVersion ? component.getVersion().replaceAll("-SNAPSHOT", "") : component.getVersion());
         } else {
            throw new MojoExecutionException("No version was found in metadata for: '"+oldFileName+"'.");
         }
         
         if (includeRevision && (component.getRevision() == null || component.getRevision().length() == 0)) {
            throw new MojoExecutionException("No revision was found in metadata for: '"+oldFileName+"'.");
         } else if (includeRevision) {
            newFileName = newFileName + "." + component.getRevision();
         }
         
         if (includeTimestamp  && component.getCreated() == null) {
            throw new MojoExecutionException("No creation timestamp was found in metadata for: '"+oldFileName+"'.");
         } else if (includeTimestamp) {
            Date date = component.getCreated().toGregorianCalendar().getTime();
            String timestamp;
            if (timestampFormat != null) {
               timestamp = MessageFormat.format( timestampFormat, new Object[] { date } );
            } else {
               timestamp = String.valueOf(date);
            }
            
            newFileName = newFileName + "." + timestamp + ".jar";
         }
*/         
         
         
      } catch (Exception e) {
         getLog().error("Unable to determine new file name: " + oldFilePath);
         getLog().error(e);
      }

      return newFileName;
   }
}
