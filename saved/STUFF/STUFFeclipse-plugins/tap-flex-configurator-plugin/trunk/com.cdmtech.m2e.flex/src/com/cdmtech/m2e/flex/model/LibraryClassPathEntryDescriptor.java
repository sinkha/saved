package com.cdmtech.m2e.flex.model;

import com.adobe.flexbuilder.project.ClassPathEntryFactory;
import com.adobe.flexbuilder.project.IClassPathEntry;
import com.adobe.flexbuilder.project.actionscript.IActionScriptProjectSettings;

/**
 * @author svanhoos
 *
 */
public class LibraryClassPathEntryDescriptor extends AbstractClassPathEntryDescriptor implements
      IClassPathEntryDescriptor {

   public LibraryClassPathEntryDescriptor() {
   }
   
   public LibraryClassPathEntryDescriptor(String path, LinkType linkType) {
      this(path, linkType, false);
   }
   
   public LibraryClassPathEntryDescriptor(String path, LinkType linkType, boolean useDefaultLinkType) {
      setPath(path);
      setLinkType(linkType);
      setUseDefaultLinkType(useDefaultLinkType);
   }
   
   @Override
   public EntryKind getKind() {
      return EntryKind.LIBRARY_FILE;
   }
   
   @Override
   public IClassPathEntry toFlexEntry(IActionScriptProjectSettings context) {
      IClassPathEntry entry = ClassPathEntryFactory.newEntry(getKind().getValue(),
            getPath(), context);
      entry.setSourceValue(getSourcePath());
      
      if (getLinkType() != null) {
         entry.setLinkType(getLinkType().getValue());
      }
      
      entry.setUseDefaultLinkType(getUseDefaultLinkType());
      
      if (getCrossDomainRsls() != null) {
         entry.setCrossDomainRsls(getCrossDomainRsls());
      }
      
      return entry;
   }

}
