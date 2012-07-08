package com.cloudbees.eclipse.dev.ui.actions;

import java.util.Map;
import java.util.concurrent.CancellationException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.JenkinsService;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsBuild;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobAndBuildsResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse;
import com.cloudbees.eclipse.dev.ui.CBDEVImages;
import com.cloudbees.eclipse.dev.ui.CloudBeesDevUiPlugin;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

public class InvokeBuildAction extends Action {

  protected Object job;

  public InvokeBuildAction() {
    super("Run a new build", Action.AS_PUSH_BUTTON | SWT.NO_FOCUS); //$NON-NLS-1$
    setToolTipText("Run a new build for this job"); //TODO i18n
    setImageDescriptor(CloudBeesDevUiPlugin.getImageDescription(CBDEVImages.IMG_RUN));
    super.setEnabled(false);
  }

  public void setJob(final Object job) {
    this.job = job;
    super.setEnabled(this.job != null);
  }

  @Override
  public void setEnabled(final boolean enable) {
    // ignore
  }

  @Override
  public boolean isEnabled() {
    if (this.job == null) {
      return false;
    }

    if (this.job instanceof JenkinsJobsResponse.Job) {
      return ((JenkinsJobsResponse.Job) this.job).buildable != null && ((JenkinsJobsResponse.Job) this.job).buildable;
    } else if (this.job instanceof JenkinsJobAndBuildsResponse) {
      return ((JenkinsJobAndBuildsResponse) this.job).buildable != null
          && ((JenkinsJobAndBuildsResponse) this.job).buildable;
    } else if (this.job instanceof JenkinsBuild) {
      return true;
    }

    return false;
  }

  @Override
  public void run() {
    try {
      if (this.job == null) {
        return;
      }
      final String url;
      final Map<String, String> props;
      if (this.job instanceof JenkinsJobsResponse.Job) {
        url = ((JenkinsJobsResponse.Job) this.job).url;
        props = CloudBeesUIPlugin.getDefault().getJobPropValues(((JenkinsJobsResponse.Job) this.job).property);
      } else if (this.job instanceof JenkinsJobAndBuildsResponse) {
        url = ((JenkinsJobAndBuildsResponse) this.job).viewUrl;
        props = CloudBeesUIPlugin.getDefault().getJobPropValues(((JenkinsJobAndBuildsResponse) this.job).property);
      } else if (this.job instanceof JenkinsBuild) {
        url = ((JenkinsBuild) this.job).url;
        // TODO find job to get props?
        props = null; // CloudBeesUIPlugin.getDefault().getJobPropValues(((JenkinsJobsResponse.Job) job).property);
      } else {
        throw new IllegalStateException("Unsupported job type: " + this.job);
      }

      final JenkinsService ns = CloudBeesUIPlugin.getDefault().getJenkinsServiceForUrl(url);
      org.eclipse.core.runtime.jobs.Job sjob = new org.eclipse.core.runtime.jobs.Job("Building job...") {
        @Override
        protected IStatus run(final IProgressMonitor monitor) {
          try {
            ns.invokeBuild(url, props, monitor);
            return org.eclipse.core.runtime.Status.OK_STATUS;
          } catch (CloudBeesException e) {
            //CloudBeesUIPlugin.getDefault().getLogger().error(e);
            return new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.Status.ERROR,
                CloudBeesUIPlugin.PLUGIN_ID, 0, e.getLocalizedMessage(), e.getCause());
          }
        }
      };
      sjob.setUser(true);
      sjob.schedule();
    } catch (CancellationException e) {
      // cancelled by user
    }
  }

}
