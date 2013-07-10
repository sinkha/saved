package com.cdmtech.sol.maven.plugin.metadata;

import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.artifact.filter.collection.ArtifactIdFilter;
import org.apache.maven.shared.artifact.filter.collection.ClassifierFilter;
import org.apache.maven.shared.artifact.filter.collection.FilterArtifacts;
import org.apache.maven.shared.artifact.filter.collection.GroupIdFilter;
import org.apache.maven.shared.artifact.filter.collection.ScopeFilter;
import org.apache.maven.shared.artifact.filter.collection.TransitivityFilter;
import org.apache.maven.shared.artifact.filter.collection.TypeFilter;

import com.cdmtech.sol.metadata.Component;
import com.cdmtech.sol.metadata.Domain;
import com.cdmtech.sol.metadata.ObjectFactory;
import com.cdmtech.sol.metadata.SourceComponent;

/**
 * Creates a Component Metadata XML file.
 * Adheres to the sol-meta component's component-2.0.xsd schema.
 * 
 * @goal create-component-metadata
 * @phase generate-resources
 * @author csleight
 */
public class CreateComponentMetadataMojo extends AbstractMojo {
   public static final String META_PACKAGE = "com.cdmtech.sol.metadata";
   
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
    * @parameter default-value="component.xml" expression="component.xml"
    */
   protected String outputFilename;
   
   /**
    * The name of this component.
    * 
    * @parameter
    */
   protected String name;
   
   /**
    * If name is not specified and this value is, then the name value will be
    * based on the maven project's artifactId after removing all occurrences of
    * this property's value from the artifactId.
    * 
    * Example: if artifactId = "this-is-the-uniqueName" and
    *          nameIsArtifactIdLess = "this-is-the-" then
    *          name = "uniqueName"
    * 
    * @parameter
    */
   protected String nameIsArtifactIdLess;
   
   /**
    * The display name of this component.
    * 
    * @parameter
    */
   protected String displayName;
   
   /**
    * If displayName is not specified and this value is, then the displayName
    * value will be based on the maven project's name after removing all
    * occurrences of this property's value from the project's name.
    * 
    * Example: if project name = "This is the projects Name" and
    *          displayNameIsProjectNameLess = "This is the projects " then
    *          displayName = "Name"
    * 
    * @parameter
    */
   protected String displayNameIsProjectNameLess;
   
   /**
    * The system name for this component.
    * 
    * @parameter
    */
   protected String systemName;
   
   /**
    * The system display name for this component.
    * 
    * @parameter
    */
   protected String systemDisplayName;
   
   /**
    * The system domain version for this component.
    * 
    * @parameter
    */
   protected String systemVersion;
   
   /**
    * The domain name for this component.
    * 
    * @parameter
    */
   protected String domainName;
   
   /**
    * The domain display name for this component.
    * 
    * @parameter
    */
   protected String domainDisplayName;
   
   /**
    * The domain version for this component.
    * 
    * @parameter
    */
   protected String domainVersion;
   
   /**
    * If we should exclude transitive dependencies when determining source
    * components.
    * 
    * @parameter expression="${excludeTransitive}" default-value="false"
    */
   protected boolean excludeTransitiveSources;
   
   /**
    * Comma-separated list of source component artifactIds to include. Empty
    * String indicates include everything (default).
    * 
    * @parameter expression="${includeSourceArtifactIds}" default-value=""
    */
   protected String includeSourceArtifactIds;
   
   /**
    * Comma-separated list of source component artifactIds to exclude. Empty
    * String indicates don't exclude anything (default).
    * 
    * @parameter expression="${excludeSourceArtifactIds}" default-value=""
    */
   protected String excludeSourceArtifactIds;
   
   /**
    * Comma-separated list of source component groupIds to include. Empty
    * String indicates include everything (default).
    * 
    * @parameter expression="${includeSourceGroupIds}" default-value=""
    */
   protected String includeSourceGroupIds;
   
   /**
    * Comma-separated list of source component groupIds to exclude. Empty
    * String indicates don't exclude anything (default).
    * 
    * @parameter expression="${excludeSourceGroupIds}" default-value=""
    */
   protected String excludeSourceGroupIds;
   
   /**
    * Comma-separated list of source component scopes to include. Empty
    * String indicates include everything (default).
    * 
    * @parameter expression="${includeSourceScope}" default-value=""
    */
   protected String includeSourceScope;
   
   /**
    * Comma-separated list of source component scopes to exclude. Empty
    * String indicates don't exclude anything (default).
    * 
    * @parameter expression="${excludeSourceScope}" default-value=""
    */
   protected String excludeSourceScope;
   
   /**
    * Comma-separated list of source component types to include. Empty
    * String indicates include everything (default).
    * 
    * @parameter expression="${includeSourceTypes}" default-value=""
    */
   protected String includeSourceTypes;
   
   /**
    * Comma-separated list of source component types to exclude. Empty
    * String indicates don't exclude anything (default).
    * 
    * @parameter expression="${excludeSourceTypes}" default-value=""
    */
   protected String excludeSourceTypes;
   
   /**
    * Comma-separated list of source component classifiers to include. Empty
    * String indicates include everything (default).
    * 
    * @parameter expression="${includeSourceClassifiers}" default-value=""
    */
   protected String includeSourceClassifiers;
   
   /**
    * Comma-separated list of source component classifiers to exclude. Empty
    * String indicates don't exclude anything (default).
    * 
    * @parameter expression="${excludeSourceClassifiers}" default-value=""
    */
   protected String excludeSourceClassifiers;
   
   /**
    * Specifies the zip/jar entry path to the metadata file within all included
    * dependency artifacts.
    * 
    * @parameter expression="${sourceDependencyMetadataPath}" default-value="doc/component.xml"
    */
   protected String sourceDependencyMetadataPath;
   
   /**
    * Indicates whether the build should fail if one or more of the included
    * source dependency artifacts is missing the metadata file located using the
    * sourceDependencyMetadataPath property.
    * 
    * @parameter expression="false" default-value="false"
    */
   protected boolean failOnMissingSourceDependencyMetadata;
   
   /**
    * Indicates whether the build should fail if no valid source component
    * dependencies were found given the specified criteria.
    * 
    * @parameter expression="false" default-value="false"
    */
   protected boolean failOnNoValidSources;
   
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
         
         if (this.name != null) {
            component.setName(this.name);
         }
         else if (nameIsArtifactIdLess != null) {
            String artifactId = project.getArtifactId();
            String name = artifactId.replaceAll(nameIsArtifactIdLess, "");
            component.setName(name.trim());
         }
         else {
            throw new MojoExecutionException("A name or way to derive a name (i.e. the nameIsArtifactIdLess property) must be specified.");
         }
         
         if (this.displayName != null) {
            component.setDisplayName(this.displayName);
         }
         else if (displayNameIsProjectNameLess != null) {
            String projectName = project.getName();
            String displayName = projectName.replaceAll(displayNameIsProjectNameLess, "");
            component.setDisplayName(displayName.trim());
         }
         
         GregorianCalendar c = new GregorianCalendar();
         c.setTime(new Date(System.currentTimeMillis()));
         XMLGregorianCalendar created = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
         component.setCreated(created);
         
         component.setVersion(project.getVersion());
         
         String revision = project.getProperties().getProperty(revisionProperty);
         component.setRevision(revision);
         
         com.cdmtech.sol.metadata.System system = objectFactory.createSystem();
         system.setName(this.systemName);
         system.setDisplayName(this.systemDisplayName);
         system.setVersion(this.systemVersion);
         
         component.setSystem(system);
         
         Domain domain = objectFactory.createDomain();
         domain.setName(this.domainName);
         domain.setDisplayName(this.domainDisplayName);
         domain.setVersion(this.domainVersion);
         
         component.setDomain(domain);
         
         addSources(objectFactory, component.getSources());
         if (failOnNoValidSources && component.getSources().size() == 0)
            throw new Exception("Failed to locate any valid source dependency artifacts. Verify that the intended dependencies exist and the dependency filters are configured correctly.");
         
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
   
   @SuppressWarnings("unchecked")
   private void addSources(ObjectFactory objectFactory, List<SourceComponent> sources) throws MojoExecutionException {
      try {
         if (project.getDependencyArtifacts() != null) {
            FilterArtifacts filter = new FilterArtifacts();
            
            filter.addFilter(new TransitivityFilter(project.getDependencyArtifacts(), this.excludeTransitiveSources));
            filter.addFilter(new ScopeFilter(this.includeSourceScope, this.excludeSourceScope));
            filter.addFilter(new TypeFilter(this.includeSourceTypes, this.excludeSourceTypes));
            filter.addFilter(new ClassifierFilter(this.includeSourceClassifiers, this.excludeSourceClassifiers));
            filter.addFilter(new GroupIdFilter(this.includeSourceGroupIds, this.excludeSourceGroupIds));
            filter.addFilter(new ArtifactIdFilter(this.includeSourceArtifactIds, this.excludeSourceArtifactIds));
            
            Set<Artifact> candidateArtifacts = filter.filter(project.getDependencyArtifacts());
            for (Artifact candidateArtifact : candidateArtifacts) {
               File file = candidateArtifact.getFile();
               ZipFile zipFile = new ZipFile(file);
               ZipEntry zipEntry = zipFile.getEntry(sourceDependencyMetadataPath);
               if (zipEntry != null) {
                  InputStream inputStream = zipFile.getInputStream(zipEntry);
                  JAXBContext jaxbContext = JAXBContext.newInstance(META_PACKAGE);
                  Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                  Component component = (Component) unmarshaller.unmarshal(inputStream);
                  
                  inputStream.close();
                  
                  SourceComponent sourceComponent = objectFactory.createSourceComponent();
                  sourceComponent.setName(component.getName());
                  sourceComponent.setDisplayName(component.getDisplayName());
                  sourceComponent.setVersion(component.getVersion());
                  sourceComponent.setCreated(component.getCreated());
                  sourceComponent.setRevision(component.getRevision());
                  sourceComponent.setName(component.getName());
                  sourceComponent.setSystem(component.getSystem());
                  sourceComponent.setDomain(component.getDomain());
                  for (SourceComponent sc : component.getSources()) {
                     sourceComponent.getSources().add(sc);
                  }
                  
                  sources.add(sourceComponent);
               }
               else if (failOnMissingSourceDependencyMetadata) {
                  throw new Exception("Failed to locate metadata file \""+sourceDependencyMetadataPath+"\" in dependency artifact \""+file.getAbsolutePath()+"\".");
               }
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
         throw new MojoExecutionException("Failed to process source artifacts from project dependencies" + (e.getMessage() != null ? ": " + e.getMessage() : "") + ".", e);
      }
   }
}
