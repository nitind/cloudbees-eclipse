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
package com.cloudbees.eclipse.run.ui.popup.actions;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.ObjectPluginAction;

import com.cloudbees.eclipse.run.ui.CBRunUiActivator;
import com.cloudbees.eclipse.run.ui.launchconfiguration.CBCloudLaunchShortcut;
import com.cloudbees.eclipse.ui.AuthStatus;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

@SuppressWarnings("restriction")
public class DeployAction implements IObjectActionDelegate {

  @Override
  public void run(final IAction action) {

    Job job = new Job("Synchronizing Forge repositories") {
      @Override
      protected IStatus run(final IProgressMonitor monitor) {
        if (action instanceof ObjectPluginAction) {
          CBCloudLaunchShortcut shortcut = new CBCloudLaunchShortcut();
          ISelection selection = ((ObjectPluginAction) action).getSelection();
          shortcut.launch(selection, "run");
        }

        return new Status(IStatus.OK, CBRunUiActivator.PLUGIN_ID, "Deploy complete");
      }
    };

    job.setUser(true);
    job.schedule();
  }

  @Override
  public void selectionChanged(IAction action, ISelection selection) {
    action.setEnabled(CloudBeesUIPlugin.getDefault().getAuthStatus()==AuthStatus.OK);
  }

  @Override
  public void setActivePart(IAction action, IWorkbenchPart targetPart) {
  }
  
}
