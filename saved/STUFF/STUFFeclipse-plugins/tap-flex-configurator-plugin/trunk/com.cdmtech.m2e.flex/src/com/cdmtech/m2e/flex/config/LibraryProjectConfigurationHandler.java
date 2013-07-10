package com.cdmtech.m2e.flex.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.model.Resource;
import org.codehaus.plexus.util.DirectoryScanner;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.flexbuilder.project.IMutableFlexLibraryProjectSettings;
import com.adobe.flexbuilder.project.XMLNamespaceManifestPath;
import com.adobe.flexbuilder.project.actionscript.IMutableActionScriptProjectSettings;
import com.adobe.flexbuilder.project.internal.FlexLibraryProjectSettings;
import com.cdmtech.m2e.flex.Natures;
import com.cdmtech.m2e.flex.model.ClassPathDescriptor;
import com.cdmtech.m2e.flex.model.LinkType;
import com.cdmtech.m2e.flex.model.ProjectType;
import com.cdmtech.m2e.flex.pom.FlexNamespace;
import com.cdmtech.m2e.flex.sdk.ISdkMavenAdapter;

/**
 * @author svanhoos
 *
 */
public class LibraryProjectConfigurationHandler extends AbstractConfigurationRequestHandler {
   
   public LibraryProjectConfigurationHandler(IMavenProjectFacade facade,
         ProjectType projectType, ProjectConfigurationInfo configInfo,
         IProgressMonitor monitor) {
      super(facade, projectType, configInfo, monitor);
   }

   private static final Logger logger = LoggerFactory
         .getLogger(LibraryProjectConfigurationHandler.class);
   
   @Override
   protected void updateNatures(Set<String> natures) {
      super.updateNatures(natures);
      
      logger.debug("Adding ACTIONSCRIPT and FLEX LIB natures");
      natures.add(Natures.ACTIONSCRIPT);
      natures.add(Natures.FLEX_LIBRARY);
   }
   
   @Override
   protected IMutableActionScriptProjectSettings createProjectSettings() {
//      return new FlexLibraryProjectSettings(project.getName(), project.getLocation(), false);
      return new FlexLibraryProjectSettings(project.getName(),
            project.getLocation(),
            false, // override html wrapper default
            false, // use multi-platform config
            false); // is flash catalyst compatible
   }
   
   @Override
   protected void configureProjectSettings(IMutableActionScriptProjectSettings settings, ISdkMavenAdapter sdk, ClassPathDescriptor classpath)
         throws FlexConfiguratorException {
      super.configureProjectSettings(settings, sdk, classpath);
      
      IMutableFlexLibraryProjectSettings s = (IMutableFlexLibraryProjectSettings) settings;
      
      s.setMainApplicationPath(new Path(mavenProject.getArtifactId() + ".as"));
      s.setGenerateHTMLWrappers(false);
      
      if (flexmojos.getNamespaces() != null) {
         List<XMLNamespaceManifestPath> namespaceEntries = new ArrayList<XMLNamespaceManifestPath>();
         for (FlexNamespace n : flexmojos.getNamespaces()) {
            IPath manifest = pathFromString(n.manifest);
            IPath src = project.getFile("src/main/flex").getLocation();
            manifest = manifest.makeRelativeTo(src);
            namespaceEntries.add(new XMLNamespaceManifestPath(n.uri, manifest));
         }
         s.setManifestPaths(namespaceEntries.toArray(new XMLNamespaceManifestPath[namespaceEntries.size()]));
      }
      
      // Flex Library-Specific Settings
      if (flexmojos.getIncludeClasses() != null) {
         s.setIncludeClasses(flexmojos.getIncludeClasses());
      }
      
      String[] includeFiles = flexmojos.getIncludeFiles();
      if (includeFiles == null || includeFiles.length == 0) {
         includeFiles = listAllResources();
      }
      
      s.setIncludeFiles(createIncludeFilesMap(includeFiles));
   }
   
   @Override
   protected void populateAdditionalCompilerArguments(List<String> args) {
      super.populateAdditionalCompilerArguments(args);
      
      String[] includeClasses = flexmojos.getIncludeClasses();
      String[] includeSources = flexmojos.getIncludeSources();
      
      if ((includeClasses == null || includeClasses.length == 0) &&
            (includeSources == null || includeSources.length == 0)) {
         args.add("-include-sources " + join(getSourceRoots(), " ", true));
      }
      else if (includeSources == null || includeSources.length == 0) {
         args.add("-include-sources " + join(getRelativeIncludedSources(), " ", true));
      }
   }

   private List<String> getRelativeIncludedSources() {
      List<String> result = new ArrayList<String>();
      if (flexmojos.getIncludeSources() != null) {
         for (String s : flexmojos.getIncludeSources()) {
            result.add(project.getFile(s).getProjectRelativePath().toString());
         }
      }
      return result;
   }
   
   private String[] listAllResources() {
      List<String> resources = new ArrayList<String>();
      for (Resource resource : mavenProject.getResources()) {
         File resourceDir = new File(resource.getDirectory());
         if (!resourceDir.exists())
            continue;
         
         DirectoryScanner scanner = new DirectoryScanner();
         scanner.setBasedir(resourceDir);
         
         List<String> includes = resource.getIncludes();
         if (includes != null && !includes.isEmpty()) {
            scanner.setIncludes(includes.toArray(new String[includes.size()]));
         }
         
         List<String> excludes = resource.getExcludes();
         if (excludes != null && !excludes.isEmpty()) {
            scanner.setExcludes(excludes.toArray(new String[excludes.size()]));
         }
         
         scanner.addDefaultExcludes();
         scanner.scan();
         
         resources.addAll(Arrays.asList(scanner.getIncludedFiles()));
      }
      
      return resources.toArray(new String[resources.size()]);
   }
   
   private Map<IPath, IPath> createIncludeFilesMap(String[] resources) {
      Map<IPath, IPath> map = new HashMap<IPath, IPath>();
      
      for (String file : resources) {
         
         for (Resource resourceFolder : mavenProject.getBuild().getResources()) {
            File f = new File(resourceFolder.getDirectory(), file);
            if (f.exists()) {
               Path absolute = new Path(f.getAbsolutePath());
               Path omg = new Path(resourceFolder.getDirectory());
               IPath relative = absolute.makeRelativeTo(omg);
               map.put(relative, relative);
            }
         }
         
      }
      return map;
   }
   
   @Override
   protected LinkType getDefaultLinkType() {
      return LinkType.EXTERNAL;
   }
   
   @Override
   protected LinkType getLibraryPathDefaultLinkType() throws FlexConfiguratorException {
      return LinkType.DEFAULT;
   }
   
}
