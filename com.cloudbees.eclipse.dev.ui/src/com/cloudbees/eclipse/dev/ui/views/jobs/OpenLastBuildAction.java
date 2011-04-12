package com.cloudbees.eclipse.dev.ui.views.jobs;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;

import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse;
import com.cloudbees.eclipse.dev.ui.CBImages;
import com.cloudbees.eclipse.dev.ui.CloudBeesDevUiPlugin;

public class OpenLastBuildAction extends Action {

  private JobsView view;

  public OpenLastBuildAction(JobsView jobsView) {
    super("Open last build details", Action.AS_PUSH_BUTTON | SWT.NO_FOCUS);
    setToolTipText("Open last build details of this job"); //TODO i18n
    setImageDescriptor(CloudBeesDevUiPlugin.getImageDescription(CBImages.IMG_BUILD_DETAILS));
    setEnabled(false);
    this.view = jobsView;
  }

  @Override
  public void runWithEvent(Event event) {

    if (view.selectedJob instanceof JenkinsJobsResponse.Job) {
      CloudBeesDevUiPlugin.getDefault().showBuildForJob(((JenkinsJobsResponse.Job) view.selectedJob));
    }

  }

}
