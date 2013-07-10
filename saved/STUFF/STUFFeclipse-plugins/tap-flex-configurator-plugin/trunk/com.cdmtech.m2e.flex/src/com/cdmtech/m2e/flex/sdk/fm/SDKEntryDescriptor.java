/**
 * 
 */
package com.cdmtech.m2e.flex.sdk.fm;

import com.cdmtech.m2e.flex.model.ProjectType;

/**
 * @author svanhoos
 *
 */
public class SDKEntryDescriptor implements Comparable<SDKEntryDescriptor> {

   private String groupId;
   private String artifactId;
   private String path;
   private String sourcePath;
   private ProjectLinkTypeMap linkMap;
   
   /**
    * 
    */
   public SDKEntryDescriptor(String groupId, String artifactId, String path, String sourcePath, ProjectLinkTypeMap linkMap) {
      this.groupId = groupId;
      this.artifactId = artifactId;
      this.path = path;
      this.sourcePath = sourcePath;
      this.linkMap = linkMap;
   }
   
   public String getGroupId() {
      return groupId;
   }
   
   public String getArtifactId() {
      return artifactId;
   }
   
   public String getPath() {
      return path;
   }
   
   public String getSourcePath() {
      return sourcePath;
   }
   
   public boolean isApplicableToProject(ProjectType type) {
      return linkMap != null && linkMap.containsKey(type);
   }
   
   @Override
   public int compareTo(SDKEntryDescriptor o) {
      // TODO Auto-generated method stub
      return 0;
   }
   
   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      return groupId + ":" + artifactId + " - " + path;
   }
   
}
