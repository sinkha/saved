/**
 * 
 */
package com.cdmtech.m2e.flex.sdk;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.maven.artifact.Artifact;

import com.adobe.flexbuilder.project.actionscript.IActionScriptProjectSettings;
import com.adobe.flexbuilder.util.FlashPlayerVersion;
import com.cdmtech.m2e.flex.model.DependencyList;
import com.cdmtech.m2e.flex.model.ExclusionDescriptor;
import com.cdmtech.m2e.flex.model.IClassPathEntryDescriptor;
import com.cdmtech.m2e.flex.model.LibraryClassPathEntryDescriptor;
import com.cdmtech.m2e.flex.model.LinkType;
import com.cdmtech.m2e.flex.model.PathClassPathEntryDescriptor;
import com.cdmtech.m2e.flex.model.ProjectType;

/**
 * A dumb implementation of ISdkMavenAdapter that functions the same
 * as flexmojos version 3. It simply excludes most of the normal
 * framework SWCs so only framework libraries added to the pom will
 * be included in the path.
 * 
 * @author svanhoos
 *
 */
public class IgnoreSdkAdapter implements ISdkMavenAdapter {
   
   @Override
   public String getName() {
      return null;
   }
   
   @Override
   public LinkType getDefaultLinkType() {
      return LinkType.INTERNAL;
   }

   @Override
   public FlashPlayerVersion getMinFlashPlayerVersion() {
      return null;
   }

   @Override
   public Set<ExclusionDescriptor> processDependenciesAndExclusions(ProjectType projectType,
         DependencyList mavenDependencies, IActionScriptProjectSettings projectSettings) {
      Set<IClassPathEntryDescriptor> excludes = new HashSet<IClassPathEntryDescriptor>();

      excludes.add(new PathClassPathEntryDescriptor("${PROJECT_FRAMEWORKS}/locale/{locale}",
            LinkType.INTERNAL));
      excludes.add(new LibraryClassPathEntryDescriptor("${PROJECT_FRAMEWORKS}/libs/flex.swc",
            LinkType.INTERNAL));
      excludes.add(new LibraryClassPathEntryDescriptor("${PROJECT_FRAMEWORKS}/libs/qtp.swc",
            LinkType.INTERNAL));
      excludes.add(new LibraryClassPathEntryDescriptor("${PROJECT_FRAMEWORKS}/libs/rpc.swc",
            LinkType.INTERNAL));
      excludes.add(new LibraryClassPathEntryDescriptor("${PROJECT_FRAMEWORKS}/libs/framework.swc",
            LinkType.CROSS_DOMAIN_RSL, true));
      excludes.add(new LibraryClassPathEntryDescriptor(
            "${PROJECT_FRAMEWORKS}/libs/datavisualization.swc", LinkType.INTERNAL));
      excludes.add(new LibraryClassPathEntryDescriptor("${PROJECT_FRAMEWORKS}/libs/automation.swc",
            LinkType.INTERNAL));
      excludes.add(new LibraryClassPathEntryDescriptor(
            "${PROJECT_FRAMEWORKS}/libs/automation_dmv.swc", LinkType.INTERNAL));
      excludes.add(new LibraryClassPathEntryDescriptor(
            "${PROJECT_FRAMEWORKS}/libs/automation_flashflexkit.swc", LinkType.INTERNAL));
      excludes.add(new LibraryClassPathEntryDescriptor("${PROJECT_FRAMEWORKS}/libs/utilities.swc",
            LinkType.INTERNAL));
      excludes.add(new LibraryClassPathEntryDescriptor(
            "${PROJECT_FRAMEWORKS}/libs/automation_agent.swc", LinkType.INTERNAL));
      
      excludes.add(new LibraryClassPathEntryDescriptor(
            "${PROJECT_FRAMEWORKS}/libs/flash-integration.swc", LinkType.INTERNAL));
      excludes.add(new LibraryClassPathEntryDescriptor(
            "${PROJECT_FRAMEWORKS}/libs/spark.swc", LinkType.INTERNAL));
//      excludes.add(new LibraryClassPathEntryDescriptor(
//            "${PROJECT_FRAMEWORKS}/libs/sparkskins.swc", LinkType.INTERNAL));
      excludes.add(new LibraryClassPathEntryDescriptor(
            "${PROJECT_FRAMEWORKS}/libs/osmf.swc", LinkType.INTERNAL));
      excludes.add(new LibraryClassPathEntryDescriptor(
            "${PROJECT_FRAMEWORKS}/libs/textLayout.swc", LinkType.INTERNAL));

      // Convert to the new exclude set format
      // Didn't want to change the above code yet in case I need the extra
      // link type information later
      Set<ExclusionDescriptor> excludesNewFormat = new HashSet<ExclusionDescriptor>();
      for (IClassPathEntryDescriptor d : excludes) {
         excludesNewFormat.add(new ExclusionDescriptor(d.getPath()));
      }
      
      return excludesNewFormat;
   }

}
