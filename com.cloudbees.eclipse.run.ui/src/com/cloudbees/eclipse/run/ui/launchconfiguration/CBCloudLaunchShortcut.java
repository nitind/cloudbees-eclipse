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
package com.cloudbees.eclipse.run.ui.launchconfiguration;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.run.core.BeesSDK;
import com.cloudbees.eclipse.run.core.util.CBRunUtil;
import com.cloudbees.eclipse.run.ui.CBRunUiActivator;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

public class CBCloudLaunchShortcut implements ILaunchShortcut {

  private ILaunchConfiguration configuration;

  boolean cancelled = false;

  @Override
  public void launch(ISelection selection, String mode) {

    if (selection instanceof IStructuredSelection) {

      IStructuredSelection structuredSelection = (IStructuredSelection) selection;

      final Object element = structuredSelection.getFirstElement();

      String name = null;
      IFile file = null;
      IProject project = null;

      if (element instanceof IProject) {
        project = (IProject) element;
        name = ((IProject) element).getName();
      } else if (element instanceof IJavaProject) {
        project = ((IJavaProject) element).getProject();
        name = ((IJavaProject) element).getProject().getName();
      } else if (element instanceof IFile) {
        file = ((IFile) element);
        name = ((IFile) element).getName();
      }

      if (name == null) {
        throw new RuntimeException("Element type not detected: " + element);
      }

      final IFile file1 = file;
      final IProject project1 = project;

      String loc = null;
      try {
        String account = CloudBeesUIPlugin.getDefault().getActiveAccountName(new NullProgressMonitor());
        loc = BeesSDK.getAccountAppId(account, null, project != null ? project : file.getProject());
      } catch (CloudBeesException e) {
        e.printStackTrace();
        throw new RuntimeException("Failed to retrieve account and app id info", e);
      } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException("Failed to retrieve account and app id info", e);
      }

      final String locf = loc;
      org.eclipse.core.runtime.jobs.Job sjob = new org.eclipse.core.runtime.jobs.Job("Deploying " + name + " to " + loc
          + " at CloudBees RUN@cloud...") {
        @Override
        protected IStatus run(final IProgressMonitor monitor) {
          try {
            CBCloudLaunchShortcut.this.internalLaunch(element, monitor, file1, project1, locf);
            return org.eclipse.core.runtime.Status.OK_STATUS;
          } catch (CloudBeesException e) {
            //CloudBeesUIPlugin.getDefault().getLogger().error(e);
            return new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.Status.ERROR,
                CloudBeesUIPlugin.PLUGIN_ID, 0, e.getLocalizedMessage(), e.getCause());
          }
        }
      };
      sjob.setUser(true);
      sjob.schedule();

      if (true) {
        return;
      }

      try {
        List<ILaunchConfiguration> launchConfigurations = CBRunUtil.getOrCreateCloudBeesLaunchConfigurations(name,
            true, null);
        this.configuration = launchConfigurations.get(launchConfigurations.size() - 1);

        if (!this.cancelled) {
          DebugUITools.launch(this.configuration, mode);
        }
      } catch (CoreException e) {
        CBRunUiActivator.logError(e);
      }

    }
  }

  protected void internalLaunch(Object element, IProgressMonitor monitor, final IFile file, IProject project, final String preappid)
      throws CloudBeesException {

    // Strategy for decising if build is needed: invoke project build always when selection is project
    if (project != null) {
      try {
        String appId = BeesSDK.getBareAppId(project);
        String account = CloudBeesUIPlugin.getDefault().getActiveAccountName(monitor);
        BeesSDK.deploy(project, account, appId, true, monitor);
        return;
      } catch (CloudBeesException e) {
        e.printStackTrace();
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else if (file != null) {// deploy specified file, without build. If unknown type, confirm first.
      String appId;
      try {

        if (!BeesSDK.hasSupportedExtension(file.getName())) {
          final String ext = BeesSDK.getExtension(file.getName());
  
          final Boolean[] openConfirm = new Boolean[]{Boolean.FALSE};
          
          PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

            public void run() {
              openConfirm[0] = MessageDialog.openConfirm(CloudBeesUIPlugin.getActiveWindow().getShell(),
                  "Deploy to CloudBees RUN@cloud", ext
                      + " is an unknown app package type.\nAre you sure you want to deploy '" + file.getName() + "' to '"
                      + preappid + "'?");              
            }

          });

          if (!openConfirm[0]) {
            return;
          }

        }

        String account = CloudBeesUIPlugin.getDefault().getActiveAccountName(monitor);
        appId = BeesSDK.getAccountAppId(account, null, file.getProject());
        BeesSDK.deploy(project, appId, file.getRawLocation().toFile(), monitor);
        return;
      } catch (Exception e) {
        final Exception e2 = e;
        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
          public void run() {
            MessageDialog.openWarning(CloudBeesUIPlugin.getActiveWindow().getShell(), "Deploy failed!",
            "Deployment failed for '" + file.getName() + "': "+e2.getMessage());
          }});
      }
    }

  }

  @Override
  public void launch(IEditorPart editor, String mode) {
    // TODO Auto-generated method stub

  }

}
