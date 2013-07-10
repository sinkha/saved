/**
 * 
 */
package com.cdmtech.m2e.flex.sdk.fm;

import java.util.HashMap;

import com.cdmtech.m2e.flex.model.ProjectType;

/**
 * @author svanhoos
 *
 */
public class ProjectLinkTypeMap extends HashMap<ProjectType, ProjectLinkTypeEntry> {

   /**
    * 
    */
   private static final long serialVersionUID = -1097697666684210724L;

   /**
    * 
    */
   public ProjectLinkTypeMap(ProjectLinkTypeEntry... entries) {
      for (ProjectLinkTypeEntry entry : entries) {
         put(entry.getProjectType(), entry);
      }
   }
   
}
