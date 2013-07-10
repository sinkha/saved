package com.cdmtech.m2e.flex.config;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.project.IMavenProjectFacade;

import com.cdmtech.m2e.flex.model.ProjectType;


/**
 * @author svanhoos
 *
 */
public class FlexProjectConfigurationHandler extends FlexConfigurationRequestHandlerBase {

   public FlexProjectConfigurationHandler(IMavenProjectFacade facade,
         ProjectType projectType, ProjectConfigurationInfo configInfo,
         IProgressMonitor monitor) {
      super(facade, projectType, configInfo, monitor);
   }

//   private static Logger logger = LoggerFactory.getLogger(FlexProjectConfigurationHandler.class);
   
}
