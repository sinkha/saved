/**
 * 
 */
package com.cdmtech.m2e.flex.sdk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;

import com.adobe.flexbuilder.project.IClassPathEntry;
import com.adobe.flexbuilder.project.IFlexSDKClassPathEntry;
import com.adobe.flexbuilder.project.actionscript.IActionScriptProjectSettings;
import com.adobe.flexbuilder.project.internal.FlexProjectCore;
import com.adobe.flexbuilder.project.sdks.IFlexSDK;
import com.adobe.flexbuilder.project.sdks.IFlexSDKPreferences;
import com.adobe.flexbuilder.util.FlashPlayerVersion;
import com.cdmtech.m2e.flex.Packagings;
import com.cdmtech.m2e.flex.config.MarkedFlexConfiguratorException;
import com.cdmtech.m2e.flex.model.DependencyList;
import com.cdmtech.m2e.flex.model.ExclusionDescriptor;
import com.cdmtech.m2e.flex.model.IPathDescriptor;
import com.cdmtech.m2e.flex.model.LinkType;
import com.cdmtech.m2e.flex.model.ProjectType;

/**
 * An SDK Adapter implementation that uses Flash Builder's SDK
 * preferences to determine which libraries are included in the
 * local SDK.
 * 
 * <p>If a framework SWC is present as a maven dependency that
 * dependency is ignored and the local SDK SWC is used instead.
 * This enables Flash Builder to autoconfigure the RSL urls and
 * source path. Flash Builder also requires that certain libraries
 * (like spark) be referenced locally otherwise it throws errors
 * that the skin is missing.</p>
 * 
 * @author svanhoos
 */
public class LocalSdkAdapter implements ISdkMavenAdapter {
   
   public static final Artifact SDK_PLACEHOLDER = new PlaceholderArtifact();
   
   /**
    * Regex to match a valid flex compiler. Not sure this is correct for
    * all cases. Does the compiler version ever have a "-qualifier"?
    */
   private static Pattern compilerVersionPattern = Pattern.compile("^\\d+\\.\\d+(\\.\\d+)*$");
   
   private IFlexSDK sdk;
   
   /**
    * @throws MarkedFlexConfiguratorException
    * 
    */
   public LocalSdkAdapter(String version) throws MarkedFlexConfiguratorException {
      setupSdk(version);
   }
   
   private void setupSdk(String version) throws MarkedFlexConfiguratorException {
      Matcher m = compilerVersionPattern.matcher(version);
      if (!m.matches()) {
         throw new MarkedFlexConfiguratorException("Invalid Flex SDK version: " + version);
      }
      
      IFlexSDKPreferences prefs = FlexProjectCore.getDefault().getFlexSDKPreferences();
//      IFlexSDKPreferences prefs = FlexBuilderInstallation.getInstalledFlexSDKPreferences();
      
      String[] parts = version.split("\\.");
      String sdkName = "Flex " + parts[0] + "." + parts[1];
      
      if (parts.length > 2 && !parts[2].equals("0")) {
         sdkName += "." + parts[2];
      }

      if (parts.length > 3) {
         sdk = prefs.getItem(sdkName + " (build " + parts[3] + ")");
      }

      if (sdk == null) {
         sdk = prefs.getItem(sdkName);
      }
      
      if (sdk == null) {
         throw new MarkedFlexConfiguratorException("A Flex SDK with the specified version is not registered with Flex Builder: " + version);
      }
   }
   
   @Override
   public String getName() {
      return sdk.getName();
   }
   
   @Override
   public LinkType getDefaultLinkType() {
      return LinkType.fromValue(sdk.getDefaultLinkType());
   }
   
   @Override
   public FlashPlayerVersion getMinFlashPlayerVersion() {
      return sdk.getTargetPlayerVersion();
   }
   
   @Override
   public Set<ExclusionDescriptor> processDependenciesAndExclusions(ProjectType projectType,
         DependencyList mavenDependencies, IActionScriptProjectSettings projectSettings) {
      Set<ExclusionDescriptor> exclusions = new HashSet<ExclusionDescriptor>();   
      
      Map<String, IClassPathEntry> localSdkSwcs = new HashMap<String, IClassPathEntry>();
      
      IFlexSDKClassPathEntry sdkPath = getDefaultSDKEntry(projectSettings);
      IClassPathEntry[] localEntries = sdkPath.getChildLibraries(null);
      Pattern p = Pattern.compile("/([^\\./]+)\\.swc$", Pattern.CASE_INSENSITIVE);
      
      for (IClassPathEntry localEntry : localEntries) {
         String path = localEntry.getValue().replace('\\', '/');
         Matcher m = p.matcher(path);
         
         if (m.find()) {
            localSdkSwcs.put(m.group(1), localEntry);
         }
         else if (path.endsWith("{locale}")) {
            exclusions.add(new ExclusionDescriptor(path, IPathDescriptor.EntryKind.PATH));
         }
      }
      
      // Add default exclusions
      for (IClassPathEntry defaultExclusion : sdkPath.getExcludedEntries()) {
         String path = defaultExclusion.getValue().replace('\\', '/');
         Matcher m = p.matcher(path);
         
         if (m.find()) {
            localSdkSwcs.remove(m.group(1));
            exclusions.add(new ExclusionDescriptor(path));
         }
      }
      
      boolean isFirstSdkItem = true;
      List<Artifact> newDependencies = new ArrayList<Artifact>();
      for (Artifact a : mavenDependencies.getArtifacts()) {
         if (Packagings.SWC.equals(a.getType()) && SDK_GROUP_ID.equals(a.getGroupId())) {
            
            if (localSdkSwcs.containsKey(a.getArtifactId())) {
               // This SDK entry is a member of the local SDK
               // Remove it from the dependency list because Flash Builder will include
               // it automatically
               
               if (isFirstSdkItem) {
                  newDependencies.add(SDK_PLACEHOLDER);
                  isFirstSdkItem = false;
               }
               
               localSdkSwcs.remove(a.getArtifactId());
            }
            else {
               // Entry in the SDK group that's not part of the local SDK
               // Not sure if this can even happen, but there's nothing special to
               // do in this case.
               newDependencies.add(a);
            }
            
         }
         else {
            newDependencies.add(a);
         }
      }
      
      mavenDependencies.setArtifacts(newDependencies);
      
      for (IClassPathEntry localEntry : localSdkSwcs.values()) {
         // This entry from the SDK was not found in the maven dependencies so exclude it
         String path = localEntry.getValue().replace('\\', '/');
         exclusions.add(new ExclusionDescriptor(path));
      }
      
      return exclusions;
      
//      Map<String, Artifact> swcDependencies = new HashMap<String, Artifact>();
//      for (Artifact a : mavenDependencies) {
//         if (Packagings.SWC.equals(a.getType()) && SDK_GROUP_ID.equals(a.getGroupId())) {
//            swcDependencies.put(a.getArtifactId(), a);
//         }
//      }
//      
//      //ClassPathEntryFactory.newFlexSDKEntry(projectSettings);
//      IFlexSDKClassPathEntry sdkPath = getDefaultSDKEntry(projectSettings);
//      IClassPathEntry[] localEntries = sdkPath.getChildLibraries(null);
//      
//      Pattern p = Pattern.compile("/([^\\./]+)\\.swc$", Pattern.CASE_INSENSITIVE);
//      
//      for (IClassPathEntry localEntry : localEntries) {
//         String path = localEntry.getValue().replace('\\', '/');
//         Matcher m = p.matcher(path);
//         
//         if (m.find()) {
//            Artifact a = swcDependencies.get(m.group(1));
//            if (a == null) {
//               //TODO: Pretty sure link type for an exclusion doesn't matter
//               exclusions.add(new ExclusionDescriptor(path));
//            }
//            else {
//               mavenDependencies.remove(a);
//            }
//         }
//         else if (path.endsWith("{locale}")) {
//            exclusions.add(new ExclusionDescriptor(path, IPathDescriptor.EntryKind.PATH));
//         }
//      }
//      
//      //TODO: Do we want to just exclude all the defaults regardless of maven dependencies??
//      for (IClassPathEntry defaultExclusion : sdkPath.getExcludedEntries()) {
//         String path = defaultExclusion.getValue().replace('\\', '/');
//         Matcher m = p.matcher(path);
//         
//         if (m.find()) {
//            Artifact a = swcDependencies.get(m.group(1));
//            if (a == null) {
//               exclusions.add(new ExclusionDescriptor(path));
//            }
//            else {
//               mavenDependencies.remove(a);
//            }
//         }
//      }
//      
//      return exclusions;
   }
   
   private IFlexSDKClassPathEntry getDefaultSDKEntry(IActionScriptProjectSettings settings) {
      for (IClassPathEntry e : settings.getDefaultLibraryPath()) {
         if (e instanceof IFlexSDKClassPathEntry) {
            return (IFlexSDKClassPathEntry) e;
         }
      }
      return null;
   }

}
