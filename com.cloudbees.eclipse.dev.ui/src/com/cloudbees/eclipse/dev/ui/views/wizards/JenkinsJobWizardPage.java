package com.cloudbees.eclipse.dev.ui.views.wizards;

import java.text.MessageFormat;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.cloudbees.eclipse.core.CloudBeesCorePlugin;
import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.domain.JenkinsInstance;
import com.cloudbees.eclipse.core.forge.api.ForgeInstance;
import com.cloudbees.eclipse.ui.wizard.CBWizardSupport;
import com.cloudbees.eclipse.ui.wizard.NewJenkinsJobComposite;
import com.cloudbees.eclipse.ui.wizard.SelectRepositoryComposite;

public class JenkinsJobWizardPage extends WizardPage {

  public static final String NAME = JenkinsJobWizardPage.class.getSimpleName();
  private static final String TITLE = "Create Jenkins Job";
  private static final String DESCRIPTION = "Create new Jenkins job for this project.";
  private static final String JOB_NAME = "Build {0}";

  private NewJenkinsJobComposite jenkinsComposite;
  private SelectRepositoryComposite repoComposite;
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
        addJenkinsInstancesToUI();
      }

      @Override
      protected void validate() {
        if (getJenkinsInstance() == null) {
          updateErrorStatus("Please select Jenkins instance.");
          return;
        }

        if (getJobNameText() == null || getJobNameText().length() == 0) {
          updateErrorStatus("Please specify Jenkins job name.");
          return;
        }

        updateErrorStatus(null);
      }

    };

    this.jenkinsComposite.setLayoutData(data);

    if (!isUnderSCM()) {
      this.repoComposite = new SelectRepositoryComposite(container) {

        @Override
        protected void updateErrorStatus(final String errorMsg) {
          JenkinsJobWizardPage.this.updateErrorStatus(errorMsg);
        }

        @Override
        protected ForgeInstance[] getRepos() {
          try {
            return CBWizardSupport.getRepos(getContainer(), ForgeInstance.TYPE.SVN);
          } catch (Exception e) {
            e.printStackTrace(); // FIXME
            return new ForgeInstance[0];
          }
        }
      };

      this.repoComposite.setLayoutData(data);

      RepoCheckListener repoCheckListener = new RepoCheckListener();
      this.repoComposite.addRepoCheckListener(repoCheckListener);
      repoCheckListener.handleSelection();
    }

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

  public ForgeInstance getRepo() {
    if (this.repoComposite != null) {
      return this.repoComposite.getSelectedRepo();
    } else {
      try {
        return CloudBeesCorePlugin.getDefault().getGrandCentralService().getForgeSyncService().getSvnRepo(this.project);
      } catch (CloudBeesException e) {
        e.printStackTrace();
      }
    }
    return null;
  }

  public boolean isAddNewRepo() {
    if (this.repoComposite != null) {
      return this.repoComposite.isAddNewRepo();
    }
    return false;
  }

  public boolean isUnderSCM() {
    try {
      return CloudBeesCorePlugin.getDefault().getGrandCentralService().getForgeSyncService().isUnderSvnScm(this.project);
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  private class RepoCheckListener implements SelectionListener {

    @Override
    public void widgetSelected(final SelectionEvent e) {
      handleSelection();
    }

    @Override
    public void widgetDefaultSelected(final SelectionEvent e) {
      handleSelection();
    }

    public void handleSelection() {
      if (JenkinsJobWizardPage.this.repoComposite.isAddNewRepo()) {
        setMessage(null);
      } else {
        setMessage("Enable hosting in Forge to configure Jenkins job SCM automatically.", WARNING);
      }
    }
  }
}
