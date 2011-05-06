package com.cloudbees.eclipse.dev.ui.actions;

import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;
import com.cloudbees.eclipse.ui.views.CBTreeAction;

public class ReloadJenkinsInstancesAction extends CBTreeAction {

  public ReloadJenkinsInstancesAction() {
    super();
    setText("Reload DEV@cloud Jenkins instances@");
    setToolTipText("Reload DEV@cload Jenkins instances");
    /*    setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
            .getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
     */
  }

  @Override
  public void run() {
    CloudBeesUIPlugin.getDefault().reloadAllJenkins(true);
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
