/**
 * 
 */
package com.cdmtech.m2e.flex.sdk;

import java.util.Collection;
import java.util.Set;

import org.apache.maven.artifact.Artifact;

import com.adobe.flexbuilder.project.actionscript.IActionScriptProjectSettings;
import com.adobe.flexbuilder.util.FlashPlayerVersion;
import com.cdmtech.m2e.flex.model.DependencyList;
import com.cdmtech.m2e.flex.model.ExclusionDescriptor;
import com.cdmtech.m2e.flex.model.LinkType;
import com.cdmtech.m2e.flex.model.ProjectType;

/**
 * @author svanhoos
 *
 */
public interface ISdkMavenAdapter {

   public static final String SDK_GROUP_ID = "com.adobe.flex.framework";
   
   /**
    * Name of the SDK in "Installed Flex SDKs" format.
    */
   String getName();
   
   /**
    * Get this SDK's default library link type.
    * @return
    */
   LinkType getDefaultLinkType();
   
   /**
    * Get the minimum flash player version required to use
    * this SDK.
    */
   FlashPlayerVersion getMinFlashPlayerVersion();
   
   /**
    * Get the list of local SDK libraries that should be excluded from the build.
    * 
    * @param projectType
    * @param mavenDependencies
    * @param projectSettings TODO
    * @return
    */
   Set<ExclusionDescriptor> processDependenciesAndExclusions(ProjectType projectType,
         DependencyList mavenDependencies, IActionScriptProjectSettings projectSettings);
}
