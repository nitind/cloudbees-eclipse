package com.cloudbees.eclipse.run.ui.popup.actions;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Iterator;

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

    private final Iterator<ApplicationInfo> iterator;
    private final int selectionCount;

    public RunnableWithProgressImpl(Iterator<ApplicationInfo> iterator, int selectionCount) {
      this.iterator = iterator;
      this.selectionCount = selectionCount;
    }

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
      try {
        monitor.beginTask("Deleting selected applications...", this.selectionCount);

        while (this.iterator.hasNext()) {
          ApplicationInfo applicationInfo = this.iterator.next();
          monitor.subTask("Deleting " + applicationInfo.getId() + "...");
          BeesSDK.delete(applicationInfo.getId());
          monitor.worked(1);
        }
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
        @SuppressWarnings("unchecked")
        Iterator<ApplicationInfo> iterator = structSelection.iterator();

        try {
          String target = structSelection.size() > 1 ? "projects" : "project";
          String question = MessageFormat.format("Are you sure you want to delete the selected {0} from RUN@cloud?",
              target);

          boolean confirmed = MessageDialog.openQuestion(Display.getCurrent().getActiveShell(), "Delete", question);
          if (confirmed) {
            ProgressMonitorDialog monitor = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());
            monitor.run(false, false, new RunnableWithProgressImpl(iterator, structSelection.size()));
            CloudBeesUIPlugin.getDefault().fireApplicationInfoChanged();
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
