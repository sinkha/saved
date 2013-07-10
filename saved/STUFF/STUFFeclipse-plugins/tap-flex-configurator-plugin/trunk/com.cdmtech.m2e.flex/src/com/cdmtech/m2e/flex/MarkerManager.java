package com.cdmtech.m2e.flex;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author svanhoos
 *
 */
public class MarkerManager {
   
   /**
    * IDs defined as eclipse marker extension points.
    * @author svanhoos
    */
   public static enum MarkerID {
      /**
       * General flex configurator problem
       */
      FLEX_CONFIGURATOR_PROBLEM("com.cdmtech.maven.flex.markers.problem")
      ;
      
      private String id;
      
      private MarkerID(String id) {
         this.id = id;
      }
      
      @Override
      public String toString() {
         return id;
      }
   }
   
   private static Logger logger = LoggerFactory.getLogger(MarkerManager.class);
   
   //private IMarker addMarker(IResource resource, String type, String message, int lineNumber, int severity, boolean isTransient)
   
   public IMarker addProblem(IResource resource, MarkerID type, String message, int lineNumber, int severity, boolean isTransient) {
      IMarker marker = null;
      try {
        if(resource.isAccessible()) {
          if(lineNumber == -1) {
            lineNumber = 1;
          }

          marker = resource.createMarker(type.toString());
          marker.setAttribute(IMarker.MESSAGE, message);
          marker.setAttribute(IMarker.SEVERITY, severity);
          marker.setAttribute(IMarker.TRANSIENT, isTransient);
          
          //marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
          logger.debug("Created marker '{}' on resource '{}'.", message, resource.getFullPath());
        }
      }
      catch(CoreException ex) {
        logger.error("Unable to add marker; " + ex.toString(), ex);
      }
      return marker;
   }
   
   public IMarker addGeneralProblem(IResource resource, String message, String location) {
      return addGeneralProblem(resource, message, location, 2, MarkerID.FLEX_CONFIGURATOR_PROBLEM);
   }
   
   public IMarker addGeneralProblem(IResource resource, String message, String location, int severity, MarkerID type) {
      IMarker marker = null;
      try {
        if(resource.isAccessible()) {
          marker = resource.createMarker(type.toString());
          marker.setAttribute(IMarker.MESSAGE, message);
          marker.setAttribute(IMarker.SEVERITY, severity);
          marker.setAttribute(IMarker.TRANSIENT, false);
          marker.setAttribute(IMarker.LOCATION, location);
          
          logger.debug("Created marker '{}' on resource '{}'.", message, resource.getFullPath());
        }
      }
      catch(CoreException ex) {
        logger.error("Unable to add marker; " + ex.toString(), ex);
      }
      return marker;
   }
   
   public void removeMarkersOn(IResource resource, MarkerID type) {
      try {
         resource.deleteMarkers(type.toString(), true, IResource.DEPTH_INFINITE);
         logger.debug("Removed markers on resource: " + resource.toString());
      }
      catch (CoreException ex) {
         logger.error("Unable to remove markers on resource: " + resource.toString(), ex);
      }
   }
}
