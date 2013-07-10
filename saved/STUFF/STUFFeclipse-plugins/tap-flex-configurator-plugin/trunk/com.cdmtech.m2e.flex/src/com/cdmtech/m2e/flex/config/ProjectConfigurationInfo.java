/**
 * 
 */
package com.cdmtech.m2e.flex.config;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.building.DefaultModelBuildingRequest;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.path.DefaultPathTranslator;
import org.apache.maven.model.path.PathTranslator;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;

import com.cdmtech.m2e.flex.Packagings;
import com.cdmtech.m2e.flex.pom.Flexmojos3Configuration;
import com.cdmtech.m2e.flex.pom.Flexmojos4Configuration;
import com.cdmtech.m2e.flex.pom.FlexmojosConfiguration;
import com.cdmtech.m2e.flex.pom.ModelObjectInterpolator;

/**
 * @author svanhoos
 *
 */
public class ProjectConfigurationInfo {

   private static final String FLEXMOJOS_GROUPID = "org.sonatype.flexmojos";
   private static final String FLEXMOJOS_ARTIFACTID = "flexmojos-maven-plugin";
   private static final ComparableVersion FLEXMOJOS_3_9 = new ComparableVersion("3.9");
   
   private MavenProject mavenProject;
   private List<Artifact> processedDependencies;
   private Artifact globalDependency = null;
   private FlexmojosConfiguration configuration;
   
   /**
    * 
    */
   public ProjectConfigurationInfo(IMavenProjectFacade facade, MavenSession session, IProgressMonitor monitor) {
      this.mavenProject = facade.getMavenProject();
      processDependencies();
      
      ModelObjectInterpolator interpolator = createInterpolator(facade, session);
//      MojoExecution flexmojosExec = getExecution(facade, monitor);
      Plugin flexmojos = getFlexmojosPlugin(mavenProject);
      
      ComparableVersion flexmojosVersion = new ComparableVersion(flexmojos.getVersion());
      if (flexmojosVersion.compareTo(FLEXMOJOS_3_9) > 0) {
         configuration = new Flexmojos4Configuration(flexmojos, interpolator);
      }
      else {
         configuration = new Flexmojos3Configuration(flexmojos, interpolator);
      }
   }
   
   public Artifact getGlobalDependency() {
      return globalDependency;
   }
   
   public boolean hasAirGlobal() {
      return "airglobal".equals(globalDependency.getArtifactId());
   }
   
   public boolean isPureActionscript() {
      return false;
      //TODO: Implement
   }
   
   /**
    * Get a list of only the SWC and SWC.RB project dependencies
    * @return
    */
   public List<Artifact> getDependencies() {
      return processedDependencies;
   }
   
   public String getCompilerVersion() {
//      for (Plugin p : mavenProject.getPluginManagement().getPlugins()) {
      for (Plugin p : mavenProject.getBuildPlugins()) {
         for (Dependency d : p.getDependencies()) {
            if ("com.adobe.flex".equals(d.getGroupId()) && "compiler".equals(d.getArtifactId()) && "pom".equals(d.getType())) {
               return d.getVersion();
            }
         }
      }
      return null;
   }
   
   public FlexmojosConfiguration getFlexmojosConfig() {
      return configuration;
   }
   
   private void processDependencies() {
      processedDependencies = new ArrayList<Artifact>();
      
      for (Artifact a : mavenProject.getArtifacts()) {
         if (Packagings.SWC.equals(a.getType()) &&
               ("playerglobal".equals(a.getArtifactId()) || "airglobal".equals(a.getArtifactId()))) {
            globalDependency = a;
         }
         
         if (Packagings.SWC.equals(a.getType()) || Packagings.RB_SWC.equals(a.getType())) {
            processedDependencies.add(a);
         }
      }
   }
   
   private Plugin getFlexmojosPlugin(MavenProject mavenProject) {
      for (Plugin p : mavenProject.getBuild().getPlugins()) {
         if (FLEXMOJOS_GROUPID.equals(p.getGroupId()) &&
               FLEXMOJOS_ARTIFACTID.equals(p.getArtifactId())) {
            return p;
         }
      }
      
      return null;
//      return mavenProject.getPlugin("org.sonatype.flexmojos:flexmojos-maven-plugin");
   }
   
//   private MojoExecution getExecution(IMavenProjectFacade facade, IProgressMonitor monitor) {
//      List<MojoExecution> execs;
//      try {
//         execs = facade.getMojoExecutions(FLEXMOJOS_GROUPID,
//               FLEXMOJOS_ARTIFACTID, monitor, "compile-swf", "compile-swc");
//      }
//      catch (CoreException e) {
//         e.printStackTrace();
//         return null;
//      }
//      
//      if (execs.size() != 1) {
//         return null;
//      }
//      
//      return execs.get(0);
//   }
   
   private ModelObjectInterpolator createInterpolator(IMavenProjectFacade facade, MavenSession session) {
      ModelBuildingRequest request = new DefaultModelBuildingRequest();
      request.setUserProperties(session.getUserProperties());
      request.setSystemProperties(session.getSystemProperties());
      if (!request.getUserProperties().contains("localRepository")) {
         String localRepositoryPath = MavenPlugin.getMaven().getLocalRepositoryPath();
         request.getUserProperties().put("localRepository", localRepositoryPath);
         request.getUserProperties().put("settings.localRepository", localRepositoryPath);
      }
//      mbr.setActiveProfileIds(configurationRequest.getResolverConfiguration().getActiveProfileList());
//      mbr.setProfiles(mavenProject.getActiveProfiles()); //?????
      
      ModelObjectInterpolator interpolator = new ModelObjectInterpolator(
            facade.getMavenProject().getModel(), facade.getProject().getLocation().toFile(),
            request);
      PathTranslator pathTranslator = new DefaultPathTranslator();
      interpolator.setPathTranslator(pathTranslator);
      return interpolator;
   }
   
}
