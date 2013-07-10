package com.cdmtech.m2e.flex.model;


import com.adobe.flexbuilder.project.IClassPathEntry;
import com.adobe.flexbuilder.project.actionscript.IActionScriptProjectSettings;
import com.adobe.flexbuilder.project.common.CrossDomainRslEntry;

/**
 * @author svanhoos
 *
 */
public interface IClassPathEntryDescriptor extends IPathDescriptor {
   
   String getSourcePath();
   void setSourcePath(String path);
   
   LinkType getLinkType();
   void setLinkType(LinkType linkType);
   
   boolean getUseDefaultLinkType();
   void setUseDefaultLinkType(boolean b);
   
   CrossDomainRslEntry[] getCrossDomainRsls();
   void setCrossDomainRsls(CrossDomainRslEntry[] rsls);
   
   //TODO: why is context needed?
   IClassPathEntry toFlexEntry(IActionScriptProjectSettings context);
   
}
