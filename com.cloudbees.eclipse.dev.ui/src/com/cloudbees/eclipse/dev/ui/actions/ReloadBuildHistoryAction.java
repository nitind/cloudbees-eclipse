package com.cloudbees.eclipse.dev.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.dev.ui.CBImages;
import com.cloudbees.eclipse.dev.ui.CloudBeesDevUiPlugin;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

public class ReloadBuildHistoryAction extends Action {

  private String viewUrl;

  public ReloadBuildHistoryAction(final boolean reload) {
    super();

    if (reload) {
      setText("Reload build history");
      setToolTipText("Reload build history");
      setImageDescriptor(CloudBeesDevUiPlugin.getImageDescription(CBImages.IMG_REFRESH));
      setAccelerator(SWT.F5);
    } else {
      setText("Open build history");
      setToolTipText("Open build history");
      setImageDescriptor(CloudBeesDevUiPlugin.getImageDescription(CBImages.IMG_BUILD_HISTORY));
    }
  }

  public void setViewUrl(final String viewUrl) {
    this.viewUrl = viewUrl;
  }

  @Override
  public boolean isEnabled() {
    return this.viewUrl == null ? false : super.isEnabled();
  }

  @Override
  public void runWithEvent(final Event event) {

    try {
      CloudBeesDevUiPlugin.getDefault().showBuildHistory(this.viewUrl, true);
    } catch (CloudBeesException e) {
      //TODO i18n
      CloudBeesUIPlugin.showError("Failed to reload Jenkins jobs!", e);
    }
  }

}
