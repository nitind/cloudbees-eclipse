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

/*    if (this.view.getSelectedJob() instanceof JenkinsJobsResponse.Job) {
      //CloudBeesDevUiPlugin.getDefault().showBuildForJob(((JenkinsJobsResponse.Job) view.getSelectedJob()));
    }
*/
  }

}
