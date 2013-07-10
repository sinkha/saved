/**
 * 
 */
package com.cdmtech.m2e.flex.config;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.flexmojos.utilities.SourceFileResolver;

import com.adobe.flexbuilder.project.FlexServerType;
import com.adobe.flexbuilder.project.actionscript.IMutableActionScriptProjectSettings;
import com.adobe.flexbuilder.project.internal.FlexProjectSettings;
import com.cdmtech.m2e.flex.Natures;
import com.cdmtech.m2e.flex.model.ClassPathDescriptor;
import com.cdmtech.m2e.flex.model.FlexConfigFile;
import com.cdmtech.m2e.flex.model.ProjectType;
import com.cdmtech.m2e.flex.sdk.ISdkMavenAdapter;

/**
 * @author svanhoos
 *
 */
public abstract class FlexConfigurationRequestHandlerBase extends AbstractConfigurationRequestHandler {
   
   public FlexConfigurationRequestHandlerBase(IMavenProjectFacade facade,
         ProjectType projectType, ProjectConfigurationInfo configInfo,
         IProgressMonitor monitor) {
      super(facade, projectType, configInfo, monitor);
   }

   private static final Logger logger = LoggerFactory.getLogger(FlexConfigurationRequestHandlerBase.class);
   
   @Override
   protected void updateNatures(Set<String> natures) {
      super.updateNatures(natures);
      
      logger.debug("Adding ACTIONSCRIPT and FLEX APP natures");
      natures.add(Natures.ACTIONSCRIPT);
      natures.add(Natures.FLEX_APPLICATION);
   }
   
   @Override
   protected IMutableActionScriptProjectSettings createProjectSettings() {
      return new FlexProjectSettings(project.getName(), project.getLocation(), false,
            FlexServerType.NO_SERVER);
   }
   
   @Override
   protected void configureProjectSettings(IMutableActionScriptProjectSettings s,
         ISdkMavenAdapter sdk, ClassPathDescriptor classpath) throws FlexConfiguratorException {
      super.configureProjectSettings(s, sdk, classpath);

      File sourceFile = SourceFileResolver.resolveSourceFile( mavenProject.getCompileSourceRoots(),
            flexmojos.getSourceFile(), mavenProject.getGroupId(), mavenProject.getArtifactId());

      if (sourceFile == null) {
         throw new MarkedFlexConfiguratorException(
               "Could not find main application! (Hint: Try to create an MXML file below your source root)");
      }

      logger.debug("Using main application file: " + sourceFile);
      
      // Source folders & Main Application
      IPath mainSourceFolder = project.getFile(s.getMainSourceFolder()).getLocation();
      IPath mainApplication = new Path(sourceFile.getAbsolutePath()).makeRelativeTo(mainSourceFolder);
      s.setMainApplicationPath(mainApplication);
      Set<IPath> appPaths = new HashSet<IPath>();
      appPaths.add(mainApplication);
      if (flexmojos.getAdditionalApplications() != null) {
         for (String appPath : flexmojos.getAdditionalApplications()) {
            appPaths.add(project.getFile(appPath).getProjectRelativePath());
         }
      }
      s.setApplicationPaths(appPaths.toArray(new IPath[appPaths.size()]));
      
      // Html Wrapper
      s.setGenerateHTMLWrappers(flexmojos.getGenerateHtmlWrapper());
      
      // CSS
      if (flexmojos.getBuildCssFiles() != null) {
         Map<IPath, IPath> cssMap = new HashMap<IPath, IPath>();
         IPath cssOut = project.getFile("bin-debug").getProjectRelativePath();
         for (String css : flexmojos.getBuildCssFiles()) {
            cssMap.put(project.getFile(css).getProjectRelativePath(), cssOut);
         }
         s.setBuildCSSFiles(cssMap);
      }
      
      FlexConfigFile c = new FlexConfigFile(project);
      c.setThemes(getThemes());
      c.writeFile();
   }
   
   @Override
   protected List<String> getConfigFiles() {
      List<String> files = super.getConfigFiles();
      files.add(".flexConfig.xml");
      return files;
   }
   
   private List<String> getThemes() {
      List<String> themes = flexmojos.getThemes();
      if (themes == null) {
         themes = new ArrayList<String>();
      }
      
      for (Artifact a : configInfo.getDependencies()) {
         if ("theme".equals(a.getScope())) {
            
            if (a.getFile() != null) {
               themes.add(a.getFile().getAbsolutePath());
            }
            
         }
      }
      return themes;
   }
}
