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
package com.cloudbees.eclipse.dev.ui.views.wizards;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.cloudbees.eclipse.core.domain.JenkinsInstance;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse.Job;
import com.cloudbees.eclipse.ui.wizard.CBWizardSupport;
import com.cloudbees.eclipse.ui.wizard.NewJenkinsJobComposite;

public class JenkinsJobWizardPage extends WizardPage {

  public static final String NAME = JenkinsJobWizardPage.class.getSimpleName();
  private static final String TITLE = "Create Jenkins Job";
  private static final String DESCRIPTION = "Create new Jenkins job for this project.";
  private static final String JOB_NAME = "Build {0}";

  private NewJenkinsJobComposite jenkinsComposite;
  private final IProject project;

  protected JenkinsJobWizardPage(final IProject project) {
    super(NAME);
    setTitle(TITLE);
    setDescription(DESCRIPTION);
    this.project = project;
  }

  @Override
  public void createControl(final Composite parent) {
    Composite container = new Composite(parent, SWT.NULL);

    GridLayout layout = new GridLayout(1, true);
    layout.marginHeight = 10;
    layout.marginWidth = 10;
    layout.verticalSpacing = 10;

    container.setLayout(layout);

    GridData data = new GridData();
    data.grabExcessHorizontalSpace = true;
    data.horizontalAlignment = SWT.FILL;

    this.jenkinsComposite = new NewJenkinsJobComposite(container) {

      @Override
      protected JenkinsInstance[] loadJenkinsInstances() {
        try {
          return CBWizardSupport.getJenkinsInstances(getContainer());
        } catch (Exception e) {
          e.printStackTrace();
          return new JenkinsInstance[] {};
        }
      }

      @Override
      protected void createComponents() {
        createInstanceChooser();
        createJobText();
      }

      @Override
      protected void validate() {
        if (getJenkinsInstance() == null) {
          updateErrorStatus(ERR_JENKINS_INSTANCE);
          return;
        }

        String jobNameText = getJobNameText();

        if (jobNameText == null || jobNameText.length() == 0) {
          updateErrorStatus(ERR_JOB_NAME);
          return;
        }

        List<Job> jobs = getInstanceJobs(getJenkinsInstance());
        boolean existingJobWithSameName = false;

        for (Job job : jobs) {
          if (job.name.equals(jobNameText)) {
            existingJobWithSameName = true;
            break;
          }
        }

        if (existingJobWithSameName) {
          updateErrorStatus(ERR_DUPLICATE_JOB_NAME);
          return;
        }

        updateErrorStatus(null);
      }

      @Override
      protected List<Job> loadJobs(JenkinsInstance instance) {
        try {
          return CBWizardSupport.getJenkinsJobs(getContainer(), instance);
        } catch (Exception e) {
          e.printStackTrace();
          return new ArrayList<JenkinsJobsResponse.Job>();
        }
      }

    };

    this.jenkinsComposite.setLayoutData(data);

    String jobName = MessageFormat.format(JOB_NAME, this.project.getName());
    this.jenkinsComposite.setJobNameText(jobName);

    setControl(container);
  }

  public void updateErrorStatus(final String message) {
    setErrorMessage(message);
    setPageComplete(message == null);
  }

  public JenkinsInstance getJenkinsInstance() {
    return this.jenkinsComposite.getJenkinsInstance();
  }

  public String getJobName() {
    return this.jenkinsComposite.getJobNameText();
  }


}
