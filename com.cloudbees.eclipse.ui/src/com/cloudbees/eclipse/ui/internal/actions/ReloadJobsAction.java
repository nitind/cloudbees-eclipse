package com.cloudbees.eclipse.ui.internal.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.ui.CBImages;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

public class ReloadJobsAction extends Action {

  public String viewUrl;

  public ReloadJobsAction() {
    super();

    setText("Reload Jenkins jobs");
    setToolTipText("Reload Jenkins jobs");
    setImageDescriptor(CloudBeesUIPlugin.getImageDescription(CBImages.IMG_REFRESH));

    setAccelerator(SWT.F5);

  }

  @Override
  public void runWithEvent(Event event) {

    try {
      CloudBeesUIPlugin.getDefault().showJobs(viewUrl, true);
    } catch (CloudBeesException e) {
      //TODO i18n
      CloudBeesUIPlugin.showError("Failed to reload Jenkins jobs!", e);
    }

  }

}
