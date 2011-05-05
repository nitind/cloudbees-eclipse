package com.cloudbees.eclipse.run.ui.popup.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.ObjectPluginAction;

import com.cloudbees.api.ApplicationInfo;
import com.cloudbees.eclipse.run.core.BeesSDK;
import com.cloudbees.eclipse.run.ui.CBRunUiActivator;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

@SuppressWarnings("restriction")
public class DeleteAction implements IObjectActionDelegate {

  private class RunnableWithProgressImpl implements IRunnableWithProgress {

    private final ApplicationInfo appInfo;

    public RunnableWithProgressImpl(ApplicationInfo appInfo) {
      this.appInfo = appInfo;
    }

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
      monitor.beginTask("Deleting project from RUN@cloud server", 1);
      try {
        BeesSDK.delete(this.appInfo.getId());
      } catch (Exception e) {
        CBRunUiActivator.logErrorAndShowDialog(e);
      } finally {
        monitor.done();
      }
    }
  }

  @Override
  public void run(IAction action) {
    if (action instanceof ObjectPluginAction) {
      ObjectPluginAction pluginAction = (ObjectPluginAction) action;
      ISelection selection = pluginAction.getSelection();

      if (selection instanceof IStructuredSelection) {
        IStructuredSelection structSelection = (IStructuredSelection) selection;
        Object element = structSelection.getFirstElement();

        if (element instanceof ApplicationInfo) {
          try {
            boolean confirmed = MessageDialog.openQuestion(Display.getCurrent().getActiveShell(), "Delete",
                "Are you sure you want to delete this project from RUN@cloud?");
            if (confirmed) {
              ProgressMonitorDialog monitor = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());
              monitor.run(false, false, new RunnableWithProgressImpl((ApplicationInfo) element));
              CloudBeesUIPlugin.getDefault().fireApplicationInfoChanged();
            }
          } catch (Exception e) {
            CBRunUiActivator.logError(e);
          }
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
