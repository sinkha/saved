package com.cdmtech.m2e.flex;

import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecution;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.lifecyclemapping.model.IPluginExecutionMetadata;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.MavenProjectChangedEvent;
import org.eclipse.m2e.core.project.configurator.AbstractBuildParticipant;
import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cdmtech.m2e.flex.config.IConfigurationDelegate;
import com.cdmtech.m2e.flex.config.ProjectConfigurationInfo;
import com.cdmtech.m2e.flex.model.ProjectType;

/**
 * @author svanhoos
 *
 */
public class FlexmojosCompileConfigurator extends AbstractProjectConfigurator {

   private static final Logger logger = LoggerFactory.getLogger(FlexmojosCompileConfigurator.class);
   
   public FlexmojosCompileConfigurator() { }

   @Override
   public void configure(ProjectConfigurationRequest arg0, IProgressMonitor arg1)
         throws CoreException {

   }
   
   @Override
   public AbstractBuildParticipant getBuildParticipant(IMavenProjectFacade projectFacade,
         MojoExecution execution, IPluginExecutionMetadata executionMetadata) {
      return new NoopBuildParticipant();
   }

   @Override
   public void mavenProjectChanged(MavenProjectChangedEvent event, IProgressMonitor monitor)
         throws CoreException {
      super.mavenProjectChanged(event, monitor);

      IMavenProjectFacade oldFacade = event.getOldMavenProject();
      IMavenProjectFacade newFacade = event.getMavenProject();
      
      if (event.getKind() == MavenProjectChangedEvent.KIND_CHANGED) {
         if (event.getFlags() == MavenProjectChangedEvent.FLAG_DEPENDENCIES) {
            logger.debug(newFacade.getProject().getName()
                  + " dependency change.");

            //TODO: Skip updating the build path if this change was the result of
            // toggling workspace resolution. (Since m2e will add the "not up-to-date"
            // marker anyway)
            
            MavenExecutionRequest execReq = MavenPlugin
                  .getMavenProjectRegistry().createExecutionRequest(oldFacade,
                        monitor);
            MavenSession session = MavenPlugin.getMaven().createSession(
                  execReq, newFacade.getMavenProject());

            ProjectConfigurationInfo configInfo = new ProjectConfigurationInfo(
                  newFacade, session, monitor);

            ProjectType projectType = ProjectType.getProjectType(
                  newFacade.getPackaging(), configInfo.hasAirGlobal(),
                  configInfo.isPureActionscript());

            IConfigurationDelegate del = projectType
                  .createConfigurationHandler(newFacade, configInfo, monitor);
            
            logger.debug("Updating build path!!");
            
            if (del != null) {
               del.updateBuildPath();
            }

         }
         else {
            logger.debug(newFacade.getProject().getName() + " non-dependency project change.");
         }
      }
      else if (event.getKind() == MavenProjectChangedEvent.KIND_ADDED) {
         logger.debug(newFacade.getProject().getName() + " - Maven project \"added\".");
      }
      else {
         logger.debug(oldFacade.getProject().getName() + " - Maven project \"removed\".");
      }
      
      return;
      
//      ResolverConfiguration oldRc = oldFacade.getResolverConfiguration();
//      ResolverConfiguration rc = facade.getResolverConfiguration();
//
//      if (oldRc.shouldResolveWorkspaceProjects() != rc.shouldResolveWorkspaceProjects()) {
//         logger.debug("'Resolve Workspace Projects' setting was changed to: {}.",
//               rc.shouldResolveWorkspaceProjects());
//         forceUpdate(facade, monitor);
//         return;
//      }
   }
}
