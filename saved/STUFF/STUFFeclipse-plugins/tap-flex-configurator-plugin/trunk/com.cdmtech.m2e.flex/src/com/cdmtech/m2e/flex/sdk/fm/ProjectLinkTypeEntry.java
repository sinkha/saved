/**
 * 
 */
package com.cdmtech.m2e.flex.sdk.fm;

import com.cdmtech.m2e.flex.model.LinkType;
import com.cdmtech.m2e.flex.model.ProjectType;

/**
 * @author svanhoos
 *
 */
public class ProjectLinkTypeEntry {

   private ProjectType projectType;
   private LinkType linkType;
   private Integer index;
   
   /**
    * 
    */
   public ProjectLinkTypeEntry(ProjectType projectType, LinkType linkType) {
      this(projectType, linkType, null);
   }
   
   /**
    * 
    */
   public ProjectLinkTypeEntry(ProjectType projectType, LinkType linkType, Integer index) {
      this.projectType = projectType;
      this.linkType = linkType;
      this.index = index;
   }
   
   public ProjectType getProjectType() {
      return projectType;
   }
   
   public LinkType getLinkType() {
      return linkType;
   }
   
   public Integer getIndex() {
      return index;
   }
   
}
