package com.cdmtech.m2e.flex.model;

import com.adobe.flexbuilder.project.common.CrossDomainRslEntry;


/**
 * @author svanhoos
 *
 */
public abstract class AbstractClassPathEntryDescriptor implements IClassPathEntryDescriptor {

   private String path = null;
   private String sourcePath = null;
   private LinkType linkType;
   private boolean useDefaultLinkType;
   private CrossDomainRslEntry[] crossDomainRsls = null;
   
   @Override
   public String getPath() {
      return path;
   }
   
   @Override
   public void setPath(String path) {
      this.path = path;
   }
   
   @Override
   public String getSourcePath() {
      return sourcePath;
   }
   
   public void setSourcePath(String sourcePath) {
      this.sourcePath = sourcePath;
   }
   
   @Override
   public LinkType getLinkType() {
      return linkType;
   }
   
   @Override
   public void setLinkType(LinkType linkType) {
      this.linkType = linkType;
   }
   
   @Override
   public boolean getUseDefaultLinkType() {
      return useDefaultLinkType;
   }
   
   @Override
   public void setUseDefaultLinkType(boolean b) {
      this.useDefaultLinkType = b;
   }
   
   @Override
   public CrossDomainRslEntry[] getCrossDomainRsls() {
      return crossDomainRsls;
   }
   
   @Override
   public void setCrossDomainRsls(CrossDomainRslEntry[] rsls) {
      this.crossDomainRsls = rsls;
   }
   
}
