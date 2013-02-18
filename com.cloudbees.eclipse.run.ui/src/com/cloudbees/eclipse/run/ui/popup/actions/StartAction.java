package com.cloudbees.eclipse.run.ui.popup.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.ObjectPluginAction;

import com.cloudbees.api.ApplicationInfo;
import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.run.core.BeesSDK;
import com.cloudbees.eclipse.run.core.CBRunCoreActivator;
import com.cloudbees.eclipse.run.ui.CBRunUiActivator;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

@SuppressWarnings("restriction")
public class StartAction implements IObjectActionDelegate {

  private final class IRunnableWithProgressImplementation implements IRunnableWithProgress {
    private final ISelection selection;

    private IRunnableWithProgressImplementation(ISelection selection) {
      this.selection = selection;
    }

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
      //monitor.beginTask("Starting RUN@cloud server", 1);
            
      Object firstElement = ((StructuredSelection) this.selection).getFirstElement();

      if (firstElement instanceof ApplicationInfo) {
        try {
          ApplicationInfo appInfo = (ApplicationInfo) firstElement;

          String id = appInfo.getId();
          int i = id.indexOf("/");
          String account = id.substring(0, i);
          String idstr = id.substring(i + 1);
          
          jobStart(account, idstr);
          
          
          //monitor.done();

        } catch (Exception e) {
          CBRunUiActivator.logErrorAndShowDialog(e);
          //monitor.done();

        }
      }
    }

    private void jobStart(final String account, final String idstr) {

      org.eclipse.core.runtime.jobs.Job job = new org.eclipse.core.runtime.jobs.Job("Starting RUN@cloud app "+idstr+" at "+account) {
        @Override
        protected IStatus run(final IProgressMonitor monitor) {

          monitor.beginTask("Starting RUN@cloud app "+idstr+" at "+account, 100);
          try {
            BeesSDK.start(account, idstr);
            monitor.worked(75);
            monitor.setTaskName("Loading RUN@cloud apps list");
            CBRunCoreActivator.getPoller().fetchAndUpdate();
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
  }

  /**
   * Constructor for Action1.
   */
  public StartAction() {
    super();
  }

  /**
   * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
   */
  @Override
  public void setActivePart(IAction action, IWorkbenchPart targetPart) {
  }

  /**
   * @see IActionDelegate#run(IAction)
   */
  @Override
  public void run(IAction action) {
    if (action instanceof ObjectPluginAction) {

      final ISelection selection = ((ObjectPluginAction) action).getSelection();

      if (selection instanceof StructuredSelection) {
        try {
          ProgressMonitorDialog monitor = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());
          monitor.run(false, false, new IRunnableWithProgressImplementation(selection));
          CloudBeesUIPlugin.getDefault().fireApplicationInfoChanged();
        } catch (Exception e) {
          CBRunUiActivator.logError(e);
        }
      }
    }
  }

  /**
   * @see IActionDelegate#selectionChanged(IAction, ISelection)
   */
  @Override
  public void selectionChanged(IAction action, ISelection selection) {
  }

}
