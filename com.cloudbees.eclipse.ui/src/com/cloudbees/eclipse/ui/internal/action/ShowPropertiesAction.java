package com.cloudbees.eclipse.ui.internal.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class ShowPropertiesAction implements IObjectActionDelegate {

  public void run(IAction action) {
    try {
      PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(IPageLayout.ID_PROP_SHEET);
    } catch (PartInitException e) {
    }
  }

  public void selectionChanged(IAction action, ISelection selection) {
  }

  public void setActivePart(IAction action, IWorkbenchPart targetPart) {
  }

}
