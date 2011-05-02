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
import com.cloudbees.eclipse.run.core.BeesSDK;
import com.cloudbees.eclipse.run.ui.CBRunUiActivator;

@SuppressWarnings("restriction")
public class StopAction implements IObjectActionDelegate {

  private final class IRunnableWithProgressImplementation implements IRunnableWithProgress {
    private final ISelection selection;

    private IRunnableWithProgressImplementation(ISelection selection) {
      this.selection = selection;
    }

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
      monitor.beginTask("Stopping RUN@cloud server", 1);
      Object firstElement = ((StructuredSelection) this.selection).getFirstElement();

      if (firstElement instanceof ApplicationInfo) {
        try {
          ApplicationInfo appInfo = (ApplicationInfo) firstElement;

          // FIXME currently cannot match with server
          //          IServer[] servers = ServerCore.getServers();
          //          for (IServer iServer : servers) {
          //            String attribute = iServer.getAttribute(CBLaunchConfigurationConstants.PROJECT, "");
          //            String name = ((IProject) firstElement).getName();
          //
          //            if (appInfo.getId().equals(attribute)
          //                && "com.cloudbees.eclipse.core.runcloud".equals(iServer.getServerType().getId())) {
          //              ((Server) iServer).setServerState(IServer.STATE_STOPPED);
          //            }
          //          }

          String id = appInfo.getId();
          BeesSDK.stop(id.substring(id.indexOf("/") + 1));
          monitor.done();

        } catch (Exception e) {
          Status status = new Status(IStatus.ERROR, CBRunUiActivator.PLUGIN_ID, e.getMessage());
          CBRunUiActivator.getDefault().getLog().log(status);
          monitor.done();
        }
      }

    }
  }

  /**
   * Constructor for Action1.
   */
  public StopAction() {
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
        ProgressMonitorDialog monitor = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());
        try {
          monitor.run(false, false, new IRunnableWithProgressImplementation(selection));
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
