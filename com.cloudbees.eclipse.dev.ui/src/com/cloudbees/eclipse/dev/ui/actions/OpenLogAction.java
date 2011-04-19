package com.cloudbees.eclipse.dev.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;

import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse.Job;
import com.cloudbees.eclipse.dev.ui.CBImages;
import com.cloudbees.eclipse.dev.ui.CloudBeesDevUiPlugin;
import com.cloudbees.eclipse.dev.ui.views.jobs.JobsView;

/**
 * @author antons
 */
public class OpenLogAction extends Action {

  private JobsView view;

  public OpenLogAction(final JobsView jobsView) {
    super("Open console log", Action.AS_PUSH_BUTTON | SWT.NO_FOCUS);
    setToolTipText("Open console log of this job"); //TODO i18n
    setImageDescriptor(CloudBeesDevUiPlugin.getImageDescription(CBImages.IMG_BUILD_CONSOLE_LOG));
    setEnabled(false);
    this.view = jobsView;
  }

  @Override
  public boolean isEnabled() {
    if (this.view.getSelectedJob() instanceof JenkinsJobsResponse.Job) {
      Job job = ((JenkinsJobsResponse.Job) this.view.getSelectedJob());
      if (job.lastBuild != null) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void runWithEvent(final Event event) {
    if (this.view.getSelectedJob() instanceof JenkinsJobsResponse.Job) {
      Job job = ((JenkinsJobsResponse.Job) this.view.getSelectedJob());
      if (job.lastBuild != null) {
        CloudBeesDevUiPlugin.getDefault().getJobConsoleManager().showConsole(job.lastBuild);
      }
    }
  }

}
