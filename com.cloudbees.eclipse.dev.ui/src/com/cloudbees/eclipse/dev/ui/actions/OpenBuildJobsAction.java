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
package com.cloudbees.eclipse.dev.ui.actions;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ObjectPluginAction;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsInstanceResponse;
import com.cloudbees.eclipse.dev.ui.CloudBeesDevUiPlugin;

@SuppressWarnings("restriction")
public class OpenBuildJobsAction implements IObjectActionDelegate {

  @Override
  public void run(IAction action) {
    if (!(action instanceof ObjectPluginAction)) {
      return;
    }

    ObjectPluginAction pluginAction = (ObjectPluginAction) action;
    ISelection selection = pluginAction.getSelection();

    if (!(selection instanceof IStructuredSelection)) {
      return;
    }

    IStructuredSelection structSelection = (IStructuredSelection) selection;

    String viewUrl = null;

    if (structSelection.getFirstElement() instanceof JenkinsInstanceResponse) {
      JenkinsInstanceResponse resp = (JenkinsInstanceResponse) structSelection.getFirstElement();
      viewUrl = resp.viewUrl;
    }

    if (structSelection.getFirstElement() instanceof com.cloudbees.eclipse.core.jenkins.api.JenkinsInstanceResponse.View) {
      JenkinsInstanceResponse.View resp = (JenkinsInstanceResponse.View) structSelection.getFirstElement();
      viewUrl = resp.url;
    }
    
    try {
      CloudBeesDevUiPlugin.getDefault().showJobs(viewUrl, true);
    } catch (CloudBeesException e) {
      Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
      Status status = new Status(IStatus.ERROR, CloudBeesDevUiPlugin.PLUGIN_ID,
          "Received error while loading build jobs", e);
      ErrorDialog.openError(shell, "Error", "Cannot open build jobs", status);
    }
  }

  @Override
  public void selectionChanged(IAction action, ISelection selection) {
  }

  @Override
  public void setActivePart(IAction action, IWorkbenchPart targetPart) {
  }

}
