/*******************************************************************************
 * Copyright (c) 2013 Cloud Bees, Inc.
 * All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	Cloud Bees, Inc. - initial API and implementation 
 *******************************************************************************/
package com.cloudbees.eclipse.run.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;

import com.cloudbees.eclipse.core.CloudBeesNature;
import com.cloudbees.eclipse.run.core.launchconfiguration.CBLaunchConfigurationConstants;

public class ProjectDeleteListener implements IResourceChangeListener {

  @Override
  public void resourceChanged(IResourceChangeEvent event) {
    if (event.getResource().getType() != IResource.PROJECT) {
      return;
    }

    IProject project = (IProject) event.getResource();

    if (!CloudBeesNature.isEnabledFor(project)) {
      return;
    }

    try {
      for (ILaunchConfiguration configuration : getLaunchConfigurations(project)) {
        try {
          configuration.delete();
        } catch (CoreException e) {
          e.printStackTrace();
        }
      }
    } catch (CoreException e) {
      e.printStackTrace();
    }
  }

  private List<ILaunchConfiguration> getLaunchConfigurations(IProject project) throws CoreException {
    ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
    List<ILaunchConfiguration> launchConfigurations = new ArrayList<ILaunchConfiguration>();

    for (ILaunchConfiguration configuration : launchManager.getLaunchConfigurations()) {
      String prj = configuration.getAttribute(CBLaunchConfigurationConstants.ATTR_CB_PROJECT_NAME, "");

      if (project != null && project.getName().equals(prj)) {
        launchConfigurations.add(configuration);
      }
    }

    return launchConfigurations;
  }

}
