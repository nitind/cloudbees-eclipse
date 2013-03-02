package com.cloudbees.eclipse.dtp.internal;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.dtp.CloudBeesDataToolsPlugin;
import com.cloudbees.eclipse.dtp.Images;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;
import com.cloudbees.eclipse.ui.views.CBTreeAction;

public class ReloadDatabaseAction extends CBTreeAction implements IObjectActionDelegate {

  public ReloadDatabaseAction() {
    super();
    setText("Reload Database info");
    setToolTipText("Reload Database info");
    setImageDescriptor(CloudBeesDataToolsPlugin.getImageDescription(Images.CLOUDBEES_REFRESH));
  }

  @Override
  public void run() {
    reload();
  }

  public static void reload() {
    org.eclipse.core.runtime.jobs.Job job = new org.eclipse.core.runtime.jobs.Job("Loading Database info") {
      @Override
      protected IStatus run(final IProgressMonitor monitor) {

        monitor.beginTask("Loading Database info", 100);
        try {
          CloudBeesDataToolsPlugin.getPoller().fetchAndUpdateDatabases(new NullProgressMonitor());         
          monitor.worked(75);
          CloudBeesUIPlugin.getDefault().fireDatabaseInfoChanged();
          monitor.worked(25);

          return Status.OK_STATUS;
        } catch (Exception e) {
          String msg = e.getLocalizedMessage();
          if (e instanceof CloudBeesException) {
            e = (Exception) e.getCause();
          }
          CloudBeesDataToolsPlugin.getDefault().getLogger().error(msg, e);
          return new Status(IStatus.ERROR, CloudBeesDataToolsPlugin.PLUGIN_ID, 0, msg, e);
        } finally {
          monitor.done();
        }
      }
    };

    job.setUser(true);
    job.schedule();

  }

  @Override
  public void run(IAction action) {
    reload();
  }

  @Override
  public void selectionChanged(IAction action, ISelection selection) {
  }

  @Override
  public void setActivePart(IAction action, IWorkbenchPart targetPart) {
  }

  @Override
  public boolean isPopup() {
    return false;
  }

  @Override
  public boolean isPullDown() {
    return true;
  }

  @Override
  public boolean isToolbar() {
    return false;
  }

}
