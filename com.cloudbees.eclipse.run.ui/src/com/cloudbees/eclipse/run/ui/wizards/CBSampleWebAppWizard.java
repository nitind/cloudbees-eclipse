package com.cloudbees.eclipse.run.ui.wizards;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.IImportStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;

import com.cloudbees.eclipse.core.CloudBeesNature;
import com.cloudbees.eclipse.core.JenkinsService;
import com.cloudbees.eclipse.core.NatureUtil;
import com.cloudbees.eclipse.run.core.CBRunCoreScripts;
import com.cloudbees.eclipse.run.ui.CBRunUiActivator;
import com.cloudbees.eclipse.run.ui.Images;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

public class CBSampleWebAppWizard extends Wizard implements INewWizard {

  private static final String WINDOW_TITLE = "Sample CloudBees Project";
  private static final String ERROR_TITLE = "Error";
  private static final String ERROR_MSG = "Received error while creating new project";
  private static final String BUILD_LABEL = "Build {0}";

  private CBSampleWebAppWizardPage newProjectPage;
  private JenkinsWizardPage jenkinsPage;

  public CBSampleWebAppWizard() {
    super();
    setNeedsProgressMonitor(true);
    setWindowTitle(WINDOW_TITLE);
    setDefaultPageImageDescriptor(CBRunUiActivator.imageDescriptorFromPlugin(CBRunUiActivator.PLUGIN_ID,
        Images.CLOUDBEES_WIZ_ICON_PATH));
  }

  @Override
  public void addPages() {
    this.newProjectPage = new CBSampleWebAppWizardPage();
    addPage(this.newProjectPage);
    this.jenkinsPage = new JenkinsWizardPage();
    addPage(this.jenkinsPage);
  }

  @Override
  public void init(IWorkbench workbench, IStructuredSelection selection) {
  }

  @Override
  public IWizardPage getNextPage(IWizardPage page) {
    if (page instanceof JenkinsWizardPage) {
      JenkinsWizardPage jenkinsPage = (JenkinsWizardPage) page;
      String jobName = jenkinsPage.getJobNameText();

      if (jobName == null || jobName.length() == 0) {
        jenkinsPage.setJobNameText(MessageFormat.format(BUILD_LABEL, this.newProjectPage.getProjectName()));
      }

      jenkinsPage.loadJenkinsInstances();
    }

    return super.getNextPage(page);
  }

  @Override
  public boolean canFinish() {
    for (IWizardPage page : getPages()) {
      if (!(page instanceof CBWizardPage)) {
        continue;
      }

      CBWizardPage cbPage = (CBWizardPage) page;
      if (cbPage.isActivePage()) {
        return super.canFinish() && cbPage.canFinish();
      }
    }
    return super.canFinish();
  }

  @Override
  public boolean performFinish() {

    IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();

    String projectName = this.newProjectPage.getProjectName();
    String worksapceLocation = workspaceRoot.getLocation().toPortableString();

    try {
      CBRunCoreScripts.executeCopySampleWebAppScript(worksapceLocation, projectName);
    } catch (Exception e) {
      CBRunUiActivator.logError(e);
      MessageDialog.openError(getShell(), ERROR_TITLE, e.getMessage());
      return false;
    }

    final IProject project = workspaceRoot.getProject(projectName);
    IPath containerPath = project.getFullPath();
    File source = workspaceRoot.getLocation().append(projectName).toFile();
    IImportStructureProvider structureProvider = FileSystemStructureProvider.INSTANCE;
    IOverwriteQuery overwriteQuery = new IOverwriteQuery() {

      @Override
      public String queryOverwrite(String pathString) {
        return IOverwriteQuery.ALL;
      }
    };

    final ImportOperation importOp = new ImportOperation(containerPath, source, structureProvider, overwriteQuery);
    final boolean isMakeJenkinsJob = this.jenkinsPage.isMakeNewJob();
    final String jobName = this.jenkinsPage.getJobNameText();

    IRunnableWithProgress progress = new IRunnableWithProgress() {

      @Override
      public void run(IProgressMonitor monitor) throws InvocationTargetException {

        try {
          importOp.setContext(getShell());
          importOp.setCreateContainerStructure(false);
          importOp.run(monitor);
          NatureUtil.addNatures(project, new String[] { CloudBeesNature.NATURE_ID }, monitor);
          if (isMakeJenkinsJob) {
            makeJenkinsJob(jobName, monitor);
          }
        } catch (final Exception e) {
          CBRunUiActivator.logError(e);
          getShell().getDisplay().syncExec(new Runnable() {
            @Override
            public void run() {
              IStatus status = new Status(IStatus.ERROR, CBRunUiActivator.PLUGIN_ID, e.getMessage(), e.getCause());
              ErrorDialog.openError(getShell(), ERROR_TITLE, ERROR_MSG, status);
            }
          });
        } finally {
          monitor.done();
        }

      }
    };

    try {
      getContainer().run(true, false, progress);
    } catch (InterruptedException e) {
      return false;
    } catch (InvocationTargetException e) {
      CBRunUiActivator.logError(e);
      Throwable targetEx = e.getTargetException();
      MessageDialog.openError(getShell(), ERROR_TITLE, targetEx.getMessage());
      return false;
    }

    return true;
  }

  private void makeJenkinsJob(String jobName, IProgressMonitor monitor) throws Exception {
    File configXML = CBRunCoreScripts.getMockConfigXML();
    CloudBeesUIPlugin plugin = CloudBeesUIPlugin.getDefault();
    JenkinsService jenkinsService = plugin.lookupJenkinsService(this.jenkinsPage.getJenkinsInstance());
    jenkinsService.createJenkinsJob(jobName, configXML, monitor);
  }

}
