package com.cdmtech.m2e.flex;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.IMavenProjectRegistry;
import org.eclipse.m2e.core.project.ResolverConfiguration;
import org.eclipse.m2e.core.project.configurator.MojoExecutionBuildParticipant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author svanhoos
 *
 */
public class FlexmojosBuildParticipant extends MojoExecutionBuildParticipant {

   private static final Logger logger = LoggerFactory.getLogger(FlexmojosBuildParticipant.class);
   
   public FlexmojosBuildParticipant(MojoExecution execution) {
      this(execution, true);
   }
   
   public FlexmojosBuildParticipant(MojoExecution execution, boolean runOnIncremental) {
      super(execution, runOnIncremental);
   }
   
   @Override
   public Set<IProject> build(int kind, IProgressMonitor monitor) throws Exception {
      IMavenProjectFacade facade = getMavenProjectFacade();
      MavenProject mavenProject = facade.getMavenProject();
      ResolverConfiguration resolverConfiguration = facade.getResolverConfiguration();

      Set<Artifact> realArtifacts = mavenProject.getArtifacts();
      boolean shouldResolveWorkspace = resolverConfiguration.shouldResolveWorkspaceProjects();
      
      try {
         if (shouldResolveWorkspace) {
            mavenProject.setArtifacts(fixWorkspaceArtifacts(facade));
            resolverConfiguration.setResolveWorkspaceProjects(false);
         }
         
         Set<IProject> buildResult = super.build(kind, monitor);
         
         return buildResult;
      }
      finally {
         mavenProject.setArtifacts(realArtifacts);
         resolverConfiguration.setResolveWorkspaceProjects(shouldResolveWorkspace);
      }
   }
   
   private Set<Artifact> fixWorkspaceArtifacts(IMavenProjectFacade facade) {
      IMavenProjectRegistry projectRegistry = MavenPlugin.getMavenProjectRegistry();
      IWorkspaceRoot workspaceRoot = facade.getProject().getWorkspace().getRoot();
      
      Set<Artifact> fakeArtifacts = new HashSet<Artifact>();
      
      for (Artifact a : facade.getMavenProject().getArtifacts()) {
         IMavenProjectFacade dependencyFacade = projectRegistry.getMavenProject(
               a.getGroupId(), a.getArtifactId(), a.getVersion());
         if (dependencyFacade != null && dependencyFacade.getProject().equals(facade.getProject())) {
            continue;
         }
         
         Artifact newArtifact;
         
         if (dependencyFacade != null && dependencyFacade.getFullPath(a.getFile()) != null) {
            // Found artifact within a workspace project
            String projectName = dependencyFacade.getProject().getName();
            IPath output = new Path("/" + projectName + "/bin-debug/" + projectName + ".swc");
            IPath absoluteOutputPath = workspaceRoot.getFile(output).getLocation();
            File absoluteOutput = absoluteOutputPath.toFile();
            
            a.setFile(absoluteOutput);

            newArtifact = new DefaultArtifact(a.getGroupId(), a.getArtifactId(), a.getVersion(),
                  a.getScope(), a.getType(), a.getClassifier(), a.getArtifactHandler());
            newArtifact.setFile(absoluteOutput);
            
            logger.info("Creating temporary artifact to replace workspace project: {}", absoluteOutput.toString());
         }
         else {
            newArtifact = a;
         }
         
         fakeArtifacts.add(newArtifact);
      }
      
      return fakeArtifacts;
   }

}
