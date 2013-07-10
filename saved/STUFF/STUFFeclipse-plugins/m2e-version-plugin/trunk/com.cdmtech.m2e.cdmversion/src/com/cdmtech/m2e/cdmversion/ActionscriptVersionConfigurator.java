/**
 * 
 */
package com.cdmtech.m2e.cdmversion;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecution;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.m2e.core.lifecyclemapping.model.IPluginExecutionMetadata;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.configurator.AbstractBuildParticipant;
import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;

/**
 * @author svanhoos
 *
 */
public class ActionscriptVersionConfigurator extends AbstractProjectConfigurator {

   public static final String CDM_VERSION_GROUP_ID = "com.cdmtech.atlas.maven";
   public static final String CDM_VERSION_ARTIFACT_ID = "cdm-version-plugin";
   public static final String GOAL_GENERATE_AS = "generate-as-version";
   public static final String GOAL_GENERATE_JAVA = "generate-java-version";
   
   @Override
   public void configure(ProjectConfigurationRequest request, IProgressMonitor monitor)
         throws CoreException {
      
      IMaven maven = MavenPlugin.getMaven();
      IMavenProjectFacade facade = request.getMavenProjectFacade();
      MavenSession session = request.getMavenSession();

      for (MojoExecution generate : facade.getMojoExecutions(CDM_VERSION_GROUP_ID,
            CDM_VERSION_ARTIFACT_ID, monitor, GOAL_GENERATE_AS)) {
         String r = maven.getMojoParameterValue(session, generate, "outputDirectory", String.class);
         
         facade.getMavenProject().addCompileSourceRoot(r);
         
         IFolder f = facade.getProject().getFolder(facade.getProjectRelativePath(r));
         if (!f.exists()) {
            f.create(true, true, monitor);
         }
      }
   }
   
   @Override
   public AbstractBuildParticipant getBuildParticipant(IMavenProjectFacade projectFacade,
         MojoExecution execution, IPluginExecutionMetadata executionMetadata) {
      
      return new ActionscriptVersionBuildParticipant(execution, false);
   }

}
