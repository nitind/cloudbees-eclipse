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

import com.cloudbees.eclipse.core.ClickStartService;
import com.cloudbees.eclipse.core.CloudBeesCorePlugin;
import com.cloudbees.eclipse.core.CloudBeesNature;
import com.cloudbees.eclipse.core.GrandCentralService;
import com.cloudbees.eclipse.core.JenkinsService;
import com.cloudbees.eclipse.core.NatureUtil;
import com.cloudbees.eclipse.core.domain.JenkinsInstance;
import com.cloudbees.eclipse.core.forge.api.ForgeInstance;
import com.cloudbees.eclipse.core.gc.api.ClickStartCreateResponse;
import com.cloudbees.eclipse.core.gc.api.ClickStartTemplate;
import com.cloudbees.eclipse.core.util.Utils;
import com.cloudbees.eclipse.run.core.CBRunCoreScripts;
import com.cloudbees.eclipse.run.ui.CBRunUiActivator;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;
import com.cloudbees.eclipse.ui.wizard.CBWizardSupport;
import com.cloudbees.eclipse.ui.wizard.Failure;

public class CBWebAppWizardFinishOperation implements IRunnableWithProgress {

  private static final String ERROR_TITLE = "Error";
  private static final String ERROR_MSG = "Received error while creating new project";

  private final CBWebAppWizard wizard;
  private final CBProjectNameAndLocationPage nameAndLocPage;
  private final ClickStartTemplateWizardPage clickStartPage;

  private IProject project;
  private boolean useDefaultLocation;
  private URI uri;
  private ImportOperation importOperation;
  //private boolean isMakeJenkinsJob;
  private boolean isAddNewRepo;
  private ClickStartTemplate template;
  private String jobName;
  private URI locationURI;
  private Failure<Exception> failure;

  public CBWebAppWizardFinishOperation(final CBWebAppWizard wizard) {
    this.wizard = wizard;
    this.nameAndLocPage = wizard.getNameAndLocationPage();
    this.clickStartPage = wizard.getClickStartPage();
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
      public String queryOverwrite(final String pathString) {
        return IOverwriteQuery.NO_ALL;
      }
    };

    this.importOperation = new ImportOperation(containerPath, source, structureProvider, overwriteQuery);
    //this.isMakeJenkinsJob = this.clickStartPage.isMakeNewJob();
    //this.isAddNewRepo = this.clickStartPage.isAddNewRepository();
    this.template = this.clickStartPage.getTemplate();
    //this.jobName = this.clickStartPage.getJobNameText();
    this.locationURI = URIUtil.append(this.uri, projectName);
    this.failure = new Failure<Exception>();

    CloudBeesUIPlugin plugin = CloudBeesUIPlugin.getDefault();
    /*JenkinsInstance instance = CBWebAppWizardFinishOperation.this.clickStartPage.getJenkinsInstance();
    if (instance != null) {
      this.jenkinsService = plugin.lookupJenkinsService(instance);
    }*/
  }

  @Override
  public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
    try {

      ClickStartService service = CloudBeesCorePlugin.getDefault().getClickStartService();
      String accountName = CloudBeesCorePlugin.getDefault().getGrandCentralService().getActiveAccountName();
      
      monitor.beginTask("Creating '"+nameAndLocPage.getProjectName()+"' for account '"+accountName+"' using ClickStart template '"+this.template.name+"'", 0);

      // Invoke provisioning request in a separate job
      // If request returns keep polling and reporting progress of the provisioning to the job label
      // As progress=100, bring up a modal background job while configuring and refreshing workspace.
      // Setup:
      // Clone from the repo and have git repo accessible
      // Configure datatools connection to the database
      // Refresh jenkins job lists and run@cloud app list.
      
      
      ClickStartCreateResponse resp = service.create(CBWebAppWizardFinishOperation.this.template.id, accountName, nameAndLocPage.getProjectName());
      System.out.println("CREATED! "+resp.appUrl);
      
/*      if (CBWebAppWizardFinishOperation.this.useDefaultLocation) {
        this.importOperation.setContext(CBWebAppWizardFinishOperation.this.wizard.getShell());
        this.importOperation.setCreateContainerStructure(false);
        this.importOperation.run(monitor);
      } else {
        BuildPathsBlock.createProject(this.project, CBWebAppWizardFinishOperation.this.locationURI, monitor);
      }

      monitor.subTask("adding CloudBees nature");
      NatureUtil.addNatures(this.project, new String[] { CloudBeesNature.NATURE_ID }, monitor);

      if (CBWebAppWizardFinishOperation.this.isAddNewRepo) {
        GrandCentralService service = CloudBeesCorePlugin.getDefault().getGrandCentralService();
        //service.getForgeSyncService().addToRepository(this.repo, this.project, monitor);
      }
*
*
*/    } catch (Exception e) {
      this.failure.cause = e;
    } finally {
      //monitor.done();
    }
  }

  public boolean performFinish() {

    try {
      CBRunCoreScripts.executeCopySampleWebAppScript(this.uri.getPath(), this.project.getName());
      this.wizard.getContainer().run(true, false, this);

      /*if (CBWebAppWizardFinishOperation.this.isMakeJenkinsJob) {
        CBWizardSupport
            .makeJenkinsJob(createConfigXML(), this.jenkinsService, this.jobName, this.wizard.getContainer());
      }
*/
      if (this.failure.cause != null) {
        handleException(this.failure.cause);
      }
    } catch (Exception e) {
      handleException(e);
    }

    IWorkingSet[] workingSets = this.nameAndLocPage.getWorkingSets();
    PlatformUI.getWorkbench().getWorkingSetManager().addToWorkingSets(this.project, workingSets);

    return true;
  }

  private void handleException(final Exception ex) {
    ex.printStackTrace();
    IStatus status = new Status(IStatus.ERROR, CBRunUiActivator.PLUGIN_ID, ex.getMessage(), ex);
    ErrorDialog.openError(this.wizard.getShell(), ERROR_TITLE, ERROR_MSG, status);
  }

/*  private String createConfigXML() throws Exception {
    if (this.isAddNewRepo) {
      String description = "Builds " + this.project.getName() + " with SCM support";

      String url = this.repo.url;
      if (!url.endsWith("/")) {
        url += "/";
      }
      url += this.project.getName();

      return Utils.createSCMConfig(description, url);

    } else {
      String description = "Builds " + this.project.getName() + " without SCM support";
      return Utils.createEmptyConfig(description);
    }
  }
*/}
