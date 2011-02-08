package com.cloudbees.eclipse.ui;

import java.lang.reflect.InvocationTargetException;
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
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.IProgressService;
import org.osgi.framework.BundleContext;

import com.cloudbees.eclipse.core.CloudBeesCorePlugin;
import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.JenkinsChangeListener;
import com.cloudbees.eclipse.core.JenkinsService;
import com.cloudbees.eclipse.core.Logger;
import com.cloudbees.eclipse.core.domain.JenkinsInstance;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsInstanceResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse.Job;
import com.cloudbees.eclipse.ui.views.build.BuildEditorInput;
import com.cloudbees.eclipse.ui.views.build.BuildPart;
import com.cloudbees.eclipse.ui.views.jobs.JobsView;

/**
 * CloudBees Eclipse Toolkit UI Plugin
 * 
 * @author ahtik
 */
public class CloudBeesUIPlugin extends AbstractUIPlugin {

  // The plug-in ID
  public static final String PLUGIN_ID = "com.cloudbees.eclipse.core"; //$NON-NLS-1$

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
    reg.put(CBImages.IMG_INSTANCE, ImageDescriptor.createFromURL(getBundle().getResource("/icons/16x16/instance.png")));
    reg.put(CBImages.IMG_VIEW,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/16x16/cb_view_dots_big.png")));

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
            // TODO Auto-generated catch block
            e.printStackTrace();
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

  public void reloadForgeRepos(boolean userAction) throws CloudBeesException {
    //      PlatformUI.getWorkbench().getActiveWorkbenchWindow().run(true, true, new IRunnableWithProgress() {
    //        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
    org.eclipse.core.runtime.jobs.Job job = new org.eclipse.core.runtime.jobs.Job("Loading Forge repositories") {
      protected IStatus run(IProgressMonitor monitor) {
        if (!getPreferenceStore().getBoolean(PreferenceConstants.P_ENABLE_FORGE)) {
          // Forge sync disabled. TODO Load?
          return Status.CANCEL_STATUS;
        }

        try {
          monitor.beginTask("Loading Forge repositories", 1000);
          CloudBeesCorePlugin.getDefault().getGrandCentralService().reloadForgeRepos(monitor);
          monitor.worked(1000);
          return Status.OK_STATUS;
        } catch (Exception e) {
          // TODO: handle exception
          e.printStackTrace();
          return new Status(Status.ERROR, PLUGIN_ID, e.getLocalizedMessage());
        } finally {
          monitor.done();
        }
      }
    };

    job.setUser(userAction);
    job.schedule();
  }

  public static void showError(String msg, Throwable e) {
    Status status = new Status(IStatus.ERROR, "Error!", 0, e.getMessage(), e);
    ErrorDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Error!", msg, status);
    e.printStackTrace();
  }

  public static void showError(String msg, String reason, Throwable e) {
    Status status = new Status(IStatus.ERROR, "Error!", 0, reason, e);
    ErrorDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Error!", msg, status);
    e.printStackTrace();
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

    // TODO read from prefs
    //    String instances = store.getString(PreferenceConstants.P_DEVATCLOUD_INSTANCES);
    //    List<JenkinsInstance> list = JenkinsInstance.decode(instances);

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
    //System.out.println("save before: " + list);
    list.remove(ni); // when editing - id is the same, but props old, so lets kill old instance first
    list.add(ni);

    jenkinsRegistry.remove(new JenkinsService(ni));

    //System.out.println("save after: " + list);
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

        try {
          monitor.beginTask("Reading DEV@Cloud and Jenkins configuration", 1000);

          List<JenkinsInstance> instances = new ArrayList<JenkinsInstance>();
          instances.addAll(loadManualJenkinsInstances());
          monitor.worked(90);
          instances.addAll(loadDevAtCloudInstances(monitor));
          monitor.worked(150);

          List<JenkinsInstanceResponse> resp = pollInstances(instances, new SubProgressMonitor(monitor, 750));

          Iterator<JenkinsChangeListener> iterator = jenkinsChangeListeners.iterator();
          while (iterator.hasNext()) {
            JenkinsChangeListener listener = (JenkinsChangeListener) iterator.next();
            listener.jenkinsChanged(resp);
          }
          monitor.worked(10);

          return Status.OK_STATUS;
        } catch (CloudBeesException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();

          return new Status(Status.ERROR, PLUGIN_ID, e.getLocalizedMessage());
        } finally {
          monitor.done();
        }
      }
    };

    job.setUser(userAction);
    job.schedule();
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
          //System.out.println("Failed to contact " + service + ". Not adding to the list for now.");//TODO log

          //TODO Consider adding it to the list anyway, just mark it somehow as "Unreachable" in UI!
          JenkinsInstanceResponse fakeResp = new JenkinsInstanceResponse();
          fakeResp.serviceUrl = inst.url;
          fakeResp.nodeName = inst.label;
          fakeResp.offline = true;
          fakeResp.atCloud = inst.atCloud;

          //fakeResp.views = new JenkinsInstanceResponse.View[0];
          resp.add(fakeResp);

          e.printStackTrace();
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

  /**
   * Either viewUrl or serviceUrl can be null. If both are provided then viewUrl must belong to serviceUrl.
   * 
   * @param serviceUrl
   * @param viewUrl
   * @throws CloudBeesException
   */
  public void showJobs(final String serviceUrl, final String viewUrl) throws CloudBeesException {
    // System.out.println("Show jobs: " + serviceUrl + " - " + viewUrl);

    if (serviceUrl == null && viewUrl == null) {
      return; // no info
    }

    IRunnableWithProgress op = new IRunnableWithProgress() {
      public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        try {
          //      IProgressMonitor monitor = new NullProgressMonitor(); // TODO add progress monitor instance from somewhere

          PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(JobsView.ID);

          //TODO Start monitoring this job list
          String servUrl = serviceUrl;
          if (servUrl == null && viewUrl != null) {
            servUrl = getJenkinsServiceForUrl(viewUrl).getUrl();
          }

          JenkinsJobsResponse jobs = getJenkinsServiceForUrl(servUrl).getJobs(viewUrl, monitor);

          if (monitor.isCanceled()) {
            throw new OperationCanceledException();
          }

          Iterator<JenkinsChangeListener> iterator = jenkinsChangeListeners.iterator();
          while (iterator.hasNext()) {
            JenkinsChangeListener listener = (JenkinsChangeListener) iterator.next();
            listener.activeJobViewChanged(jobs);
          }
        } catch (CloudBeesException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (PartInitException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    };

    IProgressService service = PlatformUI.getWorkbench().getProgressService();
    try {
      service.run(false, true, op);
    } catch (InvocationTargetException e) {
      // Operation was canceled
    } catch (InterruptedException e) {
      // Handle the wrapped exception
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
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        return;
      }
    }

  }

  public void loadAccountCredentials() throws CloudBeesException {
    //TODO Remove
    //System.out.println("Reloading credentials from the preferences");

    String password;
    try {
      password = SecurePreferencesFactory.getDefault().get(PreferenceConstants.P_PASSWORD, "");
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
      // TODO Log!
      e.printStackTrace();
    } catch (MalformedURLException e) {
      // TODO Log!
      e.printStackTrace();
    }
  }

  public static Image getImage(String imgKey) {
    return CloudBeesUIPlugin.getDefault().getImageRegistry().get(imgKey);
  }

  public static ImageDescriptor getImageDescription(String imgKey) {
    return CloudBeesUIPlugin.getDefault().getImageRegistry().getDescriptor(imgKey);
  }

}
