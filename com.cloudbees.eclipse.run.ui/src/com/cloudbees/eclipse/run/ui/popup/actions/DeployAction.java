package com.cloudbees.eclipse.run.ui.popup.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.ObjectPluginAction;

import com.cloudbees.eclipse.run.core.TestRunner;
import com.cloudbees.eclipse.run.ui.CBRunUiActivator;

public class DeployAction implements IObjectActionDelegate {

  private Shell shell;

  /**
   * Constructor for Action1.
   */
  public DeployAction() {
    super();
  }

  /**
   * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
   */
  public void setActivePart(IAction action, IWorkbenchPart targetPart) {
    this.shell = targetPart.getSite().getShell();
  }

  /**
   * @see IActionDelegate#run(IAction)
   */
  public void run(IAction action) {
    if (action instanceof ObjectPluginAction) {
      ISelection selection = ((ObjectPluginAction) action).getSelection();

      if (selection instanceof TreeSelection) {
        Object firstElement = ((TreeSelection) selection).getFirstElement();

        if (firstElement instanceof IProject) {
          try {
            new TestRunner().deploy((IProject) firstElement);

          } catch (Exception e) {
            Status status = new Status(IStatus.ERROR, CBRunUiActivator.PLUGIN_ID, e.getMessage());
            CBRunUiActivator.getDefault().getLog().log(status);
          }
        }
      }
    }
  }

  /**
   * @see IActionDelegate#selectionChanged(IAction, ISelection)
   */
  public void selectionChanged(IAction action, ISelection selection) {
  }

}
