package com.cdmtech.m2e.flex.model;

import java.util.HashSet;
import java.util.Set;

import com.adobe.flexbuilder.project.ClassPathEntryFactory;
import com.adobe.flexbuilder.project.IClassPathEntry;
import com.adobe.flexbuilder.project.IFlexSDKClassPathEntry;
import com.adobe.flexbuilder.project.actionscript.IActionScriptProjectSettings;

/**
 * @author svanhoos
 *
 */
public class SdkClassPathEntryDescriptor extends AbstractClassPathEntryDescriptor implements IClassPathEntryDescriptor {

   private Set<ExclusionDescriptor> excludedEntries = new HashSet<ExclusionDescriptor>();
   
   public Set<ExclusionDescriptor> getExcludedEntries() {
      return excludedEntries;
   }
   
   public void setExcludedEntries(Set<ExclusionDescriptor> entries) {
      excludedEntries = entries;
   }
   
   public boolean addExcludedEntry(ExclusionDescriptor entry) {
      return excludedEntries.add(entry);
   }
   
   public boolean removeExcludedEntry(ExclusionDescriptor entry) {
      return excludedEntries.remove(entry);
   }
   
   @Override
   public EntryKind getKind() {
      return EntryKind.FLEX_SDK;
   }

   @Override
   public IClassPathEntry toFlexEntry(IActionScriptProjectSettings context) {
      IFlexSDKClassPathEntry entry = ClassPathEntryFactory.newFlexSDKEntry(context);
      entry.setValue(getPath());
      if (getLinkType() != null) {
         entry.setLinkType(getLinkType().getValue());
      }
      
      IClassPathEntry[] excluded = new IClassPathEntry[excludedEntries.size()];
      int i = 0;
      for (ExclusionDescriptor e : excludedEntries) {
         excluded[i++] = e.toFlexEntry(context);
      }
      entry.setExcludedEntries(excluded);
      
      return entry;
   }

}
