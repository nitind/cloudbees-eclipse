/*******************************************************************************
 * Copyright (c) 2013, 2019 Cloud Bees, Inc. and others
 * All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	Cloud Bees, Inc. - initial API and implementation
 * 	IBM Corp. - better support for JobHolders
 *******************************************************************************/
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
import com.cloudbees.eclipse.dev.ui.views.jobs.JobHolder;
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
    boolean previous = super.isEnabled();
    this.job = job;
    super.setEnabled(this.job != null);
    firePropertyChange(ENABLED, previous, this.job != null);
  }

  @Override
  public boolean isEnabled() {
    if (this.job == null) {
      return false;
    }

    Object target = this.job;
    if (target instanceof JobHolder)
      target = ((JobHolder) target).job;
    if (target instanceof JenkinsJobsResponse.Job) {
      return ((JenkinsJobsResponse.Job) target).buildable != null && ((JenkinsJobsResponse.Job) target).buildable;
    } else if (target instanceof JenkinsJobAndBuildsResponse) {
      return ((JenkinsJobAndBuildsResponse) target).buildable != null
          && ((JenkinsJobAndBuildsResponse) target).buildable;
    } else if (target instanceof JenkinsBuild) {
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
      Object target = this.job;
      if (target instanceof JobHolder)
        target = ((JobHolder) target).job;
      if (target instanceof JenkinsJobsResponse.Job) {
        url = ((JenkinsJobsResponse.Job) target).url;
        props = CloudBeesUIPlugin.getDefault().getJobPropValues(((JenkinsJobsResponse.Job) target).property);
      } else if (target instanceof JenkinsJobAndBuildsResponse) {
        url = ((JenkinsJobAndBuildsResponse) target).viewUrl;
        props = CloudBeesUIPlugin.getDefault().getJobPropValues(((JenkinsJobAndBuildsResponse) target).property);
      } else if (target instanceof JenkinsBuild) {
        url = ((JenkinsBuild) target).url;
        // TODO find job to get props?
        props = null; // CloudBeesUIPlugin.getDefault().getJobPropValues(((JenkinsJobsResponse.Job) job).property);
      } else {
        throw new IllegalStateException("Unsupported job type: " + target);
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
