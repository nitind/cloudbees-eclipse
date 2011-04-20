package com.cloudbees.eclipse.run.ui.wizards;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;

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
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.IImportStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;

import com.cloudbees.eclipse.core.CloudBeesCorePlugin;
import com.cloudbees.eclipse.core.CloudBeesNature;
import com.cloudbees.eclipse.core.CoreScripts;
import com.cloudbees.eclipse.core.GrandCentralService;
import com.cloudbees.eclipse.core.NatureUtil;
import com.cloudbees.eclipse.core.domain.JenkinsInstance;
import com.cloudbees.eclipse.core.gc.api.AccountServiceStatusResponse.AccountServices.ForgeService.Repo;
import com.cloudbees.eclipse.run.core.CBRunCoreScripts;
import com.cloudbees.eclipse.run.ui.CBRunUiActivator;
import com.cloudbees.eclipse.ui.wizard.CBWizardSupport;
import com.cloudbees.eclipse.ui.wizard.Failiure;

public class CBWebAppWizardFinishOperation implements IRunnableWithProgress {

  private static final String ERROR_TITLE = "Error";
  private static final String ERROR_MSG = "Received error while creating new project";

  private final CBWebAppWizard wizard;
  private final CBProjectNameAndLocationPage nameAndLocPage;
  private final CBServicesWizardPage servicesPage;

  private IProject project;
  private boolean useDefaultLocation;
  private URI uri;
  private ImportOperation importOperation;
  private boolean isMakeJenkinsJob;
  private boolean isAddNewRepo;
  private Repo repo;
  private String jobName;
  private URI locationURI;
  private Failiure<Exception> failiure;

  public CBWebAppWizardFinishOperation(CBWebAppWizard wizard) {
    this.wizard = wizard;
    this.nameAndLocPage = wizard.getNameAndLocationPage();
    this.servicesPage = wizard.getServicesPage();
    prepare();
  }

  private void prepare() {
    String projectName = this.nameAndLocPage.getProjectName();

    IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
    this.uri = this.nameAndLocPage.getProjectLocationURI();
    this.useDefaultLocation = this.uri == null;
    if (this.useDefaultLocation) {
      this.uri = workspaceRoot.getLocationURI();
    }

    this.project = workspaceRoot.getProject(projectName);
    IPath path = new Path(this.uri.getPath()).append(projectName);
    IPath containerPath = this.project.getFullPath();
    File source = path.toFile();
    IImportStructureProvider structureProvider = FileSystemStructureProvider.INSTANCE;
    IOverwriteQuery overwriteQuery = new IOverwriteQuery() {
      @Override
      public String queryOverwrite(String pathString) {
        return IOverwriteQuery.NO_ALL;
      }
    };

    this.importOperation = new ImportOperation(containerPath, source, structureProvider, overwriteQuery);
    this.isMakeJenkinsJob = this.servicesPage.isMakeNewJob();
    this.isAddNewRepo = this.servicesPage.isAddNewRepository();
    this.repo = this.servicesPage.getRepo();
    this.jobName = this.servicesPage.getJobNameText();
    this.locationURI = URIUtil.append(this.uri, projectName);
    this.failiure = new Failiure<Exception>();
  }

  @Override
  public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
    try {
      if (CBWebAppWizardFinishOperation.this.useDefaultLocation) {
        this.importOperation.setContext(CBWebAppWizardFinishOperation.this.wizard.getShell());
        this.importOperation.setCreateContainerStructure(false);
        this.importOperation.run(monitor);
      } else {
        BuildPathsBlock.createProject(this.project, CBWebAppWizardFinishOperation.this.locationURI, monitor);
      }

      NatureUtil.addNatures(this.project, new String[] { CloudBeesNature.NATURE_ID }, monitor);

      if (CBWebAppWizardFinishOperation.this.isMakeJenkinsJob) {
        File configXML = CoreScripts.getMockConfigXML(); // FIXME
        JenkinsInstance instance = CBWebAppWizardFinishOperation.this.servicesPage.getJenkinsInstance();
        CBWizardSupport.makeJenkinsJob(configXML, instance, CBWebAppWizardFinishOperation.this.jobName,
            CBWebAppWizardFinishOperation.this.wizard.getContainer());
      }

      if (CBWebAppWizardFinishOperation.this.isAddNewRepo) {
        GrandCentralService service = CloudBeesCorePlugin.getDefault().getGrandCentralService();
        service.addToRepository(this.project, this.repo, monitor);
      }
    } catch (Exception e) {
      this.failiure.cause = e;
    } finally {
      monitor.done();
    }
  }

  public boolean performFinish() {

    try {
      CBRunCoreScripts.executeCopySampleWebAppScript(this.uri.getPath(), this.project.getName());
      this.wizard.getContainer().run(true, false, this);
      if (this.failiure.cause != null) {
        handleException(this.failiure.cause);
      }
    } catch (Exception e) {
      handleException(e);
    }

    IWorkingSet[] workingSets = this.nameAndLocPage.getWorkingSets();
    PlatformUI.getWorkbench().getWorkingSetManager().addToWorkingSets(this.project, workingSets);

    return true;
  }

  private void handleException(Exception ex) {
    ex.printStackTrace();
    IStatus status = new Status(IStatus.ERROR, CBRunUiActivator.PLUGIN_ID, ex.getMessage(), ex);
    ErrorDialog.openError(this.wizard.getShell(), ERROR_TITLE, ERROR_MSG, status);
  }
}
