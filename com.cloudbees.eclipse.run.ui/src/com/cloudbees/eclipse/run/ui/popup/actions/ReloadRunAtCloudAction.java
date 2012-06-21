package com.cloudbees.eclipse.run.ui.popup.actions;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.run.core.CBRunCoreActivator;
import com.cloudbees.eclipse.run.ui.CBRunUiActivator;
import com.cloudbees.eclipse.run.ui.Images;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;
import com.cloudbees.eclipse.ui.views.CBTreeAction;

public class ReloadRunAtCloudAction extends CBTreeAction implements IObjectActionDelegate {

  public ReloadRunAtCloudAction() {
    super();
    setText("Reload RUN@cloud apps@");
    setToolTipText("Reload RUN@cloud apps");
    setImageDescriptor(CBRunUiActivator.getImageDescription(Images.CLOUDBEES_REFRESH));
  }

  @Override
  public void run() {
    reload();
  }

  private void reload() {
    org.eclipse.core.runtime.jobs.Job job = new org.eclipse.core.runtime.jobs.Job("Loading RUN@cloud apps") {
      @Override
      protected IStatus run(final IProgressMonitor monitor) {

        monitor.beginTask("Loading RUN@cloud apps", 100);
        try {
          CBRunCoreActivator.getPoller().fetchAndUpdate();
          monitor.worked(75);
          CloudBeesUIPlugin.getDefault().fireApplicationInfoChanged();
          monitor.worked(25);

          return Status.OK_STATUS;
        } catch (Exception e) {
          String msg = e.getLocalizedMessage();
          if (e instanceof CloudBeesException) {
            e = (Exception) e.getCause();
          }
          CBRunUiActivator.getDefault().getLogger().error(msg, e);
          return new Status(IStatus.ERROR, CBRunUiActivator.PLUGIN_ID, 0, msg, e);
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
