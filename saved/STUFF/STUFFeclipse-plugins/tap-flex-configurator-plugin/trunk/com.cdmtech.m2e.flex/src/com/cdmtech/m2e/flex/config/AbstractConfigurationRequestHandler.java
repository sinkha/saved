package com.cdmtech.m2e.flex.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Resource;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.IMavenProjectRegistry;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.flexbuilder.project.FlexProjectManager;
import com.adobe.flexbuilder.project.IClassPathEntry;
import com.adobe.flexbuilder.project.PathVariableResolver;
import com.adobe.flexbuilder.project.actionscript.IActionScriptProject;
import com.adobe.flexbuilder.project.actionscript.IActionScriptProjectSettings;
import com.adobe.flexbuilder.project.actionscript.IMutableActionScriptProjectSettings;
import com.adobe.flexbuilder.project.actionscript.internal.ActionScriptProjectSettings;
import com.adobe.flexbuilder.util.FlashPlayerVersion;
import com.adobe.flexbuilder.util.PathUtils;
import com.cdmtech.m2e.flex.MarkerManager;
import com.cdmtech.m2e.flex.MarkerManager.MarkerID;
import com.cdmtech.m2e.flex.Natures;
import com.cdmtech.m2e.flex.Packagings;
import com.cdmtech.m2e.flex.model.ClassPathDescriptor;
import com.cdmtech.m2e.flex.model.DependencyList;
import com.cdmtech.m2e.flex.model.ExclusionDescriptor;
import com.cdmtech.m2e.flex.model.LinkType;
import com.cdmtech.m2e.flex.model.PathClassPathEntryDescriptor;
import com.cdmtech.m2e.flex.model.ProjectType;
import com.cdmtech.m2e.flex.model.SdkClassPathEntryDescriptor;
import com.cdmtech.m2e.flex.pom.CompilerDefine;
import com.cdmtech.m2e.flex.pom.FlexmojosConfiguration;
import com.cdmtech.m2e.flex.pom.FlexmojosConfiguration.ResourceBundleStyle;
import com.cdmtech.m2e.flex.sdk.ISdkMavenAdapter;
import com.cdmtech.m2e.flex.sdk.LocalSdkAdapter;

/**
 * @author svanhoos
 *
 */
public abstract class AbstractConfigurationRequestHandler implements IConfigurationDelegate {
   
   private static Logger logger = LoggerFactory.getLogger(AbstractConfigurationRequestHandler.class);
   
   protected ProjectConfigurationRequest configurationRequest;
   protected IProgressMonitor monitor;
   protected MarkerManager markerManager;
   
   private final IMavenProjectRegistry projectRegistry;
   @SuppressWarnings("unused")
   private IMavenProjectFacade mavenProjectFacade;
   protected MavenProject mavenProject;
   protected IProject project;
   private ProjectType projectType;
   
   protected ProjectConfigurationInfo configInfo;
   protected FlexmojosConfiguration flexmojos;
   
   public AbstractConfigurationRequestHandler(IMavenProjectFacade facade, ProjectType projectType,
         ProjectConfigurationInfo configInfo, IProgressMonitor monitor) {
      
      this.mavenProjectFacade = facade;
      this.mavenProject = facade.getMavenProject();
      this.project = facade.getProject();
      this.projectType = projectType;
      this.configInfo = configInfo;
      this.flexmojos = configInfo.getFlexmojosConfig();
      this.monitor = monitor;
      
      this.markerManager = new MarkerManager();
      this.projectRegistry = MavenPlugin.getMavenProjectRegistry();
   }
   
   protected IMavenProjectRegistry getProjectRegistry() {
      return projectRegistry;
   }
   
   @Override
   public void createAllConfiguration() throws CoreException {
      IProjectDescription description = project.getDescription();
      Set<String> natures = new HashSet<String>(Arrays.asList(description.getNatureIds()));
      
      markerManager.removeMarkersOn(project, MarkerID.FLEX_CONFIGURATOR_PROBLEM);
      
      try {
         // Update project natures
         updateNatures(natures);
         description.setNatureIds(natures.toArray(new String[natures.size()]));
         
         // Create Flex Builder project settings
         IMutableActionScriptProjectSettings settings = createProjectSettings();
         
         // Create adapter to get SDK information
         ISdkMavenAdapter sdk = new LocalSdkAdapter(configInfo.getCompilerVersion());

         // Create Flex Builder configuration files
         List<Artifact> dependencies = configInfo.getDependencies();
         ClassPathDescriptor classpathDescriptor = createClasspath(dependencies, sdk, settings);
         createProjectFiles(settings, sdk, classpathDescriptor);
         
         // Set the description on the project so changes to it
         // take effect
         project.setDescription(description, monitor);
      }
      catch (MarkedFlexConfiguratorException ex) {
         logger.info("Project configuration failed.", ex);
         markerManager.addGeneralProblem(project, ex.getMessage(), "Project");
      }
      catch (FlexConfiguratorException ex) {
         logger.info("Project configuration failed.", ex);
      }
   }
   
   public void updateBuildPath() {
      IActionScriptProject asProject = FlexProjectManager.getActionScriptOrFlexProject(project.getName());
      if (asProject == null) {
         System.out.println("no project");
      }
      else {
         try {
            ISdkMavenAdapter sdk = new LocalSdkAdapter(configInfo.getCompilerVersion());
            
            IMutableActionScriptProjectSettings s = asProject.getProjectSettingsClone();
            updateBuildPath(s, createClasspath(configInfo.getDependencies(), sdk, s));
            
            asProject.setProjectDescription(s, monitor);
            
         } catch (MarkedFlexConfiguratorException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         } catch (FlexConfiguratorException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         } catch (CoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }
   }
   
   protected void updateNatures(Set<String> natures) {
      if (natures.remove(Natures.JAVA)) {
         logger.debug("Removed Java project nature.");
      }
      
      IFile classpathFile = project.getFile(".classpath");
      if (classpathFile.exists()) {
         try {
            classpathFile.delete(true, monitor);
            logger.debug("Deleted .classpath from project.");
         }
         catch (CoreException ex) {
            logger.error("Unable to delete .classpath", ex);
         }
      }
   }
   
   private ClassPathDescriptor createClasspath(List<Artifact> mavenDependencies, ISdkMavenAdapter sdk, IActionScriptProjectSettings projectSettings) throws FlexConfiguratorException {
      DependencyList dependencies = new DependencyList(mavenDependencies);
      ClassPathDescriptor cp = new ClassPathDescriptor();
      
      List<Artifact> resourceBundles = processResourceBundles(cp, dependencies.getArtifacts());
      dependencies.getArtifacts().addAll(resourceBundles);
      
      SdkClassPathEntryDescriptor sdkEntry = createSdkEntry(dependencies, sdk, projectSettings);
      
      populateClasspath(cp, dependencies.getArtifacts(), sdkEntry);
      
      return cp;
   }
   
   protected List<Artifact> getFlexDependencies() {
      List<Artifact> dependencies = new ArrayList<Artifact>();
      
      for (Artifact a : mavenProject.getArtifacts()) {
         if ("playerglobal".equals(a.getArtifactId()) || "airglobal".equals(a.getArtifactId())) {
            continue;
         }
         
         if (Packagings.SWC.equals(a.getType()) || Packagings.RB_SWC.equals(a.getType())) {
            dependencies.add(a);
         }
      }
      return dependencies;
   }
   
   protected void populateClasspath(ClassPathDescriptor cp, List<Artifact> dependencies, SdkClassPathEntryDescriptor sdkEntry) {
      for (Artifact a : dependencies) {
         if ("playerglobal".equals(a.getArtifactId()) || "airglobal".equals(a.getArtifactId())) {
            continue;
         }
         
         if (a == LocalSdkAdapter.SDK_PLACEHOLDER) {
            cp.addEntry(sdkEntry);
            continue;
         }
         
         
//         IMavenProjectFacade dependencyFacade = null;
//         for (IMavenProjectFacade project : getProjectRegistry().getProjects()) {
////            ArtifactKey projectKey = project.getArtifactKey();
//            
//            MavenProject p2 = project.getMavenProject();
//            Artifact omg = p2.getArtifact();
//            
//            ArtifactKey artifactKey = new ArtifactKey(a);
//            ArtifactKey projectKey = new ArtifactKey(omg);
//            
//            if (artifactKey.equals(projectKey)) {
////            if (a.equals(omg)){
////            if (projectKey.equals(new ArtifactKey(a))){
//               dependencyFacade = project;
//               break;
//            }
//         }
         
         // Copied mostly from JDT
         // Skip current project? Not sure...
         IMavenProjectFacade dependencyFacade = getProjectRegistry().getMavenProject(
               a.getGroupId(), a.getArtifactId(), a.getVersion());
         if (dependencyFacade != null && dependencyFacade.getProject().equals(project)) {
            continue;
         }
         
         LinkType linkType = getLinkType(a);
         if (dependencyFacade != null && dependencyFacade.getFullPath(a.getFile()) != null) {
            // Found artifact within a workspace project
            cp.addProjectDependency(a, linkType, dependencyFacade);
         }
         else if (a.getFile() != null) {
            // Found regular maven dependency
            cp.addLibraryDependency(a, linkType);
         }
      }
   }

   protected LinkType getLinkType(Artifact a) {
      LinkType linkType = LinkType.parseScope(a.getScope());
      if (Packagings.SWC.equals(a.getType()) && linkType == null) {
         return getDefaultLinkType();
      }
      return linkType;
   }
   
   protected LinkType getDefaultLinkType() {
      return null;
   }
   
   protected List<Artifact> processResourceBundles(ClassPathDescriptor cp, List<Artifact> dependencies) throws FlexConfiguratorException {
      List<Artifact> specResourceBundles = new ArrayList<Artifact>();
      Iterator<Artifact> iter = dependencies.iterator();
      Set<String> locales = getLocales();
      IMaven maven = MavenPlugin.getMaven();
      
      while(iter.hasNext()) {
         Artifact a = iter.next();
         if (Packagings.RB_SWC.equals(a.getType())) {
            
            for (String locale : locales) {
               try {
                  Artifact sa = maven.resolve(a.getGroupId(), a.getArtifactId(), a.getVersion(),
                        a.getType(), locale, null, monitor);
                  specResourceBundles.add(sa);
               }
               catch (CoreException ex) {
                  throw new FlexConfiguratorException("Unable to resolve resource bundle.", ex);
               }
            }
            
            iter.remove();
         }
      }
      
      return specResourceBundles;
   }
   
   protected SdkClassPathEntryDescriptor createSdkEntry(DependencyList dependencies,
         ISdkMavenAdapter sdk, IActionScriptProjectSettings projectSettings) {
      SdkClassPathEntryDescriptor d = new SdkClassPathEntryDescriptor();
      d.setPath("");
//      d.setLinkType(LinkType.fromValue(projectSettings.getDefaultLinkType()));
      //TODO: Set link type from sdk adapter?
      d.setExcludedEntries(new HashSet<ExclusionDescriptor>(sdk.processDependenciesAndExclusions(
            projectType, dependencies, projectSettings)));
      return d;
   }
   
   protected void createProjectFiles(IMutableActionScriptProjectSettings settings, ISdkMavenAdapter sdk, ClassPathDescriptor classpath) throws FlexConfiguratorException {
      configureProjectSettings(settings, sdk, classpath);
      
      ((ActionScriptProjectSettings) settings).saveDescription(project, monitor);
   }
   
   /**
    * Create the ActionScriptProjectSettings object (or a subtype of it)
    * 
    * @return
    */
   protected abstract IMutableActionScriptProjectSettings createProjectSettings();
   
//   private IPath makeRelativeOutputPath(IPath outputPath, boolean toProjectOnly, IMutableActionScriptProjectSettings settings) {
//      IPath path = outputPath;
//      if (!path.isAbsolute()) {
//         return path;
//      }
//      
//      IPath relativeToPath = settings.getProjectLocation();
//      path = PathUtils.absoluteToRelative(path, relativeToPath);
//      if (!toProjectOnly) {
//         path = PathVariableResolver.makePathVariableRelative(path, settings, true);
//      }
//      
//      return path;
//   }
   
   protected IPath getMainSourceDirectory() {
      return makeRelativePath(mavenProject.getBuild().getSourceDirectory());
   }
   
   /**
    * Configure the ActionScriptProjectSettings
    * 
    * @param s
    * @param sdk TODO
    * @param classpath
    * @throws FlexConfiguratorException
    */
   protected void configureProjectSettings(IMutableActionScriptProjectSettings s,
         ISdkMavenAdapter sdk, ClassPathDescriptor classpath) throws FlexConfiguratorException {
      
      s.setFlexSDKName(sdk.getName());
      
      s.setMainSourceFolder(getMainSourceDirectory());
      
      // Dependencies & Library Path
      s.setDefaultLinkType(getLibraryPathDefaultLinkType().getValue());
      s.setLibraryPath(classpath.toFlexClasspath(s));

      // Custom output folder
      updateOutputDirectory(s);
      
      // Root URL for output directory
      if (flexmojos.getRootUrl() != null) {
         s.setRootURL(flexmojos.getRootUrl());
      }
      
      // Target player version
      String targetPlayerStr = flexmojos.getTargetPlayer();
      if (targetPlayerStr != null) {
         FlashPlayerVersion targetPlayer = new FlashPlayerVersion(targetPlayerStr);
         
         if (targetPlayer.compareTo(sdk.getMinFlashPlayerVersion()) < 0) {
            throw new MarkedFlexConfiguratorException("The specified target player version (" +
                  targetPlayerStr + ") is less than the minimum for the selected SDK.");
         }
         
         //TODO: handle bad version
         //TODO: set marker on pom @ correct line
         s.setTargetPlayerVersion(targetPlayer);
      }
      
      // Compilation flags
      s.setGenerateAccessibleSWF(flexmojos.getAccessible());
      s.setStrictCompile(flexmojos.getStrict());
      s.setVerifyDigests(flexmojos.getVerifyDigests());
      s.setShowWarnings(flexmojos.getShowWarnings());
      
      // Source directories
      Set<String> sourceRoots = getRelativeSourceFolders();
      IClassPathEntry[] sourceEntries = new IClassPathEntry[sourceRoots.size()];
      int i = 0;
      for (String root : sourceRoots) {
         sourceEntries[i++] = new PathClassPathEntryDescriptor(root, LinkType.INTERNAL)
               .toFlexEntry(s);
      }
      s.setSourcePath(sourceEntries);
      
      // Additional compiler arguments
      List<String> additionalArgs = new ArrayList<String>();
      populateAdditionalCompilerArguments(additionalArgs);
      s.setAdditionalCompilerArgs(join(additionalArgs));
   }
   
   private void updateOutputDirectory(IMutableActionScriptProjectSettings s) {
      String ideOutputFolderPathStr = flexmojos.getIdeOutputFolderPath();
      IPath absPath;
      if (ideOutputFolderPathStr.startsWith("\\\\")) {
         absPath = new Path(null, ideOutputFolderPathStr);
      }
      else {
         absPath = new Path(ideOutputFolderPathStr);
      }
      absPath = PathVariableResolver.resolvePath(absPath, s);
      
      IPath relPath = absPath;
      if (relPath.isAbsolute()) {
         relPath = PathUtils.absoluteToRelative(relPath, s.getProjectLocation());
         relPath = PathVariableResolver.makePathVariableRelative(relPath, s, true);
      }
      
      boolean isPathVariable = PathVariableResolver.resolvePath(relPath, s) != relPath;
      
      if (isPathVariable) {
         s.setOutputFolder(PathVariableResolver.unwrapPath(relPath, s), "bin-debug");
      }
      else if (!relPath.isAbsolute()) {
         IResource member = project.findMember(relPath);
         if (member != null && member.isLinked()) {
            s.setOutputFolder(member.getLocation(), member.getName());
         }
         else {
            s.setOutputFolder(relPath);
         }
      }
      else {
         s.setOutputFolder(absPath, "bin-debug");
      }
   }
   
   protected void updateBuildPath(IMutableActionScriptProjectSettings s, ClassPathDescriptor classpath) {
      s.setLibraryPath(classpath.toFlexClasspath(s));
   }
   
   protected void populateAdditionalCompilerArguments(List<String> args) {
      StringBuilder sb = new StringBuilder("-locale");
      for (String l : getLocales()) {
         sb.append(" ");
         sb.append(l);
      }
      args.add(sb.toString());
      
      String servicesPath = null;
      if (flexmojos.getServices() != null) {
         File f = new File(flexmojos.getServices());
         if (f.exists()) {
            try {
               servicesPath = f.getCanonicalPath();
            }
            catch (IOException ex) {
               servicesPath = f.getAbsolutePath();
               logger.warn("Bad services path", ex);
            }
         }
      }
      else {
      
         //TODO: Can this be improved to only look once per resource directory?
         for (Resource r : mavenProject.getBuild().getResources()) {
            File cfg = new File(r.getDirectory(), "services-config.xml");
            if (cfg.exists()) {
               servicesPath = cfg.getAbsolutePath();
               break;
            }
         }
      }
      
      if (servicesPath != null) {
         args.add("-services " + quoteIfNecessary(servicesPath));
      }
      
      args.add("--incremental");
      
      List<CompilerDefine> flexmojosDefines = flexmojos.getDefines();
      if (flexmojosDefines != null) {
         for (CompilerDefine cd : flexmojosDefines) {
            args.add("-define+=" + cd.name + "," + cd.value);
         }
      }
      
      if (flexmojos.getContextRoot() != null) {
         args.add("-context-root " + flexmojos.getContextRoot());
      }
      
      for (String configFile : getConfigFiles()) {
         IPath configFilePath = pathFromString(configFile).makeRelativeTo(getMainSourceDirectory());
         args.add("-load-config+=" + quoteIfNecessary(configFilePath.toString()));
      }
   }
   
   protected List<String> getConfigFiles() {
      List<String> configFiles = configInfo.getFlexmojosConfig().getConfigFiles();
      if (configFiles == null) {
         configFiles = new ArrayList<String>();
      }
      return configFiles;
   }
   
   protected Set<String> getSourceRoots() {
      Set<String> sourceFolders = new HashSet<String>();
      List<String> sourceRoots;
      
      if (mavenProject.getExecutionProject() != null) {
         sourceRoots = mavenProject.getExecutionProject().getCompileSourceRoots();
      }
      else {
         sourceRoots = mavenProject.getCompileSourceRoots();
      }
      sourceFolders.addAll(sourceRoots);
      
      List<String> testRoots;
      if (mavenProject.getExecutionProject() != null) {
         testRoots = mavenProject.getExecutionProject().getTestCompileSourceRoots();
      }
      else {
         testRoots = mavenProject.getTestCompileSourceRoots();
      }
      sourceFolders.addAll(testRoots);
      
      for (Resource r : mavenProject.getBuild().getResources()) {
         sourceFolders.add(r.getDirectory());
      }
      for (Resource r : mavenProject.getBuild().getTestResources()) {
         sourceFolders.add(r.getDirectory());
      }
      
      for (Iterator<String> iterator = sourceFolders.iterator(); iterator.hasNext();) {
         String path = iterator.next();
         if (!new File(path).exists()) {
            iterator.remove();
         }
      }
      
      //TODO: Logic for runtime resource bundles??
      if (flexmojos.getMergeResourceBundle() != ResourceBundleStyle.NO_GENERATION
            || (flexmojos.getLocalesCompiled() != null && flexmojos.getLocalesCompiled().size() > 0)) {
         sourceFolders.add(flexmojos.getLocalesSourcePath());
      }
      
      return sourceFolders;
   }
   
   protected Set<String> getRelativeSourceFolders() {
      Set<String> sourceRoots = getSourceRoots();

      Set<String> sources = new HashSet<String>();
      for ( String sourceRoot : sourceRoots )
      {
          File source = new File( sourceRoot );
          if ( source.isAbsolute() )
          {
             IPath loc = Path.fromOSString(source.getAbsolutePath());
             loc = loc.makeRelativeTo(project.getLocation());
             
             sources.add(loc.toString());
          }
          else
          {
              sources.add( sourceRoot );
          }
      }

      return sources;
   }
   
   protected IPath pathFromString(String str) {
      File loc = new File(str);
      
      if (loc.isAbsolute()) {
         return Path.fromOSString(loc.getAbsolutePath());
      }
      else {
          return project.getFolder(str).getLocation();
      }
   }
   
   protected IPath makeRelativePath(String pathStr) {
      File loc = new File(pathStr);
      
      if (loc.isAbsolute()) {
         IPath path = Path.fromOSString(loc.getAbsolutePath());
         path = path.makeRelativeTo(project.getLocation());
         
         return path;
      }
      else {
          return project.getFolder(pathStr).getLocation();
      }
   }
   
   protected Artifact resolveFlexFrameworkArtifact() {
      for (Artifact a : mavenProject.getArtifacts()) {
         if ("com.adobe.flex.framework".equals(a.getGroupId())
               && "framework".equals(a.getArtifactId()) && "swc".equals(a.getType())) {
            return a;
         }
      }
      return null;
   }
   
   protected LinkType getLibraryPathDefaultLinkType() throws FlexConfiguratorException {
      Artifact a = resolveFlexFrameworkArtifact();
      if (a == null) {
         throw new MarkedFlexConfiguratorException(
               "Could not find Flex Framework! Not included as dependency!");
      }
      return "rsl".equals(a.getScope()) ? LinkType.RSL : LinkType.INTERNAL;
   }
   
   protected Set<String> getLocales() {
      Set<String> locales = new HashSet<String>();
      
      if (flexmojos.getLocalesCompiled() != null) {
         locales.addAll(flexmojos.getLocalesCompiled());
      }
      //else runtime locales?
      //TODO: runtime
      
      if (locales.isEmpty()) {
         locales.add(flexmojos.getDefaultLocale());
      }
      
      return locales;
   }
   
   protected String join(Collection<String> strings) {
      return join(strings, " ", false);
   }
   
   protected String join(Collection<String> strings, String separator, boolean quote) {
      StringBuilder sb = new StringBuilder();
      for (String s : strings) {
         if (sb.length() > 0) {
            sb.append(separator);
         }
         if (quote) 
            sb.append("\"").append(s).append("\"");
         else
            sb.append(s);
      }
      return sb.toString();
   }
   
   protected String quoteIfNecessary(String path) {
      if (path.contains(" ")) {
         return "\"" + path + "\"";
      }
      return path;
   }
}
