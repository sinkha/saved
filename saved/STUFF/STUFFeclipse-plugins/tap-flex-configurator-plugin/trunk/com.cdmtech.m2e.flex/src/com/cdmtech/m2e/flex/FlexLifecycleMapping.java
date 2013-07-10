package com.cdmtech.m2e.flex;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IProjectConfigurationManager;
import org.eclipse.m2e.core.project.configurator.AbstractCustomizableLifecycleMapping;
import org.eclipse.m2e.core.project.configurator.ILifecycleMapping;
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
public class FlexLifecycleMapping extends AbstractCustomizableLifecycleMapping
      implements ILifecycleMapping {
   
   @SuppressWarnings("unused")
   private static final Logger logger = LoggerFactory.getLogger(FlexLifecycleMapping.class);
   
   public FlexLifecycleMapping() { }
   
   @Override
   public void configure(ProjectConfigurationRequest request, IProgressMonitor monitor) throws CoreException {
      super.configure(request, monitor);
      
      IProject project = request.getProject();
      if (project.getResourceAttributes().isReadOnly()){
        return;
      }
      
      IProjectConfigurationManager configurationManager = MavenPlugin.getProjectConfigurationManager();
      ILifecycleMapping lifecycleMapping = configurationManager.getLifecycleMapping(request.getMavenProjectFacade());
      if (lifecycleMapping == null) {
        return;
      }
      
      ProjectConfigurationInfo configInfo = new ProjectConfigurationInfo(
            request.getMavenProjectFacade(), request.getMavenSession(), monitor);
      
      ProjectType projectType = ProjectType.getProjectType(request.getMavenProject().getPackaging(),
            configInfo.hasAirGlobal(), configInfo.isPureActionscript());
      
      final IConfigurationDelegate h = projectType.createConfigurationHandler(
            request.getMavenProjectFacade(), configInfo, monitor);
      
      if (h != null) {
         monitor.subTask("Configuring Flash Builder project");
         
         IWorkspace workspace = ResourcesPlugin.getWorkspace();
         ISchedulingRule workspaceRule = workspace.getRuleFactory().createRule(workspace.getRoot());
         
         boolean newRule = (workspaceRule != Job.getJobManager().currentRule());
         
         if (newRule) {
            Job.getJobManager().beginRule(workspaceRule, monitor);
         }
         
         h.createAllConfiguration();
         
         if (newRule) {
            Job.getJobManager().endRule(workspaceRule);
         }
         monitor.worked(1);
      }
   }
}
