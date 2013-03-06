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
package com.cloudbees.eclipse.run.ui.wizards;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.cloudbees.eclipse.core.domain.JenkinsInstance;
import com.cloudbees.eclipse.core.forge.api.ForgeInstance;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse.Job;
import com.cloudbees.eclipse.run.ui.CBRunUiActivator;
import com.cloudbees.eclipse.ui.wizard.CBWizardPage;
import com.cloudbees.eclipse.ui.wizard.CBWizardSupport;
import com.cloudbees.eclipse.ui.wizard.NewJenkinsJobComposite;
import com.cloudbees.eclipse.ui.wizard.SelectRepositoryComposite;

public class CBServicesWizardPage extends WizardPage implements CBWizardPage {

  public static final String PAGE_NAME = CBServicesWizardPage.class.getSimpleName();
  private static final String PAGE_TITLE = "CloudBees Configuration";
  private static final String PAGE_DESCRIPTION = "Optionally you can integrate your project with CloudBees services.";

  private NewJenkinsJobComposite jenkinsComposite;
  private SelectRepositoryComposite repositoryComposite;

  protected CBServicesWizardPage() {
    super(PAGE_NAME);
    setTitle(PAGE_TITLE);
    setDescription(PAGE_DESCRIPTION);
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
          CBRunUiActivator.logError(e); // TODO
          return new JenkinsInstance[] {};
        }
      }

      @Override
      protected void createComponents() {
        createJobCheck();
        createInstanceChooser();
        createJobText();
      }

      @Override
      protected void validate() {
        if (!isMakeNewJob()) {
          updateErrorStatus(null);
          return;
        }

        String jobName = getJobNameText();

        if (jobName == null || jobName.length() == 0) {
          updateErrorStatus(ERR_JOB_NAME);
          return;
        }

        if (getJenkinsInstance() == null) {
          updateErrorStatus(ERR_JENKINS_INSTANCE);
          return;
        }

        List<Job> jobs = getInstanceJobs(getJenkinsInstance());
        boolean existingJobWithSameName = false;

        for (Job job : jobs) {
          if (job.name.equals(jobName)) {
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
    this.jenkinsComposite.addJobCheckListener(this.checkListener);

    data = new GridData();
    data.grabExcessHorizontalSpace = true;
    data.horizontalAlignment = SWT.FILL;

    this.repositoryComposite = new SelectRepositoryComposite(container) {

      @Override
      protected ForgeInstance[] getRepos() {
        try {
          return CBWizardSupport.getRepos(getContainer(), ForgeInstance.TYPE.SVN);
        } catch (Exception e) {
          CBRunUiActivator.logError(e); // TODO
          return new ForgeInstance[0];
        }
      }

      @Override
      protected void updateErrorStatus(final String errorMsg) {
        CBServicesWizardPage.this.updateErrorStatus(errorMsg);
      }

    };

    this.repositoryComposite.setLayoutData(data);
    this.repositoryComposite.addRepoCheckListener(this.checkListener);

    setControl(container);
  }

  public String getJobNameText() {
    return this.jenkinsComposite.getJobNameText();
  }

  public void setJobNameText(final String text) {
    this.jenkinsComposite.setJobNameText(text);
  }

  public boolean isMakeNewJob() {
    return this.jenkinsComposite.isMakeNewJob();
  }

  public boolean isAddNewRepository() {
    return this.repositoryComposite.isAddNewRepo();
  }

  public JenkinsInstance getJenkinsInstance() {
    return this.jenkinsComposite.getJenkinsInstance();
  }

  public ForgeInstance getRepo() {
    return this.repositoryComposite.getSelectedRepo();
  }

  public void updateErrorStatus(final String message) {
    setErrorMessage(message);
    setPageComplete(message == null);
  }

  @Override
  public boolean canFinish() {
    return true;
  }

  @Override
  public boolean isActivePage() {
    return isCurrentPage();
  }

  private final SelectionListener checkListener = new SelectionListener() {

    @Override
    public void widgetSelected(final SelectionEvent e) {
      handleSelected();
    }

    @Override
    public void widgetDefaultSelected(final SelectionEvent e) {
      handleSelected();
    }

    private void handleSelected() {
      boolean makeJob = CBServicesWizardPage.this.jenkinsComposite.isMakeNewJob();
      boolean makeRepo = CBServicesWizardPage.this.repositoryComposite.isAddNewRepo();
      updatePage(makeJob, makeRepo);
    }
  };

  private void updatePage(final boolean makeJob, final boolean makeRepo) {
    if (makeJob && !makeRepo) {
      setMessage("Enable hosting in Forge to configure Jenkins job SCM automatically.", WARNING);
      return;
    }

    setMessage(null);
  }

}
