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
package com.cloudbees.eclipse.dtp.internal.actions;

import java.text.MessageFormat;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.datatools.connectivity.ConnectionProfileException;
import org.eclipse.datatools.connectivity.IConnectionProfile;
import org.eclipse.datatools.connectivity.ProfileManager;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.ObjectPluginAction;

import com.cloudbees.api.DatabaseInfo;
import com.cloudbees.eclipse.dtp.CloudBeesDataToolsPlugin;
import com.cloudbees.eclipse.dtp.internal.ConnectDatabaseAction;
import com.cloudbees.eclipse.run.core.BeesSDK;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

@SuppressWarnings("restriction")
public class DeleteDatabaseAction implements IObjectActionDelegate {

  @Override
  public void run(IAction action) {
    if (action instanceof ObjectPluginAction) {
      ObjectPluginAction pluginAction = (ObjectPluginAction) action;
      ISelection selection = pluginAction.getSelection();

      if (selection instanceof IStructuredSelection) {
        final IStructuredSelection structSelection = (IStructuredSelection) selection;
        @SuppressWarnings("unchecked")
        final Iterator<DatabaseInfo> iterator = structSelection.iterator();

        String name="-";
        
        if (structSelection.size()==1) {
          DatabaseInfo d = (DatabaseInfo) structSelection.getFirstElement();
          name = d.getName();
        }
        
        
        try {
          final String target = structSelection.size() > 1 ? "the selected databases" : "'"+name+"'";
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
                    DatabaseInfo databaseInfo = iterator.next();
                    monitor.subTask("Deleting '" + databaseInfo.getName() + "'...");
                    BeesSDK.deleteDatabase(databaseInfo.getName());
                    monitor.worked(5);
                    deleteDatabaseConnectionProfile(databaseInfo);
                    monitor.worked(5);
                  }

                  CloudBeesDataToolsPlugin.getPoller().fetchAndUpdateDatabases(monitor);                  
                  CloudBeesUIPlugin.getDefault().fireDatabaseInfoChanged();

                } catch (Exception e) {
                  CloudBeesDataToolsPlugin.logErrorAndShowDialog(e);
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
          CloudBeesDataToolsPlugin.logError(e);
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

  private void deleteDatabaseConnectionProfile(DatabaseInfo db) throws ConnectionProfileException {
    String pName = db.getOwner() + "/" + db.getName();
    IConnectionProfile exp = ProfileManager.getInstance().getProfileByName(pName);
    if (exp != null) {
      try {
        exp.disconnect();
      } finally {
        ProfileManager.getInstance().deleteProfile(exp);
      }      
    }
  }
}
