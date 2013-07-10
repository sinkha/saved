/**
 * 
 */
package com.cdmtech.m2e.cdmversion;

import org.apache.maven.plugin.MojoExecution;
import org.eclipse.m2e.core.lifecyclemapping.model.IPluginExecutionMetadata;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.configurator.AbstractBuildParticipant;
import org.eclipse.m2e.jdt.AbstractJavaProjectConfigurator;

/**
 * @author svanhoos
 *
 */
public class JavaVersionConfigurator extends AbstractJavaProjectConfigurator {

   @Override
   public AbstractBuildParticipant getBuildParticipant(IMavenProjectFacade projectFacade,
         MojoExecution execution, IPluginExecutionMetadata executionMetadata) {
      return new JavaVersionBuildParticipant(execution, false);
   }
   
}
