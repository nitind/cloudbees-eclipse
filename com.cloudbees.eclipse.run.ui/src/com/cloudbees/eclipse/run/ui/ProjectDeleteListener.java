/*******************************************************************************
 * Copyright (c) 2013 Cloud Bees, Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *              http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
