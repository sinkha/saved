/**
 * 
 */
package com.cdmtech.m2e.flex;

import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.project.configurator.AbstractBuildParticipant;

/**
 * A build participant that skips the execution.
 * 
 * @author svanhoos
 */
public class NoopBuildParticipant extends AbstractBuildParticipant {

   @Override
   public Set<IProject> build(int kind, IProgressMonitor monitor) throws Exception {
      return null;
   }

}
