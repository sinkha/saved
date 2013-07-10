/**
 * 
 */
package com.cdmtech.m2e.flex.model;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cdmtech.m2e.flex.Packagings;
import com.cdmtech.m2e.flex.config.AirProjectConfigurationHandler;
import com.cdmtech.m2e.flex.config.FlexProjectConfigurationHandler;
import com.cdmtech.m2e.flex.config.IConfigurationDelegate;
import com.cdmtech.m2e.flex.config.LibraryProjectConfigurationHandler;
import com.cdmtech.m2e.flex.config.ProjectConfigurationInfo;

/**
 *
 */
public enum ProjectType {

   FLEX,
   FLEX_LIBRARY,
   ACTIONSCRIPT,
   AIR,
   AIR_LIBRARY;
   
   private static final Logger logger = LoggerFactory.getLogger(ProjectType.class);
   
   public static ProjectType getProjectType( String packaging, boolean useApoloConfig, boolean actionScript )
   {
      if (Packagings.SWF.equals(packaging) && actionScript) {
         return ACTIONSCRIPT;
      }
      else if (Packagings.AIR.equals(packaging)) {
         return AIR;
      }
      else if (Packagings.SWF.equals(packaging) && !actionScript) {
         return FLEX;
      }
      else if (Packagings.SWC.equals(packaging) && !useApoloConfig) {
         return FLEX_LIBRARY;
      }
      else if (Packagings.SWC.equals(packaging) && useApoloConfig) {
         return AIR_LIBRARY;
      }
      else {
         return FLEX_LIBRARY;
      }
   }
   
   public IConfigurationDelegate createConfigurationHandler(IMavenProjectFacade facade,
         ProjectConfigurationInfo configInfo, IProgressMonitor monitor) {
      IConfigurationDelegate handler = null;
      
      switch (this) {
      case ACTIONSCRIPT:
         //TODO: Add pure AS handler
         break;
      case AIR:
      case AIR_LIBRARY:
         handler = new AirProjectConfigurationHandler(facade, this, configInfo, monitor);
         //TODO: Add handler for air library
         break;
      case FLEX:
         handler = new FlexProjectConfigurationHandler(facade, this, configInfo, monitor);
         break;
      case FLEX_LIBRARY:
         handler = new LibraryProjectConfigurationHandler(facade, this, configInfo, monitor);
         break;
      }
      
//      handler.setProjectConfigurationRequest(request);
//      handler.setMavenProjectFacade(request.getMavenProjectFacade());
//      handler.setProjectType(this);
//      handler.setProjectConfigurationInfo(configInfo);

      logger.debug("Created a configuration handler of type: {}", handler.getClass());
      
      return handler;
   }
}
