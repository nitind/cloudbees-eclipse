package com.cloudbees.eclipse.run.ui.popup.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.cloudbees.eclipse.run.ui.CBRunUiActivator;
import com.cloudbees.eclipse.run.ui.Images;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;
import com.cloudbees.eclipse.ui.views.CBTreeAction;

public class RefreshAction extends CBTreeAction implements IObjectActionDelegate {

  public RefreshAction() {
    super();
    setText("Refresh Tomcat Apps");
    setToolTipText("Refresh Tomcat apps running in RUN@Cloud");
    setImageDescriptor(CBRunUiActivator.getImageDescription(Images.CLOUDBEES_REFRESH));
  }

  @Override
  public void run() {
    CloudBeesUIPlugin.getDefault().fireApplicationInfoChanged();
  }

  @Override
  public void run(IAction action) {
    CloudBeesUIPlugin.getDefault().fireApplicationInfoChanged();
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
