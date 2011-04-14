package com.cloudbees.eclipse.dev.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;

import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse;
import com.cloudbees.eclipse.dev.ui.views.jobs.JobsView;

public class SetJobDescriptionAction extends Action {

  private JobsView view;

  public SetJobDescriptionAction(final JobsView jobsView) {
    super("Set description...", Action.AS_PUSH_BUTTON | SWT.NO_FOCUS);
    setToolTipText("Sets the description for this job"); //TODO i18n
    //setImageDescriptor(CloudBeesDevUiPlugin.getImageDescription(CBImages.IMG_BUILD_DETAILS));
    setEnabled(false);
    this.view = jobsView;
  }

  @Override
  public void runWithEvent(final Event event) {

    if (this.view.getSelectedJob() instanceof JenkinsJobsResponse.Job) {
      //CloudBeesDevUiPlugin.getDefault().showBuildForJob(((JenkinsJobsResponse.Job) view.getSelectedJob()));
    }

  }

}
