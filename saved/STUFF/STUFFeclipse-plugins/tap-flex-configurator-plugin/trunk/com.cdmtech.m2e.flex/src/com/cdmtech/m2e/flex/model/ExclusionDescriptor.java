/**
 * 
 */
package com.cdmtech.m2e.flex.model;

import com.adobe.flexbuilder.project.ClassPathEntryFactory;
import com.adobe.flexbuilder.project.IClassPathEntry;
import com.adobe.flexbuilder.project.actionscript.IActionScriptProjectSettings;

/**
 * Represents an SDK library exclusion in Flash Builder
 * 
 * @author svanhoos
 *
 */
public class ExclusionDescriptor implements IPathDescriptor {

   private String path;
   private IPathDescriptor.EntryKind kind;
   
   public ExclusionDescriptor(String path) {
      this(path, IPathDescriptor.EntryKind.LIBRARY_FILE);
   }
   
   public ExclusionDescriptor(String path, IPathDescriptor.EntryKind kind) {
      this.path = path;
      this.setKind(kind);
   }
   
   @Override
   public void setPath(String path) {
      this.path = path;
   }
   
   @Override
   public String getPath() {
      return path;
   }
   
   public void setKind(IPathDescriptor.EntryKind kind) {
      this.kind = kind;
   }

   @Override
   public IPathDescriptor.EntryKind getKind() {
      return kind;
   }
   
   public IClassPathEntry toFlexEntry(IActionScriptProjectSettings context) {
      return ClassPathEntryFactory.newEntry(kind.getValue(), path, context);
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof ExclusionDescriptor))
         return false;
      
      ExclusionDescriptor other = (ExclusionDescriptor) obj;
      return path.equals(other.kind) && kind == other.kind;
   }
   
   @Override
   public int hashCode() {
      return kind.getValue() * path.hashCode();
   }
   
}
