/**
 * 
 */
package com.cdmtech.m2e.flex.model;

/**
 * @author svanhoos
 *
 */
public interface IPathDescriptor {

   public static enum EntryKind implements IValuedEnum<Integer> {
      /**
       * .actionScriptProperties value = 4
       */
      FLEX_SDK(4),
      /**
       * .actionScriptProperties value = 3
       */
      LIBRARY_FILE(3),
      /**
       * .actionScriptProperties value = 1
       */
      PATH(1);
      
      private int value;
      
      private EntryKind(int value) {
         this.value = value;
      }
   
      @Override
      public Integer getValue() {
         return value;
      }
   }

   String getPath();
   void setPath(String path);
   
   EntryKind getKind();
}
