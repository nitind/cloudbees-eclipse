package com.cloudbees.eclipse.dev.ui.views.jobs;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse;
import com.cloudbees.eclipse.dev.ui.CBImages;
import com.cloudbees.eclipse.dev.ui.CloudBeesDevUiPlugin;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

public class DeleteJobAction extends Action {

  private JobsView view;

  public DeleteJobAction(JobsView jobsView) {
    super("Delete Job...", Action.AS_PUSH_BUTTON | SWT.NO_FOCUS);
    setToolTipText("Deletes the build job"); //TODO i18n
    setImageDescriptor(CloudBeesDevUiPlugin.getImageDescription(CBImages.IMG_DELETE));
    setEnabled(false);
    this.view = jobsView;
  }

  @Override
  public void runWithEvent(Event event) {

    if (view.selectedJob instanceof JenkinsJobsResponse.Job) {
      try {
        CloudBeesDevUiPlugin.getDefault().deleteJob(((JenkinsJobsResponse.Job) view.selectedJob));
        CloudBeesDevUiPlugin.getDefault().showJobs(view.actionReloadJobs.viewUrl, false);
      } catch (CloudBeesException e) {
        CloudBeesUIPlugin.getDefault().showError("Failed to refresh the jobs list", e);
      }
    }

  }

}
