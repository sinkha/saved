package com.cdmtech.m2e.flex.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.flexbuilder.project.FlexProjectManager;
import com.adobe.flexbuilder.project.IClassPathEntry;
import com.adobe.flexbuilder.project.IFlexLibraryProject;
import com.adobe.flexbuilder.project.actionscript.IActionScriptProject;
import com.adobe.flexbuilder.project.actionscript.IActionScriptProjectSettings;

/**
 * @author svanhoos
 *
 */
public class ClassPathDescriptor {

   private static final String[] SDK_SOURCES = {
      "automation", "flex", "framework", "haloclassic", "rpc", "utilities"
      };
   
   static {
      Arrays.sort(SDK_SOURCES);
   }
   
   private static Logger logger = LoggerFactory.getLogger(ClassPathDescriptor.class);
   
   private List<IClassPathEntryDescriptor> entries = new ArrayList<IClassPathEntryDescriptor>();
   
   public List<IClassPathEntryDescriptor> getEntries() {
      return entries;
   }
   
   public void addEntry(IClassPathEntryDescriptor entry) {
      entries.add(entry);
   }
   
   public IClassPathEntryDescriptor addProjectDependency(Artifact artifact, LinkType linkType, IMavenProjectFacade facade) {
      ProjectClassPathEntryDescriptor d = new ProjectClassPathEntryDescriptor();
      
      String projectName = facade.getProject().getName();
      IActionScriptProject asProject = FlexProjectManager.getActionScriptOrFlexProject(facade
            .getProject());
      
      if (asProject == null) {
         // Unable to load the Flash Builder project for the given workspace dependency
         // I _believe_ this could happen in the case that a library project is mavenized
         // and another workspace project is referencing it but the flex properties haven't been
         // generated.
         //TODO: Possibly add an error marker to notify the user
         
         logger.error("Unable to open actionscript project for workspace dependency: " + facade.getProject().getName());
         
         // Fallback to flexmojos method of deriving .swc location
         // FlexbuilderMojo:308
         d.setPath("/" + projectName  + "/bin-debug/" + projectName + ".swc");
      }
      else {
      
         if (asProject instanceof IFlexLibraryProject) {
            // This is a library project
            // Use the output swc location determined by Flash Builder
            IFlexLibraryProject swcProject = (IFlexLibraryProject) asProject;
            d.setPath(swcProject.getOutputDebugSWCFile().getFullPath().toString());
         }
         else {
            logger.error("Workspace dependency is not a flex library project: " + facade.getProject().getName());
            // TODO: Find out if this can even happen...
            
            d.setPath(asProject.getOutputContainer().getFullPath()
                  .append("/bin-debug/" + projectName + ".swc").toString());
         }
      }
      
      // TODO: Improve this to use source directory specified in POM if available
      d.setSourcePath("/" + projectName + "/src/main/flex/");
      d.setLinkType(linkType);
      
      addEntry(d);
      return d;
   }
   
   public IClassPathEntryDescriptor addLibraryDependency(Artifact artifact, LinkType linkType) {
      LibraryClassPathEntryDescriptor d = new LibraryClassPathEntryDescriptor();
      
      d.setPath(artifact.getFile().getAbsolutePath());
      d.setLinkType(linkType);
      
      // Based on FlexbuilderMojo:350
      // TODO: Improve to auto attach sources if available in maven
      // TODO: Or implement "Download Sources" menu option like JDT
      if (Arrays.binarySearch(SDK_SOURCES, artifact.getArtifactId()) >= 0) {
         d.setSourcePath("${PROJECT_FRAMEWORKS}/projects/" + artifact.getArtifactId()
               + "/src");
      }
      
      addEntry(d);
      return d;
   }
   
   public IClassPathEntry[] toFlexClasspath(IActionScriptProjectSettings context) {
      List<IClassPathEntry> classpath = new ArrayList<IClassPathEntry>(entries.size());
      
      for (IClassPathEntryDescriptor d : entries) {
         classpath.add(d.toFlexEntry(context));
      }
      
      return classpath.toArray(new IClassPathEntry[classpath.size()]);
   }
   
}
