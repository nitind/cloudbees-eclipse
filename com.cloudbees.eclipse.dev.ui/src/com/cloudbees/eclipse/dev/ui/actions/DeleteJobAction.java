/*******************************************************************************
 * Copyright (c) 2013 Cloud Bees, Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *              http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.cloudbees.eclipse.dev.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse;
import com.cloudbees.eclipse.dev.ui.CBDEVImages;
import com.cloudbees.eclipse.dev.ui.CloudBeesDevUiPlugin;
import com.cloudbees.eclipse.dev.ui.views.jobs.JobsView;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

public class DeleteJobAction extends Action {

  private JobsView view;

  public DeleteJobAction(final JobsView jobsView) {
    super("Delete Job...", Action.AS_PUSH_BUTTON | SWT.NO_FOCUS);
    setToolTipText("Deletes the build job"); //TODO i18n
    setImageDescriptor(CloudBeesDevUiPlugin.getImageDescription(CBDEVImages.IMG_DELETE));
    setEnabled(false);
    this.view = jobsView;
  }

  @Override
  public void runWithEvent(final Event event) {

    if (this.view.getSelectedJob()!=null &&  this.view.getSelectedJob().job instanceof JenkinsJobsResponse.Job) {
      try {
        CloudBeesDevUiPlugin.getDefault().deleteJob(((JenkinsJobsResponse.Job) this.view.getSelectedJob().job));
        CloudBeesDevUiPlugin.getDefault().showJobs(this.view.getReloadJobsAction().viewUrl, false);
      } catch (CloudBeesException e) {
        CloudBeesUIPlugin.getDefault().showError("Failed to refresh the jobs list", e);
      }
    }

  }

}
