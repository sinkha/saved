package com.cdmtech.sol.maven.plugin.metadata;

import java.io.File;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import com.cdmtech.proj.sol.meta.Component;
import com.cdmtech.proj.sol.meta.ComponentVersion;
import com.cdmtech.proj.sol.meta.Domain;
import com.cdmtech.proj.sol.meta.ObjectFactory;
import com.cdmtech.proj.sol.meta.Version;

/**
 * Creates the "old" Component Metadata XML file.
 * Adheres to the sol-meta component's component.xsd schema.
 * This mojo will eventually go away.
 * 
 * @goal create-old-component-metadata
 * @phase generate-resources
 * @author csleight
 * @deprecated
 */
public class CreateOldComponentMetadataMojo extends AbstractMojo {
   public static final String META_PACKAGE = "com.cdmtech.proj.sol.meta";
   
   /**
    * @parameter expression="${project}"
    * @required
    * @readonly
    * @since 1.0
    */
   protected MavenProject project;
   
   /**
    * The directory into which the component metadata XML will be output.
    * 
    * @parameter default-value="${project.build.outputDirectory}/doc" expression="${project.build.outputDirectory}/doc"
    */
   protected File outputDirectory;
   
   /**
    * The file name for the component metadata XML artifact.
    * 
    * @parameter default-value="metadata.xml" expression="metadata.xml"
    */
   protected String outputFilename;
   
   /**
    * The id of this component.
    * 
    * @parameter
    */
   protected String id;
   
   /**
    * If name is not specified and this value is, then the name value will be
    * based on the maven project's artifactId after removing all occurrences of
    * this property's value from the artifactId.
    * 
    * Example: if artifactId = "this-is-the-uniqueId" and
    *          idIsArtifactIdLess = "this-is-the-" then
    *          id = "uniqueId"
    * 
    * @parameter
    */
   protected String idIsArtifactIdLess;
   
   /**
    * The domain name for this component.
    * 
    * @parameter
    */
   protected String domainName;
   
   /**
    * The Maven property containing the SCM revision value at build time.
    *  
    * @parameter default-value="buildNumber" expression="buildNumber"
    */
   protected String revisionProperty;
   
   public void execute() throws MojoExecutionException {
      try {
         ObjectFactory objectFactory = new ObjectFactory();
         Component component = objectFactory.createComponent();
         
         if (this.id != null) {
            component.setId(this.id);
         }
         else if (idIsArtifactIdLess != null) {
            String artifactId = project.getArtifactId();
            String id = artifactId.replaceAll(idIsArtifactIdLess, "");
            component.setId(id.trim());
         }
         else {
            throw new MojoExecutionException("An id or way to derive an id (i.e. the nameIsArtifactIdLess property) must be specified.");
         }
         
         GregorianCalendar c = new GregorianCalendar();
         c.setTime(new Date(System.currentTimeMillis()));
         XMLGregorianCalendar created = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
         component.setCreated(created);
         
         ComponentVersion componentVersion = getComponentVersion(objectFactory);
         component.setVersion(componentVersion);
         
         Domain domain = objectFactory.createDomain();
         Version domainVersion = objectFactory.createVersion();
         domainVersion.setMajor(0);
         domainVersion.setMinor(0);
         domainVersion.setPatch(0);
         domain.setName(this.domainName);
         domain.setVersion(domainVersion);
         
         component.setDomain(domain);
         
         File outFile = new File(outputDirectory, outputFilename);
         File parentDir = outFile.getParentFile();
         if (parentDir != null)
            parentDir.mkdirs();
         
         outFile.createNewFile();
         
         JAXBContext jaxbContext = JAXBContext.newInstance(META_PACKAGE);
         
         Marshaller marshaller = jaxbContext.createMarshaller();
         marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
         marshaller.marshal(component, outFile);
         
         if (project.getExecutionProject() != null) {
            project.getExecutionProject().addCompileSourceRoot(outputDirectory.getAbsolutePath());
         }
         
         project.addCompileSourceRoot(outputDirectory.getAbsolutePath());
      } catch (Exception e) {
         throw new MojoExecutionException("Failed to create component metadata XML" + (e.getMessage() != null ? ": " + e.getMessage() : "") + ".", e);
      }
   }
   
   private ComponentVersion getComponentVersion(ObjectFactory objectFactory) throws Exception {
      String version = project.getVersion();
      ComponentVersion componentVersion = objectFactory.createComponentVersion();
      
      try {
         int limit = 3;
         String revision = project.getProperties().getProperty(revisionProperty);
         if (revision != null)
            limit = 2;
         
         String[] versionParts = version.replaceAll("-SNAPSHOT", "").split("\\.");
         for (int i = 0; i < versionParts.length && i < limit; i++) {
            int v = Integer.parseInt(versionParts[i]);
            
            if (i == 0)
               componentVersion.setMajor(v);
            else if (i == 1)
               componentVersion.setMinor(v);
            else if (i == 2)
               componentVersion.setPatch(v);
         }
         
         if (revision != null)
            componentVersion.setPatch(Integer.parseInt(revision));
      } catch (Exception e) {
         e.printStackTrace();
         throw new Exception("Unsupported version format: " + version);
      }
      
      return componentVersion;
   }
}
