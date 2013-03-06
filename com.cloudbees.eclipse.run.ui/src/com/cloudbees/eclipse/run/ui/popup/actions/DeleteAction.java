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

import java.text.MessageFormat;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.ObjectPluginAction;

import com.cloudbees.api.ApplicationInfo;
import com.cloudbees.eclipse.run.core.BeesSDK;
import com.cloudbees.eclipse.run.core.CBRunCoreActivator;
import com.cloudbees.eclipse.run.ui.CBRunUiActivator;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

@SuppressWarnings("restriction")
public class DeleteAction implements IObjectActionDelegate {

  @Override
  public void run(IAction action) {
    if (action instanceof ObjectPluginAction) {
      ObjectPluginAction pluginAction = (ObjectPluginAction) action;
      ISelection selection = pluginAction.getSelection();

      if (selection instanceof IStructuredSelection) {
        final IStructuredSelection structSelection = (IStructuredSelection) selection;
        @SuppressWarnings("unchecked")
        final Iterator<ApplicationInfo> iterator = structSelection.iterator();

        String name="-";
        
        if (structSelection.size()==1) {
          ApplicationInfo d = (ApplicationInfo) structSelection.getFirstElement();
          name = d.getId();
        }
        
        
        try {
          final String target = structSelection.size() > 1 ? "the selected apps" : "'"+name+"'";
          String question = MessageFormat.format("Are you sure you want to delete {0}?", target);

          boolean confirmed = MessageDialog.openQuestion(Display.getCurrent().getActiveShell(), "Delete", question);
          if (confirmed) {

            org.eclipse.core.runtime.jobs.Job job = new org.eclipse.core.runtime.jobs.Job(
                "Deleting "+target) {
              @Override
              protected IStatus run(final IProgressMonitor monitor) {

                monitor.beginTask("Deleting "+target+"...", structSelection.size() * 10);
                try {

                  while (iterator.hasNext()) {
                    ApplicationInfo applicationInfo = iterator.next();
                    monitor.subTask("Deleting '" + applicationInfo.getId() + "'...");
                    BeesSDK.delete(applicationInfo.getId());
                    monitor.worked(10);
                  }

                  CBRunCoreActivator.getPoller().fetchAndUpdateApps();                  
                  CloudBeesUIPlugin.getDefault().fireApplicationInfoChanged();

                } catch (Exception e) {
                  CBRunUiActivator.logErrorAndShowDialog(e);
                } finally {
                  monitor.done();
                }

                return Status.OK_STATUS;
              }
            };

            job.setUser(true);
            job.schedule();

          }
        } catch (Exception e) {
          CBRunUiActivator.logError(e);
        }
      }
    }
  }

  @Override
  public void selectionChanged(IAction action, ISelection selection) {
  }

  @Override
  public void setActivePart(IAction action, IWorkbenchPart targetPart) {
  }

}
