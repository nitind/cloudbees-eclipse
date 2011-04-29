package com.cloudbees.eclipse.dev.ui.actions;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.dev.ui.CloudBeesDevUiPlugin;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;
import com.cloudbees.eclipse.ui.views.CBTreeAction;

public class ReloadForgeReposAction extends CBTreeAction {

  public ReloadForgeReposAction() {
    super();
    setText("Reload Forge repositories");
    setToolTipText("Reload Forge repositories and create local entries");
    /*    setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
            .getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
     */
  }

  @Override
  public void run() {
    try {
      CloudBeesDevUiPlugin.getDefault().reloadForgeRepos(true);
    } catch (CloudBeesException e) {
      //TODO i18n
      CloudBeesUIPlugin.showError("Failed to reload Forge repositories!", e);
    }
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
