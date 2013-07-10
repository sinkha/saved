/**
 * 
 */
package com.cdmtech.m2e.flex.model;

import com.adobe.flexbuilder.project.IClassPathEntry;

public enum LinkType implements IValuedEnum<Integer> {
   /**
    * 4
    * RSL Digest
    */
   CROSS_DOMAIN_RSL(4),
   
   /**
    * 0
    */
   DEFAULT(0),
   
   /**
    * 2
    */
   EXTERNAL(2),
   
   /**
    * 1
    * Internal/Merge
    */
   INTERNAL(1),
   
   /**
    * 3
    */
   RSL(3);

   private int value;

   private LinkType(int value) {
      this.value = value;
   }
   
   @Override
   public Integer getValue() {
      return value;
   }
   
   public static LinkType fromValue(int value) {
      switch (value) {
      case IClassPathEntry.LINK_TYPE_INTERNAL:
         return LinkType.INTERNAL;
      case IClassPathEntry.LINK_TYPE_EXTERNAL:
         return LinkType.EXTERNAL;
      case IClassPathEntry.LINK_TYPE_RSL:
         return LinkType.RSL;
      case IClassPathEntry.LINK_TYPE_CROSS_DOMAIN_RSL:
         return LinkType.CROSS_DOMAIN_RSL;
      default:
         return LinkType.DEFAULT;
      }
   }
   
   public static LinkType parseScope(String artifactScope) {
      if (artifactScope == null || artifactScope.isEmpty()) {
         return null;
      }
      
      if (artifactScope.equalsIgnoreCase("external")) {
         return EXTERNAL;
      }
      
      return null;
   }
   
}