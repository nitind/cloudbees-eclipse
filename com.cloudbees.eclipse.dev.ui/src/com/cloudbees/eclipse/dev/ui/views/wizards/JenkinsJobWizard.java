package com.cloudbees.eclipse.dev.ui.views.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;

import com.cloudbees.eclipse.core.CloudBeesCorePlugin;
import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.GrandCentralService;
import com.cloudbees.eclipse.core.JenkinsService;
import com.cloudbees.eclipse.core.domain.JenkinsInstance;
import com.cloudbees.eclipse.core.forge.api.ForgeInstance;
import com.cloudbees.eclipse.core.util.Utils;
import com.cloudbees.eclipse.dev.core.CloudBeesDevCorePlugin;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;
import com.cloudbees.eclipse.ui.wizard.CBWizardSupport;
import com.cloudbees.eclipse.ui.wizard.Failure;

public class JenkinsJobWizard extends Wizard {

  private static final String WINDOW_TITLE = "Jenkins Job";
  private static final String ERROR_TITLE = "Error";
  private static final String ERROR_MSG = "Received error while creating new Jenkins job";

  private final IProject project;
  private JenkinsJobWizardPage jenkinsPage;

  public JenkinsJobWizard(final IProject project) {
    this.project = project;
    setNeedsProgressMonitor(true);
    setWindowTitle(WINDOW_TITLE);
    setDefaultPageImageDescriptor(CloudBeesUIPlugin.imageDescriptorFromPlugin(CloudBeesUIPlugin.PLUGIN_ID,
        "icons/cb_wiz_icon.png"));
  }

  @Override
  public void addPages() {
    this.jenkinsPage = new JenkinsJobWizardPage(this.project);
    addPage(this.jenkinsPage);
  }

  @Override
  public boolean performFinish() {
    String jobName = this.jenkinsPage.getJobName();
    boolean isUnderSCM = this.jenkinsPage.isUnderSCM();
    boolean isAddNewRepo = this.jenkinsPage.isAddNewRepo();
    ForgeInstance repo = this.jenkinsPage.getRepo();

    try {
      if (isAddNewRepo) {
        addNewRepo(this.project, repo);
      }

      JenkinsInstance instance = this.jenkinsPage.getJenkinsInstance();
      CloudBeesUIPlugin plugin = CloudBeesUIPlugin.getDefault();
      JenkinsService jenkinsService = plugin.lookupJenkinsService(instance);
      CBWizardSupport.makeJenkinsJob(createConfigXML(isAddNewRepo, isUnderSCM, repo), jenkinsService, jobName,
          getContainer());

    } catch (Exception e) {
      handleException(e);
    }

    return true;
  }

  private void addNewRepo(final IProject project, final ForgeInstance repo) throws Exception {
    final Failure<CloudBeesException> failiure = new Failure<CloudBeesException>();

    IRunnableWithProgress operation = new IRunnableWithProgress() {

      @Override
      public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        try {
          GrandCentralService service = CloudBeesCorePlugin.getDefault().getGrandCentralService();
          service.addToRepository(project, repo, monitor);
        } catch (CloudBeesException e) {
          failiure.cause = e;
        }
      }

    };

    getContainer().run(true, false, operation);
    if (failiure.cause != null) {
      throw failiure.cause;
    }
  }

  private String createConfigXML(final boolean isAddNewRepo, final boolean isUnderSCM, final ForgeInstance repo)
      throws Exception {
    if (isAddNewRepo || isUnderSCM) {
      String description = "Builds " + this.project.getName() + " with SCM support";

      String url = repo.url;

      if (!isUnderSCM) {
        if (!url.endsWith("/")) {
          url += "/";
        }
        url += this.project.getName();
      }

      return Utils.createSCMConfig(description, url);

    } else {
      String description = "Builds " + this.project.getName() + " without SCM support";
      return Utils.createEmptyConfig(description);
    }
  }

  private void handleException(final Exception ex) {
    ex.printStackTrace();
    IStatus status = new Status(IStatus.ERROR, CloudBeesDevCorePlugin.PLUGIN_ID, ex.getMessage(), ex);
    ErrorDialog.openError(getShell(), ERROR_TITLE, ERROR_MSG, status);
  }
}
