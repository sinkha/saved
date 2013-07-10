package com.cdmtech.m2e.flex.model;

import com.adobe.flexbuilder.project.ClassPathEntryFactory;
import com.adobe.flexbuilder.project.IClassPathEntry;
import com.adobe.flexbuilder.project.actionscript.IActionScriptProjectSettings;

/**
 * @author svanhoos
 *
 */
public class PathClassPathEntryDescriptor extends AbstractClassPathEntryDescriptor implements
      IClassPathEntryDescriptor {

   public PathClassPathEntryDescriptor() {
   }
   
   public PathClassPathEntryDescriptor(String path) {
      this(path, LinkType.INTERNAL);
   }
   
   public PathClassPathEntryDescriptor(String path, LinkType linkType) {
      setPath(path);
      setLinkType(linkType);
   }
   
   @Override
   public EntryKind getKind() {
      return EntryKind.PATH;
   }
   
   @Override
   public IClassPathEntry toFlexEntry(IActionScriptProjectSettings context) {
      IClassPathEntry entry = ClassPathEntryFactory.newEntry(IPathDescriptor.EntryKind.PATH.getValue(),
            getPath(), context);
      if (getSourcePath() != null) {
         entry.setSourceValue(getSourcePath());
      }
      entry.setLinkType(getLinkType().getValue());
      
      return entry;
   }

}
