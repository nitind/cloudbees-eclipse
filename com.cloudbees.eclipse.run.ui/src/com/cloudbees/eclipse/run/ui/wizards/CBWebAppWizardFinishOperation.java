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

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.BuildPathsBlock;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
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
import com.cloudbees.eclipse.run.core.CBRunCoreActivator;
import com.cloudbees.eclipse.run.core.NewClickStartProjectHook;
import com.cloudbees.eclipse.run.ui.CBProjectSettingsPage;
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
        "Provisioning CloudBees ClickStart project. This may take a few minutes.") {
      
      
      @Override
      protected void canceling() {
        // notify user "ClickStart project provisioning cannot be cancelled."
        //MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Cannot be cancelled.", "ClickStart project provisioning cannot be cancelled.");
        MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Cancelled.", "Partially cancelled:\n1) Provisioning cannot be cancelled and will continue.\n2) After the provisioning completes Eclipse won't be configured for the app.");
        //super.canceling();
      }
      
      @Override
      protected IStatus run(final IProgressMonitor monitor) {

        monitor.beginTask("Provisioning CloudBees ClickStart project. This may take a few minutes.", 100);
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

          // Set appId for the project settings
                    
          String resId = resp.reservationId;
          int pr = service.getCreateProgress(resId);
          int lastpr = pr;

          if (monitor.isCanceled()) {
            return Status.CANCEL_STATUS;
          }
          
          monitor.beginTask("Waiting for the servers to provision ClickStart components. This may take a few minutes.", 100);
          monitor.worked(pr);
          while (pr < 100) {
            Thread.currentThread().sleep(1000);
            lastpr = pr;
            if (monitor.isCanceled()) {
              return Status.CANCEL_STATUS;
            }
            pr = service.getCreateProgress(resId);
            if (monitor.isCanceled()) {
              return Status.CANCEL_STATUS;
            }
            monitor.worked(pr - lastpr);
          }

          if (monitor.isCanceled()) {
            return Status.CANCEL_STATUS;
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
          monitor.subTask("Cloning git repository into the newly created project. From: " + resp.forgeUrl + " to "
              + project.getLocation());
          ForgeEGitSync.cloneRepo(resp.source, CBWebAppWizardFinishOperation.this.locationURI, monitor);
          monitor.worked(30);

          monitor.subTask("Creating local project '" + CBWebAppWizardFinishOperation.this.template.id + "'");
          if (CBWebAppWizardFinishOperation.this.useDefaultLocation) {
            CBWebAppWizardFinishOperation.this.importOperation.setContext(CBWebAppWizardFinishOperation.this.wizard
                .getShell());
            CBWebAppWizardFinishOperation.this.importOperation.setCreateContainerStructure(false);
            CBWebAppWizardFinishOperation.this.importOperation.run(monitor);
          } else {
            BuildPathsBlock.createProject(project, CBWebAppWizardFinishOperation.this.locationURI, monitor);
          }

          monitor.worked(10);
          monitor.subTask("Adding CloudBees and Java project nature");
          //JavaCore.NATURE_ID
          NatureUtil.addNatures(project, new String[] { CloudBeesNature.NATURE_ID, JavaCore.NATURE_ID }, monitor);
          monitor.worked(10);

          String[] s1 = resp.appUrl.split("\\.");
          String account = s1[1];
          int idx1 = s1[0].lastIndexOf('/');
          String appId = s1[0].substring(idx1+1); 
          
          project.setPersistentProperty(CloudBeesCorePlugin.PRJ_APPID_KEY, appId);
          project.setPersistentProperty(CloudBeesCorePlugin.PRJ_ACCOUNT_KEY, account);

          // Refresh project to refresh the project nature
          project.refreshLocal(IProject.DEPTH_INFINITE, monitor);

          // Let's add back the CB nature in case it was not configured by the maven script
          NatureUtil.addNatures(project, new String[] { CloudBeesNature.NATURE_ID, JavaCore.NATURE_ID }, monitor);

          // Run ant or maven build task to generate eclipse files for the project

          // Refresh run@cloud and dev@cloud items
          CloudBeesUIPlugin.getDefault().reloadAllCloudJenkins(true);
          ReloadRunAtCloudAction.reload();

          for (NewClickStartProjectHook hook : getHooks()) {
            hook.hookProject(resp, project, monitor);
          }

          // initialize database components
          /*          Provisioning database for: null
          COMPONENT: key:Source_repository; name:Source repository; url:ssh://git@git.cloudbees.com/grandomstate/sw1.git
          COMPONENT: key:Jenkins_build; name:Jenkins build; url:null
          COMPONENT: key:Web_Application_sw1; name:Web Application sw1; url:http://sw1.grandomstate.cloudbees.net
          COMPONENT: key:Database_sw1_fu9l; name:Database sw1_fu9l; url:null
          */

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

  private List<NewClickStartProjectHook> getHooks() throws CoreException {
    List<NewClickStartProjectHook> hooks = new ArrayList<NewClickStartProjectHook>();

    IExtension[] extensions = Platform.getExtensionRegistry()
        .getExtensionPoint(CBRunCoreActivator.PLUGIN_ID, "newClickStartProjectHook").getExtensions();

    for (IExtension extension : extensions) {
      for (IConfigurationElement element : extension.getConfigurationElements()) {
        Object executableExtension = element.createExecutableExtension("defaultHandler");
        if (executableExtension instanceof NewClickStartProjectHook) {
          hooks.add((NewClickStartProjectHook) executableExtension);
        }
      }
    }

    return hooks;
  }

}
