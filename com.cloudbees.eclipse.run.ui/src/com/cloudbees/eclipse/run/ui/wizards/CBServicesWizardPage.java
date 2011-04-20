package com.cloudbees.eclipse.run.ui.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.cloudbees.eclipse.core.domain.JenkinsInstance;
import com.cloudbees.eclipse.core.forge.api.ForgeSync;
import com.cloudbees.eclipse.core.gc.api.AccountServiceStatusResponse.AccountServices.ForgeService.Repo;
import com.cloudbees.eclipse.run.ui.CBRunUiActivator;
import com.cloudbees.eclipse.ui.wizard.CBWizardPage;
import com.cloudbees.eclipse.ui.wizard.CBWizardSupport;
import com.cloudbees.eclipse.ui.wizard.NewJenkinsJobComposite;

public class CBServicesWizardPage extends WizardPage implements CBWizardPage {

  public static final String PAGE_NAME = CBServicesWizardPage.class.getSimpleName();
  private static final String PAGE_TITLE = "CloudBees Configuration";
  private static final String PAGE_DESCRIPTION = "Optionally you can integrate your project with CloudBees services.";

  private NewJenkinsJobComposite jenkinsComposite;
  private CBRepositoryComposite repositoryComposite;

  protected CBServicesWizardPage() {
    super(PAGE_NAME);
    setTitle(PAGE_TITLE);
    setDescription(PAGE_DESCRIPTION);
  }

  @Override
  public void createControl(Composite parent) {
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

        updateErrorStatus(null);
      }

    };

    this.jenkinsComposite.setLayoutData(data);

    data = new GridData();
    data.grabExcessHorizontalSpace = true;
    data.horizontalAlignment = SWT.FILL;

    this.repositoryComposite = new CBRepositoryComposite(container) {

      @Override
      protected Repo[] getRepos() {
        try {
          return CBWizardSupport.getRepos(getContainer(), ForgeSync.TYPE.SVN);
        } catch (Exception e) {
          CBRunUiActivator.logError(e); // TODO
          return new Repo[] {};
        }
      }

      @Override
      protected void updateErrorStatus(String errorMsg) {
        CBServicesWizardPage.this.updateErrorStatus(errorMsg);
      }

    };

    this.repositoryComposite.setLayoutData(data);

    setControl(container);
  }

  public String getJobNameText() {
    return this.jenkinsComposite.getJobNameText();
  }

  public void setJobNameText(String text) {
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

  public Repo getRepo() {
    return this.repositoryComposite.getSelectedRepo();
  }

  public void updateErrorStatus(String message) {
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

}
