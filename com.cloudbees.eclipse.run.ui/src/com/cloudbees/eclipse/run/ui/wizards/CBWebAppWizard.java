package com.cloudbees.eclipse.run.ui.wizards;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.text.MessageFormat;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.BuildPathsBlock;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.IImportStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

import com.cloudbees.eclipse.core.CloudBeesNature;
import com.cloudbees.eclipse.core.JenkinsService;
import com.cloudbees.eclipse.core.NatureUtil;
import com.cloudbees.eclipse.run.core.CBRunCoreScripts;
import com.cloudbees.eclipse.run.ui.CBRunUiActivator;
import com.cloudbees.eclipse.run.ui.Images;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

public class CBWebAppWizard extends BasicNewResourceWizard implements INewWizard {

  private static final String WINDOW_TITLE = "CloudBees Project";
  private static final String ERROR_TITLE = "Error";
  private static final String ERROR_MSG = "Received error while creating new project";
  private static final String BUILD_LABEL = "Build {0}";

  private CBProjectNameAndLocationPage nameAndLocPage;
  private CBJenkinsWizardPage jenkinsPage;

  public CBWebAppWizard() {
    super();
    setNeedsProgressMonitor(true);
    setWindowTitle(WINDOW_TITLE);
    setDefaultPageImageDescriptor(CBRunUiActivator.imageDescriptorFromPlugin(CBRunUiActivator.PLUGIN_ID,
        Images.CLOUDBEES_WIZ_ICON_PATH));
  }

  @Override
  public void addPages() {
    this.nameAndLocPage = new CBProjectNameAndLocationPage();
    addPage(this.nameAndLocPage);

    this.jenkinsPage = new CBJenkinsWizardPage();
    addPage(this.jenkinsPage);

    this.nameAndLocPage.init(getSelection(), getActivePart());
  }

  @Override
  public IWizardPage getNextPage(final IWizardPage page) {
    if (page instanceof CBJenkinsWizardPage) {
      CBJenkinsWizardPage jenkinsPage = (CBJenkinsWizardPage) page;
      String jobName = jenkinsPage.getJobNameText();

      if (jobName == null || jobName.length() == 0) {
        jenkinsPage.setJobNameText(MessageFormat.format(BUILD_LABEL, this.nameAndLocPage.getProjectName()));
      }

      jenkinsPage.loadJenkinsInstances();
    }

    return super.getNextPage(page);
  }

  @Override
  public boolean canFinish() {
    for (IWizardPage page : getPages()) {
      if (!(page instanceof CBWizardPageSupport)) {
        continue;
      }

      CBWizardPageSupport p = (CBWizardPageSupport) page;
      if (p.isActivePage()) {
        return super.canFinish() && p.canFinish();
      }
    }
    return super.canFinish();
  }

  @Override
  public boolean performFinish() {

    IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();

    String projectName = this.nameAndLocPage.getProjectName();
    URI uri = this.nameAndLocPage.getProjectLocationURI();
    final boolean useDefaultLocation = uri == null;
    if (useDefaultLocation) {
      uri = workspaceRoot.getLocationURI();
    }

    try {
      CBRunCoreScripts.executeCopySampleWebAppScript(uri.getPath(), projectName);
    } catch (Exception e) {
      CBRunUiActivator.logError(e);
      MessageDialog.openError(getShell(), ERROR_TITLE, e.getMessage());
      return false;
    }

    final IProject project = workspaceRoot.getProject(projectName);
    IPath path = new Path(uri.getPath()).append(projectName);
    IPath containerPath = project.getFullPath();
    File source = path.toFile();
    IImportStructureProvider structureProvider = FileSystemStructureProvider.INSTANCE;
    IOverwriteQuery overwriteQuery = new IOverwriteQuery() {

      @Override
      public String queryOverwrite(final String pathString) {
        return IOverwriteQuery.NO_ALL;
      }
    };

    final ImportOperation importOp = new ImportOperation(containerPath, source, structureProvider, overwriteQuery);
    final boolean isMakeJenkinsJob = this.jenkinsPage.isMakeNewJob();
    final String jobName = this.jenkinsPage.getJobNameText();
    final URI locationURI = URIUtil.append(uri, projectName);

    IRunnableWithProgress progress = new IRunnableWithProgress() {

      @SuppressWarnings("restriction")
      @Override
      public void run(final IProgressMonitor monitor) throws InvocationTargetException {

        try {
          if (useDefaultLocation) {
            importOp.setContext(getShell());
            importOp.setCreateContainerStructure(false);
            importOp.run(monitor);
          } else {
            BuildPathsBlock.createProject(project, locationURI, monitor);
          }
          NatureUtil.addNatures(project, new String[] { CloudBeesNature.NATURE_ID }, monitor);
          if (isMakeJenkinsJob) {
            makeJenkinsJob(jobName, monitor);
          }
        } catch (final Exception e) {
          e.printStackTrace();
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
      e.printStackTrace();
      return false;
    } catch (InvocationTargetException e) {
      e.printStackTrace();
      CBRunUiActivator.logError(e);
      Throwable targetEx = e.getTargetException();
      MessageDialog.openError(getShell(), ERROR_TITLE, targetEx.getMessage());
      return false;
    }

    IWorkingSet[] workingSets = this.nameAndLocPage.getWorkingSets();
    getWorkbench().getWorkingSetManager().addToWorkingSets(project, workingSets);

    return true;
  }

  private void makeJenkinsJob(final String jobName, final IProgressMonitor monitor) throws Exception {
    File configXML = CBRunCoreScripts.getMockConfigXML();
    CloudBeesUIPlugin plugin = CloudBeesUIPlugin.getDefault();
    JenkinsService jenkinsService = plugin.lookupJenkinsService(this.jenkinsPage.getJenkinsInstance());
    jenkinsService.createJenkinsJob(jobName, configXML, monitor);
  }

  private IWorkbenchPart getActivePart() {
    IWorkbenchWindow activeWindow = getWorkbench().getActiveWorkbenchWindow();
    if (activeWindow != null) {
      IWorkbenchPage activePage = activeWindow.getActivePage();
      if (activePage != null) {
        return activePage.getActivePart();
      }
    }
    return null;
  }
}
