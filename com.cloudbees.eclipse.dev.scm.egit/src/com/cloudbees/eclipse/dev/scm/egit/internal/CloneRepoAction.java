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
package com.cloudbees.eclipse.dev.scm.egit.internal;

import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.ObjectPluginAction;

import com.cloudbees.eclipse.core.CBRemoteChangeListener;
import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.forge.api.ForgeInstance;
import com.cloudbees.eclipse.core.forge.api.ForgeInstance.STATUS;
import com.cloudbees.eclipse.dev.scm.egit.CloudBeesScmEgitPlugin;
import com.cloudbees.eclipse.dev.scm.egit.ForgeEGitSync;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

public class CloneRepoAction implements IObjectActionDelegate {

  public void run(IAction action) {
    if (action instanceof ObjectPluginAction) {
      ObjectPluginAction pluginAction = (ObjectPluginAction) action;
      ISelection selection = pluginAction.getSelection();

      if (selection instanceof IStructuredSelection) {
        IStructuredSelection structSelection = (IStructuredSelection) selection;
        Object element = structSelection.getFirstElement();

        if (element instanceof ForgeInstance) {
          final ForgeInstance fi = (ForgeInstance) element;

          cloneRepo(fi);
          
        }
      }
    }
  }

  private void cloneRepo(final ForgeInstance fi) {
    org.eclipse.core.runtime.jobs.Job job = new org.eclipse.core.runtime.jobs.Job("Cloning "+fi.url) {
      @Override
      protected IStatus run(final IProgressMonitor monitor) {

        monitor.beginTask("Preparing clone wizard", 100);
        try {

          if (ForgeEGitSync.internalSync(fi, monitor)) {
            CloudBeesUIPlugin.getDefault().showView("org.eclipse.egit.ui.RepositoriesView");
            
            boolean cloned = ForgeEGitSync.isAlreadyCloned(fi.url);

            if (cloned) {
              fi.status = STATUS.SYNCED;
            } else {
              fi.status = STATUS.SKIPPED;
            }

            Iterator<CBRemoteChangeListener> iterator = CloudBeesUIPlugin.getDefault().getJenkinsChangeListeners()
                .iterator();
            while (iterator.hasNext()) {
              CBRemoteChangeListener listener = iterator.next();
              listener.forgeChanged(null);
            }
            
          }

          return Status.OK_STATUS;
        } catch (Exception e) {
          String msg = e.getLocalizedMessage();
          if (e instanceof CloudBeesException) {
            e = (Exception) e.getCause();
          }
          CloudBeesScmEgitPlugin.getDefault().getLogger().error(msg, e);
          return new Status(IStatus.ERROR, CloudBeesScmEgitPlugin.PLUGIN_ID, 0, msg, e);
        } finally {
          monitor.done();
        }
      }
    };

    job.setUser(true);
    job.schedule();
  }
  
  public void selectionChanged(IAction action, ISelection selection) {
  }

  public void setActivePart(IAction action, IWorkbenchPart targetPart) {
  }
  
}