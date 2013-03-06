package com.cloudbees.eclipse.dev.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.cloudbees.eclipse.dev.ui.CBDEVImages;
import com.cloudbees.eclipse.dev.ui.CloudBeesDevUiPlugin;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;
import com.cloudbees.eclipse.ui.views.CBTreeAction;

public class ReloadJenkinsCloudInstancesAction extends CBTreeAction implements IObjectActionDelegate {

  public ReloadJenkinsCloudInstancesAction() {
    super(true);
    setText("Reload DEV@cloud Jenkins instances@");
    setToolTipText("Reload DEV@cloud Jenkins instances");
    setImageDescriptor(CloudBeesDevUiPlugin.getImageDescription(CBDEVImages.IMG_REFRESH));
  }

  @Override
  public void run() {
    CloudBeesUIPlugin.getDefault().reloadAllCloudJenkins(true);
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

  @Override
  public void run(IAction action) {
    run();
  }

  @Override
  public void setActivePart(IAction action, IWorkbenchPart targetPart) {
  }

}
