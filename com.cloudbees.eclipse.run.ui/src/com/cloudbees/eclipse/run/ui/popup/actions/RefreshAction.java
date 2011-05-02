package com.cloudbees.eclipse.run.ui.popup.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

public class RefreshAction implements IObjectActionDelegate {

  @Override
  public void run(IAction action) {
    CloudBeesUIPlugin.getDefault().reloadAllJenkins(true);
  }

  @Override
  public void selectionChanged(IAction action, ISelection selection) {
  }

  @Override
  public void setActivePart(IAction action, IWorkbenchPart targetPart) {
  }

}
