/**
 * 
 */
package com.cdmtech.m2e.flex.sdk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;

import com.adobe.flexbuilder.project.actionscript.IActionScriptProjectSettings;
import com.adobe.flexbuilder.util.FlashPlayerVersion;
import com.cdmtech.m2e.flex.Packagings;
import com.cdmtech.m2e.flex.model.DependencyList;
import com.cdmtech.m2e.flex.model.ExclusionDescriptor;
import com.cdmtech.m2e.flex.model.LinkType;
import com.cdmtech.m2e.flex.model.ProjectType;
import com.cdmtech.m2e.flex.sdk.fm.ProjectLinkTypeEntry;
import com.cdmtech.m2e.flex.sdk.fm.ProjectLinkTypeMap;
import com.cdmtech.m2e.flex.sdk.fm.SDKEntryDescriptor;

/**
 * SDK Descriptor implementation that copies the functionality from
 * llinder's version of flexmojos.
 * 
 * @author svanhoos
 *
 */
public class StaticSdkAdapter implements ISdkMavenAdapter {
   
   public static enum Version {
      FLEX3_0_0("3.0.0"),
      FLEX3_1_0("3.1.0"),
      FLEX3_2_0("3.2.0"),
      FLEX3_3_0("3.3.0"),
      FLEX3_4_0("3.4.0"),
      FLEX3_5_0("3.5.0"),
      FLEX4_0_0("4.0.0"),
      FLEX4_1_0("4.1.0"),
      FLEX4_5_0("4.5.0");
      
      private final String version;
      
      public static Version valueFrom(String version) {
         for (int i = 0; i < Version.values().length; i++) {
            Version v = Version.values()[i];
            if (v.getVersion().equals(version)) {
               return v;
            }
         }

         return null;
      }

      private Version(String version) {
         this.version = version;
      }

      public String getVersion() {
         return this.version;
      }
   }
   
   private Version version;
   private HashMap<String, SDKEntryDescriptor> entries;
   
   
   /**
    * 
    */
   public StaticSdkAdapter(String versionString) {
      this.version = getBestVersion(versionString);
      
      if (this.version == null) {
         throw new IllegalArgumentException("Unrecoginized Flex SDK version.");
      }
      
      this.entries = createEntries(this.version);
   }
   
   @Override
   public String getName() {
      // TODO Auto-generated method stub
      return null;
   }
   
   @Override
   public LinkType getDefaultLinkType() {
      LinkType type = LinkType.INTERNAL;

      switch (version) {
      case FLEX4_0_0:
         type = LinkType.RSL; // Yes it is RSL and NOT RSL_DIGEST.
         break;
      case FLEX3_5_0:
      case FLEX3_4_0:
      case FLEX3_3_0:
      case FLEX3_2_0:
      case FLEX3_1_0:
      case FLEX3_0_0:
         type = LinkType.INTERNAL;
         break;
      default:
         type = LinkType.INTERNAL;
         break;
      }

      return type;
   }
   
   @Override
   public FlashPlayerVersion getMinFlashPlayerVersion() {
      //TODO: Fix this
      return new FlashPlayerVersion("10.0.0");
   }
   
   /**
    * Returns a list of libraries from the local SDK which aren't included in the
    * list of Maven dependencies. (That way, leaving a flex library out of the pom
    * prevents it from being included in the build.)
    * 
    * <p>Mostly copied from flexmojo's LocalSdk.java</p>
    */
   public List<SDKEntryDescriptor> getSdkExcludes(ProjectType projectType, Collection<Artifact> mavenDependencies) {
      List<SDKEntryDescriptor> includedEntries = new ArrayList<SDKEntryDescriptor>();
      List<SDKEntryDescriptor> excludedEntries = new ArrayList<SDKEntryDescriptor>(entries.values());

      // Loop through and collect all local SDK entries that are in the Maven
      // dependency collection
      for (Artifact dep : mavenDependencies) {
         if (SDK_GROUP_ID.equals(dep.getGroupId()) && Packagings.SWC.equals(dep.getType())) {
            SDKEntryDescriptor localEntry = entries.get(dep.getArtifactId());
            if (localEntry != null && localEntry.isApplicableToProject(projectType)) {
               includedEntries.add(localEntry);
            }
         }
      }
      Collections.sort(includedEntries);

      // Loop through and collect all local SDK entries that are not in the
      // Maven dependency collection
      Iterator<SDKEntryDescriptor> localIter = excludedEntries.iterator();
      while (localIter.hasNext()) {
         SDKEntryDescriptor localEntry = localIter.next();
         if (localEntry == null || includedEntries.contains(localEntry)
               || !localEntry.isApplicableToProject(projectType))
            localIter.remove();
      }
      
      return excludedEntries;
   }
   
   @Override
   public Set<ExclusionDescriptor> processDependenciesAndExclusions(ProjectType projectType,
         DependencyList mavenDependencies, IActionScriptProjectSettings projectSettings) {
      Set<ExclusionDescriptor> excludes = new HashSet<ExclusionDescriptor>();
      
      for (SDKEntryDescriptor q : getSdkExcludes(projectType, mavenDependencies.getArtifacts())) {
         if (q.getPath() != null) {
            excludes.add(new ExclusionDescriptor(q.getPath()));
         }
      }
      return excludes;
   }

   private Version getBestVersion(String version) {
      // Find closest matching configuration.
      Version v = Version.valueFrom(version);
      while (v == null) {
         version = version.substring(0, version.lastIndexOf("."));
         v = Version.valueFrom(version);
      }
      return v;
   }
   
   private HashMap<String, SDKEntryDescriptor> createEntries(Version v) {
      HashMap<String, SDKEntryDescriptor>map = new HashMap<String, SDKEntryDescriptor>();
      
      switch (v) {
      case FLEX4_5_0:
      case FLEX4_1_0:
      case FLEX4_0_0:
         // No longer part of standard dependencies. Path changed.
         if( !map.containsKey( "automation") )
            map.put( "automation",
                  new SDKEntryDescriptor(
                  SDK_GROUP_ID,
                  "automation",
                  "${PROJECT_FRAMEWORKS}/libs/automation/automation.swc",
                  "${PROJECT_FRAMEWORKS}/projects/automation/src",
                  null
                  ) );
         // No longer part of standard dependencies. Path changed.
         if( !map.containsKey( "automation_agent") )
            map.put( "automation_agent",
                  new SDKEntryDescriptor(
                  SDK_GROUP_ID,
                  "automation_agent",
                  "${PROJECT_FRAMEWORKS}/libs/automation/automation_agent.swc",
                  null,
                  null
                  ) );
         // New
         if( !map.containsKey( "automation_air") )
            map.put( "automation_air",
                  new SDKEntryDescriptor(
                  SDK_GROUP_ID,
                  "automation_air",
                  "${PROJECT_FRAMEWORKS}/libs/automation/automation_air.swc",
                  "${PROJECT_FRAMEWORKS}/projects/automation_air/src",
                  null
                  ) );
         // New
         if( !map.containsKey( "automation_airspark") )
            map.put( "automation_airspark",
                  new SDKEntryDescriptor(
                  SDK_GROUP_ID,
                  "automation_airspark",
                  "${PROJECT_FRAMEWORKS}/libs/automation/automation_airspark.swc",
                  "${PROJECT_FRAMEWORKS}/projects/automation_airspark/src",
                  null
                  ) );
         // No longer part of standard dependencies. Path changed.
         if( !map.containsKey( "automation_dmv") )
            map.put( "automation_dmv",
                  new SDKEntryDescriptor(
                  SDK_GROUP_ID,
                  "automation_dmv",
                  "${PROJECT_FRAMEWORKS}/libs/automation/automation_dmv.swc",
                  "${PROJECT_FRAMEWORKS}/projects/automation_dmv/src",
                  null
                  ) );
         // No longer part of standard dependencies. Path changed.
         if( !map.containsKey( "automation_flashflexkit") )
            map.put( "automation_flashflexkit",
                  new SDKEntryDescriptor(
                  SDK_GROUP_ID,
                  "automation_flashflexkit",
                  "${PROJECT_FRAMEWORKS}/libs/automation/automation_flashflexkit.swc",
                  "${PROJECT_FRAMEWORKS}/projects/automation_flashflexkit/src",
                  null
                  ) );
         // New
         if( !map.containsKey( "automation_spark") )
            map.put( "automation_spark",
                  new SDKEntryDescriptor(
                  SDK_GROUP_ID,
                  "automation_spark",
                  "${PROJECT_FRAMEWORKS}/libs/automation/automation_spark.swc",
                  "${PROJECT_FRAMEWORKS}/projects/automation_spark/src",
                  null
                  ) );
         // New
         if( !map.containsKey( "qtp_air") )
            map.put( "qtp_air",
                  new SDKEntryDescriptor(
                  SDK_GROUP_ID,
                  "qtp_air",
                  "${PROJECT_FRAMEWORKS}/libs/automation/qtp_air.swc",
                  null,
                  null
                  ) );
          // No longer part of standard dependencies. Path changed.
         if( !map.containsKey( "qtp") )
            map.put( "qtp",
                  new SDKEntryDescriptor(
                  SDK_GROUP_ID,
                  "qtp",
                  "${PROJECT_FRAMEWORKS}/libs/automation/qtp.swc",
                  null,
                  null
                  ) );
         // Source path changed
         if( !map.containsKey( "datavisualization") )
            map.put( "datavisualization",
                  new SDKEntryDescriptor(
                  SDK_GROUP_ID,
                  "datavisualization",
                  "${PROJECT_FRAMEWORKS}/libs/datavisualization.swc",
                  "${PROJECT_FRAMEWORKS}/projects/datavisualization/src",
                  new ProjectLinkTypeMap(
                        new ProjectLinkTypeEntry( ProjectType.FLEX, LinkType.INTERNAL, 7 ),
                        new ProjectLinkTypeEntry( ProjectType.FLEX_LIBRARY, LinkType.INTERNAL, 7 ),
                        new ProjectLinkTypeEntry( ProjectType.AIR, LinkType.INTERNAL ),
                        new ProjectLinkTypeEntry( ProjectType.AIR_LIBRARY, LinkType.INTERNAL ) )
                  ) );
         // New
         if( !map.containsKey( "flash-integration") )
            map.put( "flash-integration",
                  new SDKEntryDescriptor(
                  SDK_GROUP_ID,
                  "flash-integration",
                  "${PROJECT_FRAMEWORKS}/libs/flash-integration.swc",
                  "${PROJECT_FRAMEWORKS}/projects/flash-integration/src",
                  new ProjectLinkTypeMap(
                        new ProjectLinkTypeEntry( ProjectType.FLEX, LinkType.INTERNAL, 8 ),
                        new ProjectLinkTypeEntry( ProjectType.FLEX_LIBRARY, LinkType.INTERNAL, 8 ),
                        new ProjectLinkTypeEntry( ProjectType.AIR, LinkType.INTERNAL ),
                        new ProjectLinkTypeEntry( ProjectType.AIR_LIBRARY, LinkType.INTERNAL ),
                        new ProjectLinkTypeEntry( ProjectType.ACTIONSCRIPT, LinkType.INTERNAL ) )
                  ) );
         // New
         if( !map.containsKey( "framework") )
            map.put( "framework",
                  new SDKEntryDescriptor(
                  SDK_GROUP_ID,
                  "framework",
                  "${PROJECT_FRAMEWORKS}/libs/framework.swc",
                  "${PROJECT_FRAMEWORKS}/projects/framework/src",
                  new ProjectLinkTypeMap(
                        new ProjectLinkTypeEntry( ProjectType.FLEX, LinkType.CROSS_DOMAIN_RSL, 3 ),
                        new ProjectLinkTypeEntry( ProjectType.FLEX_LIBRARY, LinkType.EXTERNAL, 3 ),
                        new ProjectLinkTypeEntry( ProjectType.AIR, LinkType.INTERNAL ),
                        new ProjectLinkTypeEntry( ProjectType.AIR_LIBRARY, LinkType.EXTERNAL ) )
                  ) );
         // No longer part of the SDK
         if( !map.containsKey( "haloclassic") )
            map.put( "haloclassic", null );
         // New
         if( !map.containsKey( "osmf") )
            map.put( "osmf",
                  new SDKEntryDescriptor(
                  SDK_GROUP_ID,
                  "osmf",
                  "${PROJECT_FRAMEWORKS}/libs/osmf.swc",
                  "${PROJECT_FRAMEWORKS}/projects/osmf/src",
                  new ProjectLinkTypeMap(
                        new ProjectLinkTypeEntry( ProjectType.FLEX, LinkType.CROSS_DOMAIN_RSL, 2 ),
                        new ProjectLinkTypeEntry( ProjectType.FLEX_LIBRARY, LinkType.EXTERNAL, 2 ),
                        new ProjectLinkTypeEntry( ProjectType.AIR, LinkType.INTERNAL ),
                        new ProjectLinkTypeEntry( ProjectType.AIR_LIBRARY, LinkType.EXTERNAL ),
                        new ProjectLinkTypeEntry( ProjectType.ACTIONSCRIPT, LinkType.INTERNAL ) )
                  ) );
         // Changed link type
         if( !map.containsKey( "rpc") )
            map.put( "rpc",
                  new SDKEntryDescriptor(
                  SDK_GROUP_ID,
                  "rpc",
                  "${PROJECT_FRAMEWORKS}/libs/rpc.swc",
                  "${PROJECT_FRAMEWORKS}/projects/rpc/src",
                  new ProjectLinkTypeMap(
                        new ProjectLinkTypeEntry( ProjectType.FLEX, LinkType.CROSS_DOMAIN_RSL, 6 ),
                        new ProjectLinkTypeEntry( ProjectType.FLEX_LIBRARY, LinkType.EXTERNAL, 6 ),
                        new ProjectLinkTypeEntry( ProjectType.AIR, LinkType.INTERNAL ),
                        new ProjectLinkTypeEntry( ProjectType.AIR_LIBRARY, LinkType.EXTERNAL ) )
                  ) );
         // New
         if( !map.containsKey( "spark") )
            map.put( "spark",
                  new SDKEntryDescriptor(
                  SDK_GROUP_ID,
                  "spark",
                  "${PROJECT_FRAMEWORKS}/libs/spark.swc",
                  "${PROJECT_FRAMEWORKS}/projects/spark/src",
                  new ProjectLinkTypeMap(
                        new ProjectLinkTypeEntry( ProjectType.FLEX, LinkType.CROSS_DOMAIN_RSL, 4 ),
                        new ProjectLinkTypeEntry( ProjectType.FLEX_LIBRARY, LinkType.EXTERNAL, 4 ),
                        new ProjectLinkTypeEntry( ProjectType.AIR, LinkType.INTERNAL ),
                        new ProjectLinkTypeEntry( ProjectType.AIR_LIBRARY, LinkType.EXTERNAL ) )
                  ) );
         // New
         if( !map.containsKey( "sparkskins") )
            map.put( "sparkskins",
                  new SDKEntryDescriptor(
                  SDK_GROUP_ID,
                  "sparkskins",
                  "${PROJECT_FRAMEWORKS}/libs/sparkskins.swc",
                  "${PROJECT_FRAMEWORKS}/projects/sparkskins/src",
                  new ProjectLinkTypeMap(
                        new ProjectLinkTypeEntry( ProjectType.FLEX, LinkType.CROSS_DOMAIN_RSL, 5 ),
                        new ProjectLinkTypeEntry( ProjectType.FLEX_LIBRARY, LinkType.EXTERNAL, 5 ),
                        new ProjectLinkTypeEntry( ProjectType.AIR, LinkType.INTERNAL ),
                        new ProjectLinkTypeEntry( ProjectType.AIR_LIBRARY, LinkType.EXTERNAL ) )
                  ) );
         // New
         if( !map.containsKey( "textLayout") )
            map.put( "textLayout",
                  new SDKEntryDescriptor(
                  SDK_GROUP_ID,
                  "textLayout",
                  "${PROJECT_FRAMEWORKS}/libs/textLayout.swc",
                  "${PROJECT_FRAMEWORKS}/projects/textLayout/src",
                  new ProjectLinkTypeMap(
                        new ProjectLinkTypeEntry( ProjectType.FLEX, LinkType.CROSS_DOMAIN_RSL, 1 ),
                        new ProjectLinkTypeEntry( ProjectType.FLEX_LIBRARY, LinkType.EXTERNAL, 1 ),
                        new ProjectLinkTypeEntry( ProjectType.AIR, LinkType.INTERNAL ),
                        new ProjectLinkTypeEntry( ProjectType.AIR_LIBRARY, LinkType.EXTERNAL ) )
                  ) );
         // Index changed.
         if( !map.containsKey( "utilities") )
            map.put( "utilities",
                  new SDKEntryDescriptor(
                  SDK_GROUP_ID,
                  "utilities",
                  "${PROJECT_FRAMEWORKS}/libs/utilities.swc",
                  "${PROJECT_FRAMEWORKS}/projects/utilities/src",
                  new ProjectLinkTypeMap(
                        new ProjectLinkTypeEntry( ProjectType.ACTIONSCRIPT, LinkType.INTERNAL ),
                        new ProjectLinkTypeEntry( ProjectType.FLEX, LinkType.INTERNAL, 9 ),
                        new ProjectLinkTypeEntry( ProjectType.FLEX_LIBRARY, LinkType.INTERNAL, 9 ),
                        new ProjectLinkTypeEntry( ProjectType.AIR, LinkType.INTERNAL ),
                        new ProjectLinkTypeEntry( ProjectType.AIR_LIBRARY, LinkType.INTERNAL ) )
                  ) );
      case FLEX3_5_0:
      case FLEX3_4_0:
         // Datavisulization source now included.
         if( !map.containsKey( "datavisualization") )
            map.put( "datavisualization",
                  new SDKEntryDescriptor(
                  SDK_GROUP_ID,
                  "datavisualization",
                  "${PROJECT_FRAMEWORKS}/libs/datavisualization.swc",
                  "${PROJECT_FRAMEWORKS}/projects/datavisualisation/src",
                  new ProjectLinkTypeMap(
                        new ProjectLinkTypeEntry( ProjectType.FLEX, LinkType.INTERNAL ),
                        new ProjectLinkTypeEntry( ProjectType.FLEX_LIBRARY, LinkType.INTERNAL ),
                        new ProjectLinkTypeEntry( ProjectType.AIR, LinkType.INTERNAL ),
                        new ProjectLinkTypeEntry( ProjectType.AIR_LIBRARY, LinkType.INTERNAL ) )
                  ) );
      case FLEX3_3_0:
      case FLEX3_2_0:
      case FLEX3_1_0:
      case FLEX3_0_0:
         if( !map.containsKey( "airframework") )
            map.put( "airframework",
                  new SDKEntryDescriptor(
                  SDK_GROUP_ID,
                  "airframework",
                  "${PROJECT_FRAMEWORKS}/libs/air/airframework.swc",
                  "${PROJECT_FRAMEWORKS}/projects/airframework/src",
                  new ProjectLinkTypeMap(
                        new ProjectLinkTypeEntry( ProjectType.AIR, LinkType.INTERNAL ),
                        new ProjectLinkTypeEntry( ProjectType.AIR_LIBRARY, LinkType.INTERNAL ) )
                  ) );
         if( !map.containsKey( "airglobal") )
            map.put( "airglobal",
                  new SDKEntryDescriptor(
                  SDK_GROUP_ID,
                  "airglobal",
                  "${PROJECT_FRAMEWORKS}/libs/air/airglobal.swc",
                  null,
                  new ProjectLinkTypeMap(
                        new ProjectLinkTypeEntry( ProjectType.AIR, LinkType.EXTERNAL ),
                        new ProjectLinkTypeEntry( ProjectType.AIR_LIBRARY, LinkType.EXTERNAL ) )
                  ) );
         if( !map.containsKey( "applicationupdater") )
            map.put( "applicationupdater",
                  new SDKEntryDescriptor(
                  SDK_GROUP_ID,
                  "applicationupdater",
                  "${PROJECT_FRAMEWORKS}/libs/air/applicationupdater.swc",
                  "${PROJECT_FRAMEWORKS}/projects/air/ApplicationUpdater/src/ApplicationUpdater",
                  new ProjectLinkTypeMap(
                        new ProjectLinkTypeEntry( ProjectType.AIR, LinkType.INTERNAL ),
                        new ProjectLinkTypeEntry( ProjectType.AIR_LIBRARY, LinkType.INTERNAL ) )
                  ) );
         if( !map.containsKey( "applicationupdater_ui") )
            map.put( "applicationupdater_ui",
                  new SDKEntryDescriptor(
                  SDK_GROUP_ID,
                  "applicationupdater_ui",
                  "${PROJECT_FRAMEWORKS}/libs/air/applicationupdater_ui.swc",
                  "${PROJECT_FRAMEWORKS}/projects/air/ApplicationUpdater/src/ApplicationUpdaterDialogs",
                  new ProjectLinkTypeMap(
                        new ProjectLinkTypeEntry( ProjectType.AIR, LinkType.INTERNAL ),
                        new ProjectLinkTypeEntry( ProjectType.AIR_LIBRARY, LinkType.INTERNAL ) )
                  ) );
         if( !map.containsKey( "automation") )
            map.put( "automation",
                  new SDKEntryDescriptor(
                  SDK_GROUP_ID,
                  "automation",
                  "${PROJECT_FRAMEWORKS}/libs/automation.swc",
                  "${PROJECT_FRAMEWORKS}/projects/automation/src",
                  new ProjectLinkTypeMap(
                        new ProjectLinkTypeEntry( ProjectType.FLEX, LinkType.INTERNAL ),
                        new ProjectLinkTypeEntry( ProjectType.FLEX_LIBRARY, LinkType.INTERNAL ),
                        new ProjectLinkTypeEntry( ProjectType.AIR, LinkType.INTERNAL ),
                        new ProjectLinkTypeEntry( ProjectType.AIR_LIBRARY, LinkType.INTERNAL ) )
                  ) );
         if( !map.containsKey( "automation_agent") )
            map.put( "automation_agent",
                  new SDKEntryDescriptor(
                  SDK_GROUP_ID,
                  "automation_agent",
                  "${PROJECT_FRAMEWORKS}/libs/automation_agent.swc",
                  null,
                  new ProjectLinkTypeMap(
                        new ProjectLinkTypeEntry( ProjectType.FLEX, LinkType.INTERNAL ),
                        new ProjectLinkTypeEntry( ProjectType.FLEX_LIBRARY, LinkType.INTERNAL ),
                        new ProjectLinkTypeEntry( ProjectType.AIR, LinkType.INTERNAL ),
                        new ProjectLinkTypeEntry( ProjectType.AIR_LIBRARY, LinkType.INTERNAL ) )
                  ) );
         if( !map.containsKey( "automation_dmv") )
            map.put( "automation_dmv",
                  new SDKEntryDescriptor(
                  SDK_GROUP_ID,
                  "automation_dmv",
                  "${PROJECT_FRAMEWORKS}/libs/automation_dmv.swc",
                  null,
                  new ProjectLinkTypeMap(
                        new ProjectLinkTypeEntry( ProjectType.FLEX, LinkType.INTERNAL ),
                        new ProjectLinkTypeEntry( ProjectType.FLEX_LIBRARY, LinkType.INTERNAL ),
                        new ProjectLinkTypeEntry( ProjectType.AIR, LinkType.INTERNAL ),
                        new ProjectLinkTypeEntry( ProjectType.AIR_LIBRARY, LinkType.INTERNAL ) )
                  ) );
         if( !map.containsKey( "automation_flashflexkit") )
            map.put( "automation_flashflexkit",
                  new SDKEntryDescriptor(
                  SDK_GROUP_ID,
                  "automation_flashflexkit",
                  "${PROJECT_FRAMEWORKS}/libs/automation_flashflexkit.swc",
                  null,
                  new ProjectLinkTypeMap(
                        new ProjectLinkTypeEntry( ProjectType.FLEX, LinkType.INTERNAL ),
                        new ProjectLinkTypeEntry( ProjectType.FLEX_LIBRARY, LinkType.INTERNAL ),
                        new ProjectLinkTypeEntry( ProjectType.AIR, LinkType.INTERNAL ),
                        new ProjectLinkTypeEntry( ProjectType.AIR_LIBRARY, LinkType.INTERNAL ) )
                  ) );
         if( !map.containsKey( "flex") )
            map.put( "flex",
                  new SDKEntryDescriptor(
                  SDK_GROUP_ID,
                  "flex",
                  "${PROJECT_FRAMEWORKS}/libs/flex.swc",
                  "${PROJECT_FRAMEWORKS}/projects/flex/src",
                  new ProjectLinkTypeMap(
                        new ProjectLinkTypeEntry( ProjectType.ACTIONSCRIPT, LinkType.INTERNAL ) )
                  ) );
         if( !map.containsKey( "framework") )
            map.put( "framework",
                  new SDKEntryDescriptor(
                  SDK_GROUP_ID,
                  "framework",
                  "${PROJECT_FRAMEWORKS}/libs/framework.swc",
                  "${PROJECT_FRAMEWORKS}/projects/framework/src",
                  new ProjectLinkTypeMap(
                        new ProjectLinkTypeEntry( ProjectType.FLEX, LinkType.INTERNAL ),
                        new ProjectLinkTypeEntry( ProjectType.FLEX_LIBRARY, LinkType.EXTERNAL ),
                        new ProjectLinkTypeEntry( ProjectType.AIR, LinkType.INTERNAL ),
                        new ProjectLinkTypeEntry( ProjectType.AIR_LIBRARY, LinkType.EXTERNAL ) )
                  ) );
         if( !map.containsKey( "haloclassic") )
            map.put( "haloclassic",
                  new SDKEntryDescriptor(
                  SDK_GROUP_ID,
                  "haloclassic",
                  "${PROJECT_FRAMEWORKS}/themes/HaloClassic/haloclassic.swc",
                  "${PROJECT_FRAMEWORKS}/projects/haloclassic/src",
                  new ProjectLinkTypeMap(
                        new ProjectLinkTypeEntry( ProjectType.FLEX, LinkType.INTERNAL ),
                        new ProjectLinkTypeEntry( ProjectType.FLEX_LIBRARY, LinkType.EXTERNAL ),
                        new ProjectLinkTypeEntry( ProjectType.AIR, LinkType.INTERNAL ),
                        new ProjectLinkTypeEntry( ProjectType.AIR_LIBRARY, LinkType.EXTERNAL ) )
                  ) );
         if( !map.containsKey( "playerglobal") )
            map.put( "playerglobal",
                  new SDKEntryDescriptor(
                  SDK_GROUP_ID,
                  "playerglobal",
                  null,
                  null,
                  new ProjectLinkTypeMap(
                        new ProjectLinkTypeEntry( ProjectType.FLEX, LinkType.EXTERNAL, 0 ),
                        new ProjectLinkTypeEntry( ProjectType.FLEX_LIBRARY, LinkType.EXTERNAL, 0 ),
                        new ProjectLinkTypeEntry( ProjectType.ACTIONSCRIPT, LinkType.EXTERNAL, 0 ) )
                  ) );
         if( !map.containsKey( "rpc") )
            map.put( "rpc",
                  new SDKEntryDescriptor(
                  SDK_GROUP_ID,
                  "rpc",
                  "${PROJECT_FRAMEWORKS}/libs/rpc.swc",
                  "${PROJECT_FRAMEWORKS}/projects/rpc/src",
                  new ProjectLinkTypeMap(
                        new ProjectLinkTypeEntry( ProjectType.FLEX, LinkType.INTERNAL ),
                        new ProjectLinkTypeEntry( ProjectType.FLEX_LIBRARY, LinkType.EXTERNAL ),
                        new ProjectLinkTypeEntry( ProjectType.AIR, LinkType.INTERNAL ),
                        new ProjectLinkTypeEntry( ProjectType.AIR_LIBRARY, LinkType.EXTERNAL ) )
                  ) );
         if( !map.containsKey( "servicemonitor") )
            map.put( "servicemonitor",
                  new SDKEntryDescriptor(
                  SDK_GROUP_ID,
                  "servicemonitor",
                  "${PROJECT_FRAMEWORKS}/libs/air/servicemonitor.swc",
                  "${PROJECT_FRAMEWORKS}/projects/air/ServiceMonitor/src",
                  new ProjectLinkTypeMap(
                        new ProjectLinkTypeEntry( ProjectType.AIR, LinkType.INTERNAL ),
                        new ProjectLinkTypeEntry( ProjectType.AIR_LIBRARY, LinkType.INTERNAL ) )
                  ) );
         if( !map.containsKey( "utilities") )
            map.put( "utilities",
                  new SDKEntryDescriptor(
                  SDK_GROUP_ID,
                  "utilities",
                  "${PROJECT_FRAMEWORKS}/libs/utilities.swc",
                  "${PROJECT_FRAMEWORKS}/projects/utilities/src",
                  new ProjectLinkTypeMap(
                        new ProjectLinkTypeEntry( ProjectType.ACTIONSCRIPT, LinkType.INTERNAL ),
                        new ProjectLinkTypeEntry( ProjectType.FLEX, LinkType.INTERNAL ),
                        new ProjectLinkTypeEntry( ProjectType.FLEX_LIBRARY, LinkType.INTERNAL ),
                        new ProjectLinkTypeEntry( ProjectType.AIR, LinkType.INTERNAL ),
                        new ProjectLinkTypeEntry( ProjectType.AIR_LIBRARY, LinkType.INTERNAL ) )
                  ) );
      }
      
      return map;
   }
   
}
