package com.cdmtech.m2e.flex.config;

/**
 * A configuration exception that should result in an
 * eclipse problem marker
 * 
 * @author svanhoos
 *
 */
public class MarkedFlexConfiguratorException extends FlexConfiguratorException {

   /**
    * 
    */
   private static final long serialVersionUID = 3976159158987234131L;

   public MarkedFlexConfiguratorException(String message) {
      super(message);
   }

}
