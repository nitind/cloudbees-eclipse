package com.cloudbees.eclipse.run.ui.wizards;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import org.apache.maven.Maven;
import org.apache.maven.cli.MavenCli;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.BuildPathsBlock;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.IImportStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;

import com.cloudbees.eclipse.core.ClickStartService;
import com.cloudbees.eclipse.core.CloudBeesCorePlugin;
import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.CloudBeesNature;
import com.cloudbees.eclipse.core.NatureUtil;
import com.cloudbees.eclipse.core.gc.api.ClickStartCreateResponse;
import com.cloudbees.eclipse.core.gc.api.ClickStartTemplate;
import com.cloudbees.eclipse.dev.scm.egit.ForgeEGitSync;
import com.cloudbees.eclipse.run.ui.CBRunUiActivator;
import com.cloudbees.eclipse.run.ui.popup.actions.ReloadRunAtCloudAction;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;
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

  private static void handleException(final String msg, final IStatus status) {
    if (status.getException() != null) {
      CBRunUiActivator.logError(status.getException());
    }
    Display.getDefault().asyncExec(new Runnable() {
      public void run() {
        ErrorDialog.openError(CBRunUiActivator.getDefault().getWorkbench().getDisplay().getActiveShell(), ERROR_TITLE,
            msg, status);
      }
    });

  }

  @Override
  public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

    org.eclipse.core.runtime.jobs.Job job = new org.eclipse.core.runtime.jobs.Job(
        "Provisioning CloudBees ClickStart project") {
      @Override
      protected IStatus run(final IProgressMonitor monitor) {

        monitor.beginTask("Provisioning CloudBees ClickStart project", 100);
        try {

          ClickStartService service = CloudBeesCorePlugin.getDefault().getClickStartService();
          String accountName = CloudBeesCorePlugin.getDefault().getGrandCentralService().getActiveAccountName();
          monitor.worked(25);

          monitor.setTaskName("Creating '" + nameAndLocPage.getProjectName() + "' for account '" + accountName
              + "' using ClickStart template '" + CBWebAppWizardFinishOperation.this.template.name + "'");

          // Invoke provisioning request in a separate job
          // If request returns keep polling and reporting progress of the provisioning to the job label
          // As progress=100, bring up a modal background job while configuring and refreshing workspace.
          // Setup:
          // Clone from the repo and have git repo accessible
          // Configure datatools connection to the database
          // Refresh jenkins job lists and run@cloud app list.

          ClickStartCreateResponse resp = service.create(CBWebAppWizardFinishOperation.this.template.id, accountName,
              nameAndLocPage.getProjectName());

          String resId = resp.reservationId;
          int pr=service.getCreateProgress(resId);
          int lastpr = pr;
          
          monitor.beginTask("Waiting for the servers to provision ClickStart components...", 100);
          monitor.worked(pr);
          while (pr<100) {
            Thread.currentThread().sleep(3);
            lastpr=pr;
            pr=service.getCreateProgress(resId);            
            monitor.worked(pr-lastpr);
          }

          monitor.beginTask("Configuring local workspace for CloudBees ClickStart project", 100);

          //if (CBWebAppWizardFinishOperation.this.isAddNewRepo) {
          //GrandCentralService service = CloudBeesCorePlugin.getDefault().getGrandCentralService();
          //service.getForgeSyncService().addToRepository(this.repo, this.project, monitor);
          //}

          if (project == null) {
            return Status.OK_STATUS;
          }

          // Clone generated project.
          monitor.subTask("Cloning git repository into the newly created project. From: "+resp.forgeUrl+" to "+project.getLocation());
          ForgeEGitSync.cloneRepo(resp.source, CBWebAppWizardFinishOperation.this.locationURI, monitor);
          monitor.worked(30);

          monitor.subTask("Creating local project '"+CBWebAppWizardFinishOperation.this.template.id+"'");
          if (CBWebAppWizardFinishOperation.this.useDefaultLocation) {
            CBWebAppWizardFinishOperation.this.importOperation.setContext(CBWebAppWizardFinishOperation.this.wizard
                .getShell());
            CBWebAppWizardFinishOperation.this.importOperation.setCreateContainerStructure(false);
            CBWebAppWizardFinishOperation.this.importOperation.run(monitor);
          } else {
            BuildPathsBlock.createProject(project, CBWebAppWizardFinishOperation.this.locationURI, monitor);
          }

          monitor.worked(10);
          monitor.subTask("Adding CloudBees project nature");
          NatureUtil.addNatures(project, new String[] { CloudBeesNature.NATURE_ID }, monitor);
          monitor.worked(10);
          
          // Refresh project to refresh the project nature
          project.refreshLocal(IProject.DEPTH_INFINITE, monitor);
          
          // Let's add back the CB nature in case it was not configured by the maven script
          NatureUtil.addNatures(project, new String[] { CloudBeesNature.NATURE_ID }, monitor);
          
          
          // Run ant or maven build task to generate eclipse files for the project

          boolean mvnExists = project.exists(new Path("/pom.xml"));
          boolean antExists = project.exists(new Path("/build.xml"));

          if (mvnExists) {
            //NatureUtil.addNatures(project, new String[] { "org.eclipse.m2e.core.maven2Nature" }, monitor);
            monitor.subTask("Detected maven build scripts, building project to generate eclipse settings.");
            //int res = CBMavenBuilder.buildMavenProject(project);
            //System.out.println("Maven builder returned: "+res);
            project.refreshLocal(IProject.DEPTH_INFINITE, monitor);
          }
          
          // Refresh run@cloud and dev@cloud items
          CloudBeesUIPlugin.getDefault().reloadAllCloudJenkins(true);
          ReloadRunAtCloudAction.reload();
          
          
          /*project.accept(new IResourceVisitor() {
            @Override
            public boolean visit(final IResource resource) throws CoreException {
              if (resource.getType() == IResource.FILE && "war".equalsIgnoreCase(resource.getFileExtension())) {
                //wars.add(resource.getProjectRelativePath().toOSString());
              }
              return true;
            }
          });
*/
          return Status.OK_STATUS;
        } catch (Exception e) {
          String msg = e.getLocalizedMessage();
          if (e instanceof CloudBeesException) {
            e = (Exception) e.getCause();
          }
          CBRunUiActivator.getDefault().getLogger().error(msg, e);

          String rmsg = "Failed to provision ClickStart project: " + msg;

          if (e instanceof CloudBeesException && e.getMessage() != null) {
            rmsg = e.getMessage();
          }

          return new Status(IStatus.ERROR, CBRunUiActivator.PLUGIN_ID, rmsg, e);

        } finally {
          monitor.done();
        }
      }
    };

    job.setUser(true);
    job.schedule();

  }

  public boolean performFinish() {

    try {
      //CBRunCoreScripts.executeCopySampleWebAppScript(this.uri.getPath(), this.project.getName());
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
  */
}
