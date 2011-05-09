package com.cloudbees.eclipse.dev.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.dev.ui.CBImages;
import com.cloudbees.eclipse.dev.ui.CloudBeesDevUiPlugin;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;
import com.cloudbees.eclipse.ui.views.CBTreeAction;

public class ReloadForgeReposAction extends CBTreeAction implements IObjectActionDelegate {

  public ReloadForgeReposAction() {
    super();
    setText("Refresh Forge repositories");
    setToolTipText("Refresh Forge repositories and create local entries");
    setImageDescriptor(CloudBeesDevUiPlugin.getImageDescription(CBImages.IMG_REFRESH));
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

  @Override
  public void run(IAction action) {
    run();
  }

  @Override
  public void selectionChanged(IAction action, ISelection selection) {
  }

  @Override
  public void setActivePart(IAction action, IWorkbenchPart targetPart) {
  }

}
