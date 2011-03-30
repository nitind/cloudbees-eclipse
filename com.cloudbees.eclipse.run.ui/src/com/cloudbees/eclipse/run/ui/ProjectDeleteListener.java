package com.cloudbees.eclipse.run.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;

import com.cloudbees.eclipse.core.CloudBeesNature;
import com.cloudbees.eclipse.run.core.util.CBRunUtil;

public class ProjectDeleteListener implements IResourceChangeListener {
  
  public void resourceChanged(IResourceChangeEvent event) {
    if(event.getResource().getType() != IResource.PROJECT) {
      return;
    }
    
    IProject project = (IProject) event.getResource();
    
    if(!CloudBeesNature.isEnabledFor(project)) {
      return;
    }
    
    try {
      for(ILaunchConfiguration configuration : CBRunUtil.getLaunchConfigurations(project.getName())) {
        configuration.delete();
      }
    } catch (CoreException e) {
      e.printStackTrace();
    }
  }
}
