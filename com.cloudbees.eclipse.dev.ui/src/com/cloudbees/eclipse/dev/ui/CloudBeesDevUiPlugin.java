package com.cloudbees.eclipse.dev.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.cloudbees.eclipse.core.CBRemoteChangeAdapter;
import com.cloudbees.eclipse.core.CBRemoteChangeListener;
import com.cloudbees.eclipse.core.CloudBeesCorePlugin;
import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.JenkinsService;
import com.cloudbees.eclipse.core.Logger;
import com.cloudbees.eclipse.core.forge.api.ForgeInstance;
import com.cloudbees.eclipse.core.forge.api.ForgeInstance.STATUS;
import com.cloudbees.eclipse.core.jenkins.api.ChangeSetPathItem;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsBuild;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobAndBuildsResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse.Job;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsScmConfig;
import com.cloudbees.eclipse.dev.core.CloudBeesDevCorePlugin;
import com.cloudbees.eclipse.dev.ui.views.build.BuildEditorInput;
import com.cloudbees.eclipse.dev.ui.views.build.BuildHistoryView;
import com.cloudbees.eclipse.dev.ui.views.build.BuildPart;
import com.cloudbees.eclipse.dev.ui.views.forge.ForgeSyncConfirmation;
import com.cloudbees.eclipse.dev.ui.views.jobs.JobConsoleManager;
import com.cloudbees.eclipse.dev.ui.views.jobs.JobsView;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;
import com.cloudbees.eclipse.ui.PreferenceConstants;

/**
 * The activator class controls the plug-in life cycle
 */
public class CloudBeesDevUiPlugin extends AbstractUIPlugin {

  private CBRemoteChangeListener remoteChangeListener = new CBRemoteChangeAdapter() {

    public void activeAccountChanged(String email, String newAccountName) {
      try {
        CloudBeesDevUiPlugin.this.reloadForgeRepos(false);
      } catch (CloudBeesException e) {
        // safe to ignore.
      }

      PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
        @Override
        public void run() {
              closeBuildEditors();
        }
      });      

    }

    private void closeBuildEditors() {

      try {
        // Close all editors that host cloud-hosted build
        IWorkbenchPage page = CloudBeesUIPlugin.getActiveWindow().getActivePage();
        //com.cloudbees.eclipse.dev.ui.views.build.BuildPart
        //CloudBeesUIPlugin.getActiveWindow().getActivePage()

        for (IEditorReference ref : page.getEditorReferences()) {
          try {
            if (ref.getId() != null && ref.getId().equals(com.cloudbees.eclipse.dev.ui.views.build.BuildPart.ID)) {
              IEditorInput in;
              try {
                in = ref.getEditorInput();
              } catch (PartInitException e) {
                e.printStackTrace();
                continue;
              }
              IEditorPart p = ref.getEditor(false);
              if (in instanceof BuildEditorInput && p != null) {
                JenkinsService s = CloudBeesUIPlugin.getDefault().getJenkinsServiceForUrl(
                    ((BuildEditorInput) in).getJobUrl());
                if (s == null || s.isCloud()) {
                  // if service not existing anymore or hosted at cloud, close the editor
                  page.closeEditor(p, false);
                }
              }
            }

          } catch (Throwable t) {
            t.printStackTrace();
            continue;
          }
        }
      } catch (Exception e) {
        getLogger().error(e);
      }
    }

  };

  public static final String PLUGIN_ID = "com.cloudbees.eclipse.dev.ui"; //$NON-NLS-1$
  private static CloudBeesDevUiPlugin plugin;

  private JobConsoleManager jobConsoleManager;

  private Logger logger;

  /**
   * The constructor
   */
  public CloudBeesDevUiPlugin() {

  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(final BundleContext context) throws Exception {
    super.start(context);    
    CloudBeesDevUiPlugin.plugin = this;
    CloudBeesDevCorePlugin.getDefault();
    this.logger = new Logger(getLog());
    reloadForgeRepos(false);

    CloudBeesUIPlugin.getDefault().addCBRemoteChangeListener(this.remoteChangeListener);

  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(final BundleContext context) throws Exception {
    CloudBeesDevUiPlugin.plugin = null;
    this.logger = null;
    if (this.jobConsoleManager != null) {
      this.jobConsoleManager.unregister();
      this.jobConsoleManager = null;
    }

    CloudBeesUIPlugin.getDefault().removeCBRemoteChangeListener(this.remoteChangeListener);

    super.stop(context);
  }

  /**
   * Returns the shared instance
   * 
   * @return the shared instance
   */
  public static CloudBeesDevUiPlugin getDefault() {
    return plugin;
  }

  public Logger getLogger() {
    return this.logger;
  }

  public JobConsoleManager getJobConsoleManager() {
    if (this.jobConsoleManager == null) {
      this.jobConsoleManager = new JobConsoleManager();
    }

    return this.jobConsoleManager;
  }

  @Override
  protected void initializeImageRegistry(final ImageRegistry reg) {
    super.initializeImageRegistry(reg);

    reg.put(CBDEVImages.IMG_CONSOLE,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/epl/monitor_obj.png")));
    reg.put(CBDEVImages.IMG_REFRESH, ImageDescriptor.createFromURL(getBundle().getResource("/icons/epl/refresh.png")));

    reg.put(CBDEVImages.IMG_BROWSER,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/epl/internal_browser.gif")));

    reg.put(CBDEVImages.IMG_RUN, ImageDescriptor.createFromURL(getBundle().getResource("/icons/epl/lrun_obj.png")));
    reg.put(CBDEVImages.IMG_BUILD_HISTORY,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/epl/history_view.gif")));
    reg.put(CBDEVImages.IMG_DEPLOY,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/jenkins-icons/16x16/clock.gif")));

    reg.put(CBDEVImages.IMG_FOLDER_HOSTED,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/16x16/cb_folder_cb.png")));
    reg.put(CBDEVImages.IMG_FOLDER_LOCAL,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/16x16/cb_folder_run_plain.png")));
    reg.put(CBDEVImages.IMG_INSTANCE,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/16x16/jenkins.png")));

    reg.put(CBDEVImages.IMG_FOLDER_FORGE,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/16x16/cb_folder_cb.png")));
    reg.put(CBDEVImages.IMG_INSTANCE_FORGE_GIT,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/16x16/scm_git.png")));
    reg.put(CBDEVImages.IMG_INSTANCE_FORGE_SVN,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/16x16/scm_svn.png")));

    reg.put(CBDEVImages.IMG_VIEW,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/16x16/cb_view_dots_big.png")));

    reg.put(CBDEVImages.IMG_VIEWR2,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/16x16/cb_view_dots_bigr2.png")));

    //reg.put(CBImages.IMG_VIEW, ImageDescriptor.createFromURL(getBundle().getResource("/icons/epl/det_pane_hide.gif")));

    reg.put(CBDEVImages.IMG_FILE, ImageDescriptor.createFromURL(getBundle().getResource("/icons/epl/file_obj.gif")));

    reg.put(CBDEVImages.IMG_FILE_ADDED,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/epl/add_stat.gif")));
    reg.put(CBDEVImages.IMG_FILE_MODIFIED,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/epl/mod_stat.gif")));
    reg.put(CBDEVImages.IMG_FILE_DELETED,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/epl/del_stat.gif")));

    reg.put(CBDEVImages.IMG_JUNIT, ImageDescriptor.createFromURL(getBundle().getResource("/icons/epl/junit.gif")));

    reg.put(CBDEVImages.IMG_BUILD_DETAILS,
        ImageDescriptor.createFromURL(getBundle().getResource("icons/epl/debugt_obj.png")));

    reg.put(CBDEVImages.IMG_DELETE, ImageDescriptor.createFromURL(getBundle().getResource("icons/epl/delete.gif")));

    reg.put(CBDEVImages.IMG_DELETE_DISABLED,
        ImageDescriptor.createFromURL(getBundle().getResource("icons/epl/d/delete.gif")));

    reg.put(CBDEVImages.IMG_COLOR_16_GREY,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/jenkins-icons/16x16/grey.gif")));

    reg.put(CBDEVImages.IMG_COLOR_16_RED,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/jenkins-icons/16x16/red.gif")));

    reg.put(CBDEVImages.IMG_COLOR_16_BLUE,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/jenkins-icons/16x16/blue.gif")));

    reg.put(CBDEVImages.IMG_COLOR_16_YELLOW,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/jenkins-icons/16x16/yellow.gif")));

    reg.put(CBDEVImages.IMG_COLOR_24_RED,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/jenkins-icons/24x24/red.gif")));

    reg.put(CBDEVImages.IMG_COLOR_24_BLUE,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/jenkins-icons/24x24/blue.gif")));

    // HEALTH 16px
    reg.put(CBDEVImages.IMG_HEALTH_16_00_to_19,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/jenkins-icons/16x16/health-00to19.gif")));
    reg.put(CBDEVImages.IMG_HEALTH_16_20_to_39,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/jenkins-icons/16x16/health-20to39.gif")));
    reg.put(CBDEVImages.IMG_HEALTH_16_40_to_59,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/jenkins-icons/16x16/health-40to59.gif")));
    reg.put(CBDEVImages.IMG_HEALTH_16_60_to_79,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/jenkins-icons/16x16/health-60to79.gif")));
    reg.put(CBDEVImages.IMG_HEALTH_16_80PLUS,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/jenkins-icons/16x16/health-80plus.gif")));

    // HEALTH 24px
    reg.put(CBDEVImages.IMG_HEALTH_24_00_to_19,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/jenkins-icons/24x24/health-00to19.gif")));
    reg.put(CBDEVImages.IMG_HEALTH_24_20_to_39,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/jenkins-icons/24x24/health-20to39.gif")));
    reg.put(CBDEVImages.IMG_HEALTH_24_40_to_59,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/jenkins-icons/24x24/health-40to59.gif")));
    reg.put(CBDEVImages.IMG_HEALTH_24_60_to_79,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/jenkins-icons/24x24/health-60to79.gif")));
    reg.put(CBDEVImages.IMG_HEALTH_24_80PLUS,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/jenkins-icons/24x24/health-80plus.gif")));

    reg.put(CBDEVImages.IMG_CB_ICON_LARGE_64x66,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/cb_wiz_icon.png")));
  }

  public static Image getImage(final String imgKey) {
    return CloudBeesDevUiPlugin.getDefault().getImageRegistry().get(imgKey);
  }

  public static ImageDescriptor getImageDescription(final String imgKey) {
    CloudBeesDevUiPlugin pl = CloudBeesDevUiPlugin.getDefault();
    return pl != null ? pl.getImageRegistry().getDescriptor(imgKey) : null;
  }

  private org.eclipse.core.runtime.jobs.Job showJobsForHash(final String viewUrl, final boolean userAction,
      final String hash) throws CloudBeesException {
    // CloudBeesUIPlugin.getDefault().getLogger().info("Show jobs: " + viewUrl);
    //System.out.println("Show jobs: " + viewUrl);

    if (viewUrl == null) {
      return null; // no info
    }

    org.eclipse.core.runtime.jobs.Job job = new org.eclipse.core.runtime.jobs.Job("Loading Jenkins jobs") {
      @Override
      protected IStatus run(final IProgressMonitor monitor) {
        if (!CloudBeesUIPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.P_ENABLE_JAAS)) {
          return Status.CANCEL_STATUS;
        }

        try {

          if (userAction) {
            PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
              @Override
              public void run() {
                try {
                  IWorkbenchPage activePage = CloudBeesUIPlugin.getActiveWindow().getActivePage();

                  //hashCode = viewUrl.hashCode();

                  int mode = userAction ? IWorkbenchPage.VIEW_ACTIVATE : IWorkbenchPage.VIEW_CREATE;

                  IViewPart view = activePage.showView(JobsView.ID, hash, mode);

                } catch (PartInitException e) {
                  CloudBeesUIPlugin.showError("Failed to show Jobs view", e);
                }
              }
            });
          }

          if (monitor.isCanceled()) {
            throw new OperationCanceledException();
          }

          JenkinsService jenkinsService = CloudBeesUIPlugin.getDefault().getJenkinsServiceForUrl(viewUrl);

          JenkinsJobsResponse jobs = jenkinsService.getJobs(viewUrl, monitor);

          if (monitor.isCanceled()) {
            throw new OperationCanceledException();
          }

          List<CBRemoteChangeListener> listeners = Collections.unmodifiableList(CloudBeesUIPlugin.getDefault()
              .getJenkinsChangeListeners());
          monitor.subTask("Notifying jenkins components");
          Iterator<CBRemoteChangeListener> iterator = listeners.iterator();
          while (iterator.hasNext()) {
            CBRemoteChangeListener listener = iterator.next();
            try {
              listener.activeJobViewChanged(jobs);
            } catch (Throwable e) {
              // listeners must not fail
              getLogger().error(e);
            }
            try {
              listener.jenkinsStatusUpdate(viewUrl, true);
            } catch (Throwable e) {
              // listeners must not fail
              getLogger().error(e);
            }
            monitor.worked(10);
          }

          return Status.OK_STATUS;
        } catch (CloudBeesException e) {

          // Failed to load jobs. Notify in order to show node as offline
          List<CBRemoteChangeListener> listeners = Collections.unmodifiableList(CloudBeesUIPlugin.getDefault()
              .getJenkinsChangeListeners());
          monitor.subTask("Failed! Notifying jenkins components.");
          Iterator<CBRemoteChangeListener> iterator = listeners.iterator();
          while (iterator.hasNext()) {
            CBRemoteChangeListener listener = iterator.next();

            try {
              listener.jenkinsStatusUpdate(viewUrl, false);
            } catch (Throwable t) {
              // listeners must not fail
              getLogger().error(t);
            }

            monitor.worked(10);
          }

          getLogger().error(e);
          return new Status(Status.WARNING, PLUGIN_ID, 0, e.getLocalizedMessage(), e.getCause());
        }
      }

    };

    job.setUser(userAction); // Let's always show this job as non-user so it's less distracting in the UI   //userAction
    if (CloudBeesUIPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.P_ENABLE_JAAS)) {
      job.schedule();
    }

    return job;

  }

  public org.eclipse.core.runtime.jobs.Job showJobs(final String viewUrl, final boolean userAction)
      throws CloudBeesException {
    String urlHash = Long.toString(CloudBeesUIPlugin.getDefault().getJenkinsServiceForUrl(viewUrl).getUrl().hashCode());

    return showJobsForHash(viewUrl, userAction, urlHash);
  }

  public void showView(final String viewId) {
    PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
      @Override
      public void run() {
        try {
          CloudBeesUIPlugin.getActiveWindow().getActivePage().showView(viewId);
        } catch (PartInitException e) {
          CloudBeesUIPlugin.showError("Failed to show view: " + viewId, e);
        }
      }
    });
  }

  public void showBuildForJob(final Job job) {
    if (job == null) {
      return;
    }

    String name;
    String url;
    if (job.lastBuild != null && job.lastBuild.url != null) {
      name = job.lastBuild.getDisplayName();
      url = job.lastBuild.url;
    } else {
      name = job.getDisplayName();
      url = job.url;
    }

    try {
      CloudBeesUIPlugin.getActiveWindow().getActivePage().openEditor(new BuildEditorInput(name, url), BuildPart.ID);
    } catch (PartInitException e) {
      CloudBeesUIPlugin.getDefault().getLogger().error(e);
    }
  }

  public void showBuild(final JenkinsBuild build) {
    if (build == null) {
      return;
    }
    try {
      CloudBeesUIPlugin.getActiveWindow().getActivePage()
          .openEditor(new BuildEditorInput(build.getDisplayName(), build.url), BuildPart.ID);
    } catch (PartInitException e) {
      CloudBeesUIPlugin.getDefault().getLogger().error(e);
    }
  }

  /**
   * @param job
   * @param monitor
   * @return <code>true</code> if succeeded
   * @throws CloudBeesException
   */
  public void deleteJob(final Job job) throws CloudBeesException {
    boolean openConfirm = MessageDialog.openConfirm(Display.getCurrent().getActiveShell(), "DELETING A BUILD BUILD!",
        "Are you sure you want to delete this build job?\n" + "Name: " + job.getDisplayName());

    if (openConfirm) {
      JenkinsService service = CloudBeesUIPlugin.getDefault().getJenkinsServiceForUrl(job.url);
      service.deleteJenkinsJob(job.url, new NullProgressMonitor());
    }

  }

  public void openRemoteFile(final String jobUrl, final ChangeSetPathItem item) {
    org.eclipse.core.runtime.jobs.Job job = new org.eclipse.core.runtime.jobs.Job("Opening remote file") {
      @Override
      protected IStatus run(final IProgressMonitor monitor) {
        try {
          JenkinsService jenkins = CloudBeesUIPlugin.getDefault().getJenkinsServiceForUrl(jobUrl);

          if (monitor.isCanceled()) {
            throw new OperationCanceledException();
          }

          JenkinsScmConfig scmConfig = jenkins.getJenkinsScmConfig(jobUrl, monitor);

          if (monitor.isCanceled()) {
            throw new OperationCanceledException();
          }

          boolean opened = CloudBeesCorePlugin.getDefault().getGrandCentralService().getForgeSyncService()
              .openRemoteFile(scmConfig, item, monitor);

          return opened ? Status.OK_STATUS : new Status(IStatus.INFO, PLUGIN_ID, "Can't open " + item.path);
        } catch (CloudBeesException e) {
          getLogger().error(e);
          return new Status(Status.ERROR, PLUGIN_ID, 0, e.getLocalizedMessage(), e.getCause());
        }
      }
    };

    job.setUser(true);
    job.schedule();
  }

  public void reloadForgeRepos(final boolean userAction) throws CloudBeesException {

    //      PlatformUI.getWorkbench().getActiveWorkbenchWindow().run(true, true, new IRunnableWithProgress() {
    //        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
    org.eclipse.core.runtime.jobs.Job job = new org.eclipse.core.runtime.jobs.Job("Synchronizing Forge repositories") {
      @Override
      protected IStatus run(final IProgressMonitor monitor) {
        if (!CloudBeesUIPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.P_ENABLE_FORGE)) {
          // Forge sync disabled.
          return Status.CANCEL_STATUS;
        }

        try {
          monitor.beginTask("Checking Forge repositories", 10);

          final List<ForgeInstance> forgeRepos = CloudBeesUIPlugin.getDefault().getForgeRepos(monitor);
          int step = 1000 / Math.max(forgeRepos.size(), 1);

          IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 4);
          subMonitor.beginTask("", 1000);
          for (ForgeInstance repo : forgeRepos) {
            subMonitor.subTask("Checking repository '" + repo.url + "'");
            CloudBeesCorePlugin.getDefault().getGrandCentralService().getForgeSyncService()
                .updateStatus(repo, subMonitor);
            subMonitor.worked(step);
          }
          subMonitor.subTask("");

          boolean needsToSync = false;
          final List<ForgeInstance> toSync = new ArrayList<ForgeInstance>();
          for (ForgeInstance repo : forgeRepos) {
            if (repo.status != STATUS.SYNCED) {
              toSync.add(repo);
              if (userAction || repo.status == STATUS.UNKNOWN) { // automatically check only unknowns, which are either new or failed
                needsToSync = true;
              }
            }
          }

          if (needsToSync) {
            PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
              @Override
              public void run() {

                ForgeSyncConfirmation confDialog = new ForgeSyncConfirmation(CloudBeesDevUiPlugin.getDefault()
                    .getWorkbench().getDisplay().getActiveShell(), toSync);
                int result = confDialog.open();

                toSync.clear();
                if (result == Dialog.OK) {
                  if (confDialog.getSelectedRepos() != null && !confDialog.getSelectedRepos().isEmpty()) {
                    toSync.addAll(confDialog.getSelectedRepos());
                  }
                  // set those which user has seen but not checked as SKIPPED, otherwise it is UNKNOWN and must be checked
                  for (ForgeInstance repo : forgeRepos) {
                    if (repo.status != STATUS.SYNCED) {
                      if (!toSync.contains(repo)) {
                        repo.status = STATUS.SKIPPED;
                      } else {
                        repo.status = STATUS.UNKNOWN;
                      }
                    }
                  }
                }
              }
            });
          } else {
            toSync.clear();
          }

          String mess = new String();
          if (!toSync.isEmpty()) {
            subMonitor = new SubProgressMonitor(monitor, 4);
            subMonitor.beginTask("", 1000);
            for (ForgeInstance repo : toSync) {
              subMonitor.subTask("Synchronizing repository '" + repo.url + "'");
              CloudBeesCorePlugin.getDefault().getGrandCentralService().getForgeSyncService().sync(repo, subMonitor);
              mess += repo.status.getLabel() + " " + repo.url + "\n\n"; // TODO show lastException somewhere here?
              subMonitor.worked(step);
            }
            subMonitor.subTask("");
          }

          CloudBeesUIPlugin.getDefault().getPreferenceStore()
              .setValue(PreferenceConstants.P_FORGE_INSTANCES, ForgeInstance.encode(forgeRepos));

          if (forgeRepos.isEmpty()) {
            mess = "Found no Forge repositories!";
          }

          Iterator<CBRemoteChangeListener> iterator = CloudBeesUIPlugin.getDefault().getJenkinsChangeListeners()
              .iterator();
          while (iterator.hasNext()) {
            CBRemoteChangeListener listener = iterator.next();
            listener.forgeChanged(forgeRepos);
          }

          monitor.worked(4);

          if (userAction && !toSync.isEmpty()) {
            final String msg = mess;
            PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
              @Override
              public void run() {
                MessageDialog.openInformation(CloudBeesDevUiPlugin.getDefault().getWorkbench().getDisplay()
                    .getActiveShell(), "Synced Forge repositories", msg);
              }
            });
          }

          return Status.OK_STATUS; // new Status(Status.INFO, PLUGIN_ID, mess);
        } catch (Exception e) {
          CloudBeesUIPlugin.getDefault().getLogger().error(e);
          Iterator<CBRemoteChangeListener> iterator = CloudBeesUIPlugin.getDefault().getJenkinsChangeListeners()
              .iterator();
          while (iterator.hasNext()) {
            CBRemoteChangeListener listener = iterator.next();
            listener.forgeChanged(null);
          }
          return new Status(Status.ERROR, PLUGIN_ID, e.getLocalizedMessage(), e);
        } finally {
          monitor.done();
        }
      }
    };

    job.setUser(userAction);
    if (CloudBeesUIPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.P_ENABLE_FORGE)) {
      job.schedule();
    }
  }

  public void showBuildHistory(final String jobUrl, final boolean userAction) throws CloudBeesException {
    // CloudBeesUIPlugin.getDefault().getLogger().info("Show build history: " + jobUrl);
    //System.out.println("Show build history: " + jobUrl);

    if (jobUrl == null) {
      return; // no info
    }

    org.eclipse.core.runtime.jobs.Job job = new org.eclipse.core.runtime.jobs.Job("Loading build history") {
      @Override
      protected IStatus run(final IProgressMonitor monitor) {
        if (!CloudBeesUIPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.P_ENABLE_JAAS)) {
          return Status.CANCEL_STATUS;
        }

        try {
          PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
            @Override
            public void run() {
              try {
                CloudBeesUIPlugin
                    .getActiveWindow()
                    .getActivePage()
                    .showView(
                        BuildHistoryView.ID,
                        Long.toString(CloudBeesUIPlugin.getDefault().getJenkinsServiceForUrl(jobUrl).getUrl()
                            .hashCode()), userAction ? IWorkbenchPage.VIEW_ACTIVATE : IWorkbenchPage.VIEW_CREATE);
              } catch (PartInitException e) {
                CloudBeesUIPlugin.getDefault().showError("Failed to show build history view", e);
              }
            }
          });

          if (monitor.isCanceled()) {
            throw new OperationCanceledException();
          }

          JenkinsJobAndBuildsResponse builds = CloudBeesUIPlugin.getDefault().getJenkinsServiceForUrl(jobUrl)
              .getJobBuilds(jobUrl, monitor);

          if (monitor.isCanceled()) {
            throw new OperationCanceledException();
          }

          Iterator<CBRemoteChangeListener> iterator = CloudBeesUIPlugin.getDefault().getJenkinsChangeListeners()
              .iterator();
          while (iterator.hasNext()) {
            CBRemoteChangeListener listener = iterator.next();
            listener.activeJobHistoryChanged(builds);
          }

          return Status.OK_STATUS;
        } catch (CloudBeesException e) {
          CloudBeesUIPlugin.getDefault().getLogger().error(e);
          return new Status(Status.ERROR, PLUGIN_ID, 0, e.getLocalizedMessage(), e.getCause());
        }
      }
    };

    job.setUser(userAction);
    if (CloudBeesUIPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.P_ENABLE_JAAS)) {
      job.schedule();
    }
  }

  public static void logError(final Exception e) {
    IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, e.getMessage());
    plugin.getLog().log(status);
  }
}
