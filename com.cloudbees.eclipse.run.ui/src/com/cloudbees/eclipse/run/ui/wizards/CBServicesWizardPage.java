package com.cloudbees.eclipse.run.ui.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.cloudbees.eclipse.core.CloudBeesCorePlugin;
import com.cloudbees.eclipse.core.GrandCentralService;
import com.cloudbees.eclipse.core.domain.JenkinsInstance;
import com.cloudbees.eclipse.core.gc.api.AccountServiceStatusResponse;
import com.cloudbees.eclipse.core.gc.api.AccountServiceStatusResponse.AccountServices.ForgeService.Repo;
import com.cloudbees.eclipse.run.ui.CBRunUiActivator;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

public class CBServicesWizardPage extends WizardPage implements CBWizardPageSupport {

  public static final String PAGE_NAME = CBServicesWizardPage.class.getSimpleName();
  private static final String PAGE_TITLE = "CloudBees Configuration";
  private static final String PAGE_DESCRIPTION = "Optionally you can integrate your project with CloudBees services.";

  private CBJenkinsComposite jenkinsComposite;
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

    this.jenkinsComposite = new CBJenkinsComposite(container) {

      @Override
      protected void updateErrorStatus(String errorMsg) {
        CBServicesWizardPage.this.updateErrorStatus(errorMsg);
      }

      @Override
      protected JenkinsInstance[] loadJenkinsInstances() {
        return CBServicesWizardPage.this.getJenkinsInstances();
      }

    };

    this.jenkinsComposite.setLayoutData(data);

    data = new GridData();
    data.grabExcessHorizontalSpace = true;
    data.horizontalAlignment = SWT.FILL;

    this.repositoryComposite = new CBRepositoryComposite(container) {

      @Override
      protected Repo[] getRepos() {
        return CBServicesWizardPage.this.getRepos();
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

  public JenkinsInstance getJenkinsInstance() {
    return this.jenkinsComposite.getJenkinsInstance();
  }

  private JenkinsInstance[] getJenkinsInstances() {
    JenkinsInstance[] result = null;

    final List<JenkinsInstance> instances = new ArrayList<JenkinsInstance>();
    IRunnableWithProgress operation = new IRunnableWithProgress() {

      @Override
      public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        try {
          CloudBeesUIPlugin plugin = CloudBeesUIPlugin.getDefault();
          List<JenkinsInstance> manualInstances = plugin.loadManualJenkinsInstances();
          List<JenkinsInstance> cloudInstances = plugin.loadDevAtCloudInstances(monitor);
          instances.addAll(manualInstances);
          instances.addAll(cloudInstances);
        } catch (Exception e) {
          CBRunUiActivator.logError(e); // TODO
        }
      }
    };

    try {
      getContainer().run(true, false, operation);
      result = new JenkinsInstance[instances.size()];
      instances.toArray(result);
    } catch (Exception e) {
      CBRunUiActivator.logError(e); // TODO
    }

    return result;
  }

  private Repo[] getRepos() {
    Repo[] result = null;

    final List<Repo> repos = new ArrayList<AccountServiceStatusResponse.AccountServices.ForgeService.Repo>();
    IRunnableWithProgress operation = new IRunnableWithProgress() {

      @Override
      public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        try {
          GrandCentralService service = CloudBeesCorePlugin.getDefault().getGrandCentralService();
          repos.addAll(service.getForgeRepos(monitor));
        } catch (Exception e) {
          CBRunUiActivator.logError(e); // TODO
        }
      }
    };

    try {
      getContainer().run(true, false, operation);
      result = new Repo[repos.size()];
      repos.toArray(result);
    } catch (Exception e) {
      CBRunUiActivator.logError(e); // TODO
    }

    return result;
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
