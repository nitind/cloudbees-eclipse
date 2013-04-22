package com.cloudbees.eclipse.dev.scm.subversive.internal;

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
import com.cloudbees.eclipse.dev.scm.subversive.CloudBeesScmSubversivePlugin;
import com.cloudbees.eclipse.dev.scm.subversive.ForgeSubversiveSync;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

public class RepoLocationAction implements IObjectActionDelegate {

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
    org.eclipse.core.runtime.jobs.Job job = new org.eclipse.core.runtime.jobs.Job("Importing location "+fi.url) {
      @Override
      protected IStatus run(final IProgressMonitor monitor) {

        monitor.beginTask("Preparing import wizard", 100);
        try {

          if (ForgeSubversiveSync.internalSync(fi, monitor)) {
            
            CloudBeesUIPlugin.getDefault().showView("org.eclipse.team.svn.ui.repository.RepositoriesView");
            
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
          CloudBeesUIPlugin.getDefault().getLogger().error(msg, e);
          return new Status(IStatus.ERROR, CloudBeesScmSubversivePlugin.PLUGIN_ID, 0, msg, e);
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