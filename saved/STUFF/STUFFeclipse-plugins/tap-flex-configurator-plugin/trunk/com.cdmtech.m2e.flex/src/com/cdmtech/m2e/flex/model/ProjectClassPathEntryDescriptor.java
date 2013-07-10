package com.cdmtech.m2e.flex.model;

import com.adobe.flexbuilder.project.ClassPathEntryFactory;
import com.adobe.flexbuilder.project.IClassPathEntry;
import com.adobe.flexbuilder.project.actionscript.IActionScriptProjectSettings;

/**
 * @author svanhoos
 *
 */
public class ProjectClassPathEntryDescriptor extends AbstractClassPathEntryDescriptor implements
      IClassPathEntryDescriptor {

   public ProjectClassPathEntryDescriptor() {
      
      // FlexMojos puts all project/library dependencies as link internal
      // TODO: Investigate if this works in all cases
      setLinkType(LinkType.INTERNAL);
   }
   
   @Override
   public EntryKind getKind() {
      return EntryKind.LIBRARY_FILE;
   }
   
   @Override
   public IClassPathEntry toFlexEntry(IActionScriptProjectSettings context) {
      IClassPathEntry entry = ClassPathEntryFactory.newEntry(getKind().getValue(),
            getPath(), context);
      
      if (getSourcePath() != null) {
         entry.setSourceValue(getSourcePath());
      }
      
      if (getLinkType() != null) {
         entry.setLinkType(getLinkType().getValue());
      }
      
      return entry;
   }

}
