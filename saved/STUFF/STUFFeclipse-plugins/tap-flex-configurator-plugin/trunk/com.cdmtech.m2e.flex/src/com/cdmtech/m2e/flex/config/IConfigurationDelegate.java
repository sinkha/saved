package com.cdmtech.m2e.flex.config;

import org.eclipse.core.runtime.CoreException;

/**
 * @author svanhoos
 *
 */
public interface IConfigurationDelegate {
   
   /**
    * Create the Flash Builder project configuration from scratch.
    */
   void createAllConfiguration() throws CoreException;
   
   void updateBuildPath();
   
//   ProjectConfigurationRequest getProjectConfigurationRequest();
//   void setProjectConfigurationRequest(ProjectConfigurationRequest request);
   
//   IMavenProjectFacade getMavenProjectFacade();
//   void setMavenProjectFacade(IMavenProjectFacade f);
   
//   ProjectType getProjectType();
//   void setProjectType(ProjectType projectType);
   
//   ProjectConfigurationInfo getProjectConfigurationInfo();
//   void setProjectConfigurationInfo(ProjectConfigurationInfo configInfo);
}
