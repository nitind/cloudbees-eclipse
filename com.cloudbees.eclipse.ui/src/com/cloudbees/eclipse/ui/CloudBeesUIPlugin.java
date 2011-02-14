package com.cloudbees.eclipse.ui;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.cloudbees.eclipse.core.CloudBeesCorePlugin;
import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.ForgeSyncService;
import com.cloudbees.eclipse.core.JenkinsChangeListener;
import com.cloudbees.eclipse.core.JenkinsService;
import com.cloudbees.eclipse.core.Logger;
import com.cloudbees.eclipse.core.domain.JenkinsInstance;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsInstanceResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse.Job;
import com.cloudbees.eclipse.ui.internal.forge.ForgeEGitSync;
import com.cloudbees.eclipse.ui.views.build.BuildEditorInput;
import com.cloudbees.eclipse.ui.views.build.BuildPart;
import com.cloudbees.eclipse.ui.views.jobs.JobsView;

/**
 * CloudBees Eclipse Toolkit UI Plugin
 * 
 * @author ahtik
 */
public class CloudBeesUIPlugin extends AbstractUIPlugin {

  private final static boolean USE_SECURE_STORAGE = false;

  // The plug-in ID
  public static final String PLUGIN_ID = "com.cloudbees.eclipse.ui"; //$NON-NLS-1$

  // The shared instance
  private static CloudBeesUIPlugin plugin;

  private Logger logger;

  private List<JenkinsService> jenkinsRegistry = new ArrayList<JenkinsService>();

  private List<JenkinsChangeListener> jenkinsChangeListeners = new ArrayList<JenkinsChangeListener>();

  private IPropertyChangeListener prefListener;

  public CloudBeesUIPlugin() {
    super();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
   * )
   */
  public void start(BundleContext context) throws Exception {
    super.start(context);
    plugin = this;
    logger = new Logger(getLog());
    loadAccountCredentials();
    hookPrefChangeListener();

    if ((ForgeSyncService.bundleActive("org.eclipse.egit.core") || ForgeSyncService.bundleActive("org.eclipse.egit"))
        && ForgeSyncService.bundleActive("org.eclipse.jgit")) {
      CloudBeesCorePlugin.getDefault().getGrandCentralService().addForgeSyncProvider(new ForgeEGitSync());
    }

    reloadForgeRepos(false);
  }

  @Override
  protected void initializeImageRegistry(ImageRegistry reg) {
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

  private void hookPrefChangeListener() {
    //SecurePreferencesFactory.getDefault().// get(PreferenceConstants.P_PASSWORD, "");
    prefListener = new IPropertyChangeListener() {

      public void propertyChange(PropertyChangeEvent event) {
        if (PreferenceConstants.P_PASSWORD.equalsIgnoreCase(event.getProperty())
            || PreferenceConstants.P_EMAIL.equalsIgnoreCase(event.getProperty())) {
          try {
            loadAccountCredentials();
          } catch (CloudBeesException e) {
            CloudBeesUIPlugin.getDefault().getLogger().error(e);
          }
        }
      }
    };
    getPreferenceStore().addPropertyChangeListener(prefListener);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
   * )
   */
  public void stop(BundleContext context) throws Exception {
    logger = null;
    plugin = null;
    super.stop(context);
    if (prefListener != null) {
      getPreferenceStore().removePropertyChangeListener(prefListener);
      prefListener = null;
    }
  }

  /**
   * Returns the shared instance
   * 
   * @return the shared instance
   */
  public static CloudBeesUIPlugin getDefault() {
    return plugin;
  }

  public void reloadForgeRepos(final boolean userAction) throws CloudBeesException {
    //      PlatformUI.getWorkbench().getActiveWorkbenchWindow().run(true, true, new IRunnableWithProgress() {
    //        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
    org.eclipse.core.runtime.jobs.Job job = new org.eclipse.core.runtime.jobs.Job("Loading Forge repositories") {
      protected IStatus run(IProgressMonitor monitor) {
        if (!getPreferenceStore().getBoolean(PreferenceConstants.P_ENABLE_FORGE)) {
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
              public void run() {
                MessageDialog.openInformation(CloudBeesUIPlugin.getDefault().getWorkbench().getDisplay()
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
    if (getPreferenceStore().getBoolean(PreferenceConstants.P_ENABLE_FORGE)) {
      job.schedule();
    }
  }

  public static IStatus showError(final String msg, final Throwable e) {
    final Status status = new Status(IStatus.ERROR, PLUGIN_ID, 0, msg, e);
    try {
      CloudBeesUIPlugin.getDefault().getLogger().error(msg, e);
      PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
        public void run() {
          Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
          ErrorDialog.openError(shell, "Error!", msg, status);
        }
      });
    } catch (Exception e2) {
      CloudBeesUIPlugin.getDefault().getLogger().error(msg, e2);
    }
    return status;
  }

  public static IStatus showError(final String msg, final String reason, final Throwable e) {
    final Status status = new Status(IStatus.ERROR, PLUGIN_ID, 0, reason, e);
    try {
      CloudBeesUIPlugin.getDefault().getLogger().error(msg + " - " + reason, e);
      PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
        public void run() {
          Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
          ErrorDialog.openError(shell, "Error!", msg, status);
        }
      });
    } catch (Exception e2) {
      CloudBeesUIPlugin.getDefault().getLogger().error(msg, e2);
    }
    return status;
  }

  public Logger getLogger() {
    return logger;
  }

  public List<JenkinsInstance> loadManualJenkinsInstances() {
    IPreferenceStore store = CloudBeesUIPlugin.getDefault().getPreferenceStore();
    String instances = store.getString(PreferenceConstants.P_JENKINS_INSTANCES);
    List<JenkinsInstance> list = JenkinsInstance.decode(instances);

    if (list != null) {
      for (JenkinsInstance inst : list) {
        lookupJenkinsService(inst);
      }
    }

    return list;
  }

  public List<JenkinsInstance> loadDevAtCloudInstances(IProgressMonitor monitor) throws CloudBeesException {

    List<JenkinsInstance> instances = CloudBeesCorePlugin.getDefault().getGrandCentralService()
        .loadDevAtCloudInstances(monitor);

    for (JenkinsInstance ni : instances) {
      if (getJenkinsServiceForUrl(ni.url) == null) {
        jenkinsRegistry.add(new JenkinsService(ni));
      }
    }

    return instances;
  }

  public void saveJenkinsInstance(JenkinsInstance ni) {
    if (ni == null || ni.label == null || ni.url == null || ni.label.length() == 0 || ni.url.length() == 0) {
      throw new IllegalStateException("Unable to add new instance with an empty url or label!");
    }
    List<JenkinsInstance> list = loadManualJenkinsInstances();
    list.remove(ni); // when editing - id is the same, but props old, so lets kill old instance first
    list.add(ni);

    jenkinsRegistry.remove(new JenkinsService(ni));

    Collections.sort(list);
    CloudBeesUIPlugin.getDefault().getPreferenceStore()
        .setValue(PreferenceConstants.P_JENKINS_INSTANCES, JenkinsInstance.encode(list));
  }

  public void removeJenkinsInstance(JenkinsInstance ni) {
    if (ni == null) {
      throw new RuntimeException("Unable to remove null instance!");
    }
    List<JenkinsInstance> list = loadManualJenkinsInstances();
    list.remove(ni);
    CloudBeesUIPlugin.getDefault().getPreferenceStore()
        .setValue(PreferenceConstants.P_JENKINS_INSTANCES, JenkinsInstance.encode(list));
  }

  public void reloadAllJenkins(boolean userAction) {
    org.eclipse.core.runtime.jobs.Job job = new org.eclipse.core.runtime.jobs.Job(
        "Loading DEV@Cloud & Jenkins instances") {
      protected IStatus run(IProgressMonitor monitor) {
        if (!getPreferenceStore().getBoolean(PreferenceConstants.P_ENABLE_JAAS)) {
          return new Status(Status.INFO, PLUGIN_ID, "DEV@Cloud Continuous Integration is not enabled");
        }

        Exception toReport = null;

        try {
          monitor.beginTask("Reading DEV@Cloud and Jenkins configuration", 1000);

          List<JenkinsInstance> instances = new ArrayList<JenkinsInstance>();
          try {
            instances.addAll(loadDevAtCloudInstances(monitor));
          } catch (Exception e) {
            if (toReport == null) {
              toReport = e;
            }
          }
          monitor.worked(125);
          try {
            instances.addAll(loadManualJenkinsInstances());
          } catch (Exception e) {
            if (toReport == null) {
              toReport = e;
            }
          }
          monitor.worked(125);

          List<JenkinsInstanceResponse> resp = pollInstances(instances, new SubProgressMonitor(monitor, 740));

          Iterator<JenkinsChangeListener> iterator = jenkinsChangeListeners.iterator();
          while (iterator.hasNext()) {
            JenkinsChangeListener listener = (JenkinsChangeListener) iterator.next();
            listener.jenkinsChanged(resp);
          }
          monitor.worked(10);

          if (toReport != null) {
            throw toReport;
          }

          return Status.OK_STATUS;
        } catch (Exception e) {
          String msg = e.getLocalizedMessage();
          if (e instanceof CloudBeesException) {
            e = (Exception) e.getCause();
          }
          CloudBeesUIPlugin.getDefault().getLogger().error(msg, e);
          return new Status(IStatus.ERROR, PLUGIN_ID, 0, msg, e);
        } finally {
          monitor.done();
        }
      }
    };

    job.setUser(userAction);
    if (getPreferenceStore().getBoolean(PreferenceConstants.P_ENABLE_JAAS)) {
      job.schedule();
    }
  }

  private List<JenkinsInstanceResponse> pollInstances(List<JenkinsInstance> instances, IProgressMonitor monitor) {
    try {
      int scale = 10;
      monitor.beginTask("Fetching instance details", instances.size() * scale);

      List<JenkinsInstanceResponse> resp = new ArrayList<JenkinsInstanceResponse>();
      for (JenkinsInstance inst : instances) {
        if (monitor.isCanceled()) {
          throw new OperationCanceledException();
        }
        try {
          JenkinsService service = lookupJenkinsService(inst);
          monitor.setTaskName("Fetching instance details for '" + inst.label + "'...");
          resp.add(service.getInstance(monitor));
        } catch (OperationCanceledException e) {
          throw e;
        } catch (CloudBeesException e) {
          JenkinsInstanceResponse fakeResp = new JenkinsInstanceResponse();
          fakeResp.viewUrl = inst.url;
          fakeResp.nodeName = inst.label;
          fakeResp.offline = true;
          fakeResp.atCloud = inst.atCloud;
          resp.add(fakeResp);

          logger.warn("Failed to fetch info about '" + inst.url + "':" + e.getLocalizedMessage(), e.getCause());
        } finally {
          monitor.worked(1 * scale);
        }
      }

      return resp;
    } finally {
      monitor.done();
    }
  }

  public JenkinsService lookupJenkinsService(JenkinsInstance inst) {
    JenkinsService service = getJenkinsServiceForUrl(inst.url);
    if (service == null) {
      service = new JenkinsService(inst);
      jenkinsRegistry.add(service);
    }
    return service;
  }

  public void showJobs(final String viewUrl, final boolean userAction)
      throws CloudBeesException {
    // CloudBeesUIPlugin.getDefault().getLogger().info("Show jobs: " + viewUrl);
    System.out.println("Show jobs: " + viewUrl);

    if (viewUrl == null) {
      return; // no info
    }

    org.eclipse.core.runtime.jobs.Job job = new org.eclipse.core.runtime.jobs.Job("Loading Jenkins jobs") {
      protected IStatus run(IProgressMonitor monitor) {
        if (!getPreferenceStore().getBoolean(PreferenceConstants.P_ENABLE_JAAS)) {
          return Status.CANCEL_STATUS;
        }

        try {
          PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
            public void run() {
              try {
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                    .showView(JobsView.ID, Long.toString(viewUrl.hashCode()),
                        userAction ? IWorkbenchPage.VIEW_ACTIVATE : IWorkbenchPage.VIEW_CREATE);
              } catch (PartInitException e) {
                showError("Failed to show Jobs view", e);
              }
            }
          });

          if (monitor.isCanceled()) {
            throw new OperationCanceledException();
          }

          JenkinsJobsResponse jobs = getJenkinsServiceForUrl(viewUrl).getJobs(viewUrl, monitor);

          if (monitor.isCanceled()) {
            throw new OperationCanceledException();
          }

          Iterator<JenkinsChangeListener> iterator = jenkinsChangeListeners.iterator();
          while (iterator.hasNext()) {
            JenkinsChangeListener listener = (JenkinsChangeListener) iterator.next();
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
    if (getPreferenceStore().getBoolean(PreferenceConstants.P_ENABLE_JAAS)) {
      job.schedule();
    }
  }

  public void addJenkinsChangeListener(JenkinsChangeListener listener) {
    this.jenkinsChangeListeners.add(listener);
  }

  public void removeJenkinsChangeListener(JenkinsChangeListener listener) {
    jenkinsChangeListeners.remove(listener);
  }

  public JenkinsService getJenkinsServiceForUrl(String serviceOrViewOrJobUrl) {
    Iterator<JenkinsService> iter = jenkinsRegistry.iterator();
    while (iter.hasNext()) {
      JenkinsService service = (JenkinsService) iter.next();
      if (serviceOrViewOrJobUrl.startsWith(service.getUrl())) {
        return service;
      }
    }
    return null;
  }

  public void showBuildForJob(Job el) {
    if (el == null) {
      return;
    }
    // Look up the service
    Iterator<JenkinsService> it = jenkinsRegistry.iterator();
    while (it.hasNext()) {
      JenkinsService service = (JenkinsService) it.next();
      if (el.url.startsWith(service.getUrl())) {

        try {
          //JobDetailsForm.ID, Utils.toB64(jobUrl), IWorkbenchPage.VIEW_ACTIVATE
          // IEditorDescriptor descr = PlatformUI.getWorkbench().getEditorRegistry().findEditor(JobDetailsForm.ID);

          PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
              .openEditor(new BuildEditorInput(el), BuildPart.ID);

        } catch (PartInitException e) {
          CloudBeesUIPlugin.getDefault().getLogger().error(e);
        }
        return;
      }
    }

  }

  public void loadAccountCredentials() throws CloudBeesException {
    String password;
    try {
      password = readP();
    } catch (StorageException e) {
      throw new CloudBeesException("Failed to load GrandCentral password from the storage!", e);
    }

    String email = getPreferenceStore().getString(PreferenceConstants.P_EMAIL);
    CloudBeesCorePlugin.getDefault().getGrandCentralService().setAuthInfo(email, password);
  }

  /**
   * As secure storage is not providing change listener functionality, we must call this programmatically.
   * 
   * @throws CloudBeesException
   */
  public void fireSecureStorageChanged() throws CloudBeesException {
    loadAccountCredentials();
  }

  public void openWithBrowser(String url) {
    IWorkbenchBrowserSupport browserSupport = PlatformUI.getWorkbench().getBrowserSupport();
    try {

      IWebBrowser browser = browserSupport.getExternalBrowser();/*createBrowser(IWorkbenchBrowserSupport.LOCATION_BAR
                                                                | IWorkbenchBrowserSupport.NAVIGATION_BAR, null, null, null);*/
      browser.openURL(new URL(url));
    } catch (PartInitException e) {
      CloudBeesUIPlugin.getDefault().getLogger().error(e);
    } catch (MalformedURLException e) {
      CloudBeesUIPlugin.getDefault().getLogger().error(e);
    }
  }

  public static Image getImage(String imgKey) {
    return CloudBeesUIPlugin.getDefault().getImageRegistry().get(imgKey);
  }

  public static ImageDescriptor getImageDescription(String imgKey) {
    return CloudBeesUIPlugin.getDefault().getImageRegistry().getDescriptor(imgKey);
  }

  public void storeP(String text) throws StorageException, CloudBeesException {
    if (USE_SECURE_STORAGE) {
      SecurePreferencesFactory.getDefault().put(PreferenceConstants.P_PASSWORD, text, true);
    } else {
      getPreferenceStore().putValue(PreferenceConstants.P_PASSWORD, text);
    }
    // Call programmatically as SecurePreferences does not provide change listeners          
    CloudBeesUIPlugin.getDefault().fireSecureStorageChanged();

  }

  public String readP() throws StorageException {
    if (USE_SECURE_STORAGE) {
      return SecurePreferencesFactory.getDefault().get(PreferenceConstants.P_PASSWORD, "");
    }
    return getPreferenceStore().getString(PreferenceConstants.P_PASSWORD);
  }

}
