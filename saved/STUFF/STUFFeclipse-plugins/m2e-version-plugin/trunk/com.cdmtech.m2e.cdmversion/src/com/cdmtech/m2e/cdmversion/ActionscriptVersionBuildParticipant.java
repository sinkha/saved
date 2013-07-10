package com.cdmtech.m2e.cdmversion;

import java.io.File;
import java.util.Set;

import org.apache.maven.plugin.MojoExecution;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.m2e.core.project.configurator.MojoExecutionBuildParticipant;

/**
 * @author svanhoos
 *
 */
public class ActionscriptVersionBuildParticipant extends MojoExecutionBuildParticipant {

   /**
    * @param execution
    * @param runOnIncremental
    */
   public ActionscriptVersionBuildParticipant(MojoExecution execution, boolean runOnIncremental) {
      super(execution, runOnIncremental);
   }
   
   @Override
   public Set<IProject> build(int kind, IProgressMonitor monitor) throws Exception {
      Set<IProject> result = super.build(kind, monitor);
      
      IMaven maven = MavenPlugin.getMaven();
      
      File generated = maven.getMojoParameterValue(getSession(), getMojoExecution(), "outputDirectory", File.class);
      if (generated != null) {
         getBuildContext().refresh(generated);
      }
      
      return result;
   }

}
