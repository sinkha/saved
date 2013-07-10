package com.cdmtech.m2e.flex.config;

import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.flexbuilder.project.FlexServerType;
import com.adobe.flexbuilder.project.actionscript.IMutableActionScriptProjectSettings;
import com.adobe.flexbuilder.project.air.IMutableApolloProjectSettings;
import com.adobe.flexbuilder.project.air.internal.ApolloProjectSettings;
import com.cdmtech.m2e.flex.Natures;
import com.cdmtech.m2e.flex.model.ClassPathDescriptor;
import com.cdmtech.m2e.flex.model.ProjectType;
import com.cdmtech.m2e.flex.sdk.ISdkMavenAdapter;

/**
 * @author svanhoos
 *
 */
public class AirProjectConfigurationHandler extends FlexConfigurationRequestHandlerBase {
   
   public AirProjectConfigurationHandler(IMavenProjectFacade facade,
         ProjectType projectType, ProjectConfigurationInfo configInfo,
         IProgressMonitor monitor) {
      super(facade, projectType, configInfo, monitor);
   }

   private static final Logger logger = LoggerFactory.getLogger(AirProjectConfigurationHandler.class);

   @Override
   protected void updateNatures(Set<String> natures) {
      super.updateNatures(natures);
      
      logger.debug("Adding AIR nature");
      natures.add(Natures.FLEX_AIR);
   }
   
   @Override
   protected IMutableActionScriptProjectSettings createProjectSettings() {
      return new ApolloProjectSettings(project.getName(), project.getLocation(),
            FlexServerType.NO_SERVER);
   }
   
   @Override
   protected void configureProjectSettings(IMutableActionScriptProjectSettings settings, ISdkMavenAdapter sdk, ClassPathDescriptor classpath)
         throws FlexConfiguratorException {
      super.configureProjectSettings(settings, sdk, classpath);
      
      IMutableApolloProjectSettings s = (IMutableApolloProjectSettings) settings;
      
      s.setUseAIRConfig(useApolloConfig(mavenProject.getArtifacts()));
   }
   
   private boolean useApolloConfig(Set<Artifact> dependencies) {
      for (Artifact a : dependencies) {
         if ("airglobal".equals(a.getArtifactId())) {
            return true;
         }
      }
      return false;
   }
}
