package com.cloudbees.eclipse.dev.ui;

import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.cloudbees.eclipse.core.CloudBeesCorePlugin;
import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.JenkinsChangeListener;
import com.cloudbees.eclipse.core.JenkinsService;
import com.cloudbees.eclipse.core.Logger;
import com.cloudbees.eclipse.core.domain.JenkinsInstance;
import com.cloudbees.eclipse.core.forge.api.ForgeSync;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse.Job;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsScmConfig;
import com.cloudbees.eclipse.dev.core.CloudBeesDevCorePlugin;
import com.cloudbees.eclipse.dev.ui.utils.FavouritesUtils;
import com.cloudbees.eclipse.dev.ui.views.build.BuildEditorInput;
import com.cloudbees.eclipse.dev.ui.views.build.BuildPart;
import com.cloudbees.eclipse.dev.ui.views.jobs.JobConsoleManager;
import com.cloudbees.eclipse.dev.ui.views.jobs.JobsView;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;
import com.cloudbees.eclipse.ui.PreferenceConstants;

/**
 * The activator class controls the plug-in life cycle
 */
public class CloudBeesDevUiPlugin extends AbstractUIPlugin {

  private final class FavouritesTracker extends Thread {
    private static final int POLL_DELAY = 10 * 1000;
    JenkinsJobsResponse previous = null;
    private boolean halted = false;

    public FavouritesTracker() {
      super("Favourites Tracker");
    }

    @Override
    public void run() {
      while (!this.halted) {
        try {
          Thread.sleep(POLL_DELAY);
          JenkinsJobsResponse favouritesResponse = FavouritesUtils.getFavouritesResponse(new NullProgressMonitor());

          if (this.previous != null) {
            for (Job j : this.previous.jobs) {
              checkJobsForNewBuild(j, favouritesResponse.jobs);
            }
          }

          this.previous = favouritesResponse;

        } catch (CloudBeesException e) {
          CloudBeesDevUiPlugin.this.logger.error(e);
        } catch (InterruptedException e) {
          this.halted = true;
        }
      }
    }

    private void checkJobsForNewBuild(Job j, Job[] newJobs) {
      for (final Job q : newJobs) {
        if (q.url.equals(j.url)) {
          showAlertIfHasNewBuild(j, q);
        }
      }
    }

    private void showAlertIfHasNewBuild(Job j, final Job q) {
      if (q.lastBuild != null && (j.lastBuild == null || q.lastBuild.number > j.lastBuild.number)) {
        Display.getDefault().syncExec(new Runnable() {

          @Override
          public void run() {
            FavouritesUtils.showNotification(q);
          }
        });
      }
    }

    public void halt() {
      this.halted = true;
    }
  }

  public static final String PLUGIN_ID = "com.cloudbees.eclipse.dev.ui"; //$NON-NLS-1$
  private static CloudBeesDevUiPlugin plugin;

  private JobConsoleManager jobConsoleManager;

  private Logger logger;
  private FavouritesTracker favouritesTracker;

  /**
   * The constructor
   */
  public CloudBeesDevUiPlugin() {
    CloudBeesUIPlugin.getDefault().getAllJenkinsServices()
        .add(new JenkinsService(new JenkinsInstance("Favourite jobs", FavouritesUtils.FAVOURITES)));
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(final BundleContext context) throws Exception {
    super.start(context);
    CloudBeesDevUiPlugin.plugin = this;
    this.logger = new Logger(getLog());
    reloadForgeRepos(false);
    this.favouritesTracker = new FavouritesTracker();
    this.favouritesTracker.start();
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
    this.favouritesTracker.halt();
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

    reg.put(CBImages.IMG_CONSOLE, ImageDescriptor.createFromURL(getBundle().getResource("/icons/epl/monitor_obj.png")));
    reg.put(CBImages.IMG_REFRESH, ImageDescriptor.createFromURL(getBundle().getResource("/icons/epl/refresh.png")));

    reg.put(CBImages.IMG_BROWSER,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/epl/internal_browser.gif")));

    reg.put(CBImages.IMG_RUN, ImageDescriptor.createFromURL(getBundle().getResource("/icons/epl/lrun_obj.png")));

    reg.put(CBImages.IMG_FOLDER_HOSTED,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/16x16/cb_folder_run.png")));
    reg.put(CBImages.IMG_FOLDER_LOCAL,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/16x16/cb_folder_run.png")));
    reg.put(CBImages.IMG_INSTANCE, ImageDescriptor.createFromURL(getBundle().getResource("/icons/16x16/jenkins.png")));

    reg.put(CBImages.IMG_VIEW,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/16x16/cb_view_dots_big.png")));
    //reg.put(CBImages.IMG_VIEW, ImageDescriptor.createFromURL(getBundle().getResource("/icons/epl/det_pane_hide.gif")));

    reg.put(CBImages.IMG_FILE, ImageDescriptor.createFromURL(getBundle().getResource("/icons/epl/file_obj.gif")));

    reg.put(CBImages.IMG_FILE_ADDED, ImageDescriptor.createFromURL(getBundle().getResource("/icons/epl/add_stat.gif")));
    reg.put(CBImages.IMG_FILE_MODIFIED,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/epl/mod_stat.gif")));
    reg.put(CBImages.IMG_FILE_DELETED,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/epl/del_stat.gif")));

    reg.put(CBImages.IMG_JUNIT, ImageDescriptor.createFromURL(getBundle().getResource("/icons/epl/junit.gif")));

    reg.put(CBImages.IMG_BUILD_DETAILS,
        ImageDescriptor.createFromURL(getBundle().getResource("icons/epl/debugt_obj.png")));
    reg.put(CBImages.IMG_BUILD_CONSOLE_LOG,
        ImageDescriptor.createFromURL(getBundle().getResource("icons/epl/debugtt_obj.png"))); // TODO

    reg.put(CBImages.IMG_DELETE, ImageDescriptor.createFromURL(getBundle().getResource("icons/epl/delete.gif")));

    reg.put(CBImages.IMG_DELETE_DISABLED,
        ImageDescriptor.createFromURL(getBundle().getResource("icons/epl/d/delete.gif")));

    reg.put(CBImages.IMG_COLOR_16_GREY,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/jenkins-icons/16x16/grey.gif")));

    reg.put(CBImages.IMG_COLOR_16_RED,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/jenkins-icons/16x16/red.gif")));

    reg.put(CBImages.IMG_COLOR_16_BLUE,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/jenkins-icons/16x16/blue.gif")));

    reg.put(CBImages.IMG_COLOR_24_RED,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/jenkins-icons/24x24/red.gif")));

    reg.put(CBImages.IMG_COLOR_24_BLUE,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/jenkins-icons/24x24/blue.gif")));

    // HEALTH 16px
    reg.put(CBImages.IMG_HEALTH_16_00_to_19,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/jenkins-icons/16x16/health-00to19.gif")));
    reg.put(CBImages.IMG_HEALTH_16_20_to_39,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/jenkins-icons/16x16/health-20to39.gif")));
    reg.put(CBImages.IMG_HEALTH_16_40_to_59,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/jenkins-icons/16x16/health-40to59.gif")));
    reg.put(CBImages.IMG_HEALTH_16_60_to_79,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/jenkins-icons/16x16/health-60to79.gif")));
    reg.put(CBImages.IMG_HEALTH_16_80PLUS,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/jenkins-icons/16x16/health-80plus.gif")));

    // HEALTH 24px
    reg.put(CBImages.IMG_HEALTH_24_00_to_19,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/jenkins-icons/24x24/health-00to19.gif")));
    reg.put(CBImages.IMG_HEALTH_24_20_to_39,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/jenkins-icons/24x24/health-20to39.gif")));
    reg.put(CBImages.IMG_HEALTH_24_40_to_59,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/jenkins-icons/24x24/health-40to59.gif")));
    reg.put(CBImages.IMG_HEALTH_24_60_to_79,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/jenkins-icons/24x24/health-60to79.gif")));
    reg.put(CBImages.IMG_HEALTH_24_80PLUS,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/jenkins-icons/24x24/health-80plus.gif")));

  }

  public static Image getImage(final String imgKey) {
    return CloudBeesDevUiPlugin.getDefault().getImageRegistry().get(imgKey);
  }

  public static ImageDescriptor getImageDescription(final String imgKey) {
    return CloudBeesDevUiPlugin.getDefault().getImageRegistry().getDescriptor(imgKey);
  }

  public void showJobs(final String viewUrl, final boolean userAction) throws CloudBeesException {
    // CloudBeesUIPlugin.getDefault().getLogger().info("Show jobs: " + viewUrl);
    System.out.println("Show jobs: " + viewUrl);

    if (viewUrl == null) {
      return; // no info
    }

    org.eclipse.core.runtime.jobs.Job job = new org.eclipse.core.runtime.jobs.Job("Loading Jenkins jobs") {
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
                boolean isFavourites = FavouritesUtils.FAVOURITES.equals(viewUrl);
                IWorkbenchPage activePage = CloudBeesUIPlugin.getActiveWindow().getActivePage();

                int hashCode;
                if (isFavourites) {
                  hashCode = viewUrl.hashCode();
                } else {
                  hashCode = CloudBeesUIPlugin.getDefault().getJenkinsServiceForUrl(viewUrl).getUrl().hashCode();
                }

                String urlHash = Long.toString(hashCode);

                int mode = userAction ? IWorkbenchPage.VIEW_ACTIVATE : IWorkbenchPage.VIEW_CREATE;

                IViewPart view = activePage.showView(JobsView.ID, urlHash, mode);

              } catch (PartInitException e) {
                CloudBeesUIPlugin.showError("Failed to show Jobs view", e);
              }
            }
          });

          if (monitor.isCanceled()) {
            throw new OperationCanceledException();
          }

          JenkinsJobsResponse jobs;
          if (FavouritesUtils.FAVOURITES.equals(viewUrl)) {
            jobs = FavouritesUtils.getFavouritesResponse(monitor);
          } else {
            jobs = CloudBeesUIPlugin.getDefault().getJenkinsServiceForUrl(viewUrl).getJobs(viewUrl, monitor);
          }
          if (monitor.isCanceled()) {
            throw new OperationCanceledException();
          }

          Iterator<JenkinsChangeListener> iterator = CloudBeesUIPlugin.getDefault().getJenkinsChangeListeners()
              .iterator();
          while (iterator.hasNext()) {
            JenkinsChangeListener listener = iterator.next();
            listener.activeJobViewChanged(jobs);
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

  public void showBuildForJob(final Job el) {
    if (el == null) {
      return;
    }
    // Look up the service
    Iterator<JenkinsService> it = CloudBeesUIPlugin.getDefault().getAllJenkinsServices().iterator();
    while (it.hasNext()) {
      JenkinsService service = it.next();
      if (el.url.startsWith(service.getUrl())) {

        try {
          //JobDetailsForm.ID, Utils.toB64(jobUrl), IWorkbenchPage.VIEW_ACTIVATE
          // IEditorDescriptor descr = PlatformUI.getWorkbench().getEditorRegistry().findEditor(JobDetailsForm.ID);

          CloudBeesUIPlugin.getActiveWindow().getActivePage().openEditor(new BuildEditorInput(el), BuildPart.ID);

        } catch (PartInitException e) {
          CloudBeesUIPlugin.getDefault().getLogger().error(e);
        }
        return;
      }
    }
  }

  /**
   * @param job
   * @param monitor
   * @return <code>true</code> if succeeded
   * @throws CloudBeesException
   */
  public void deleteJob(final Job job) throws CloudBeesException {
    boolean openConfirm = MessageDialog.openConfirm(Display.getCurrent().getActiveShell(), "DELETING A BUILD JOB!",
        "Are you sure you want to delete this build job?\n" + "Name: " + job.getDisplayName());

    if (openConfirm) {
      JenkinsService service = CloudBeesUIPlugin.getDefault().getJenkinsServiceForUrl(job.url);
      service.deleteJenkinsJob(job.url, new NullProgressMonitor());
    }

  }

  public void openRemoteFile(final String jobUrl, final ForgeSync.ChangeSetPathItem item) {
    org.eclipse.core.runtime.jobs.Job job = new org.eclipse.core.runtime.jobs.Job("Loading Jenkins jobs") {
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

          boolean opened = CloudBeesCorePlugin.getDefault().getGrandCentralService()
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
    CloudBeesDevCorePlugin.getDefault();

    //      PlatformUI.getWorkbench().getActiveWorkbenchWindow().run(true, true, new IRunnableWithProgress() {
    //        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
    org.eclipse.core.runtime.jobs.Job job = new org.eclipse.core.runtime.jobs.Job("Loading Forge repositories") {
      @Override
      protected IStatus run(final IProgressMonitor monitor) {
        if (!CloudBeesUIPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.P_ENABLE_FORGE)) {
          // Forge sync disabled.
          return Status.CANCEL_STATUS;
        }

        try {
          monitor.beginTask("Loading Forge repositories", 1000);

          String[] status = CloudBeesCorePlugin.getDefault().getGrandCentralService().reloadForgeRepos(monitor);

          String mess = "";
          if (status != null) {
            for (String st : status) {
              mess += st + "\n\n";
            }
          }

          if (mess.length() == 0) {
            mess = "Found no Forge repositories!";
          }

          monitor.worked(1000);

          if (userAction) {
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

}
