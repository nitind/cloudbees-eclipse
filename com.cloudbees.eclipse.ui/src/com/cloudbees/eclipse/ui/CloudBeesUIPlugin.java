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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
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
import org.osgi.framework.BundleContext;

import com.cloudbees.eclipse.core.CloudBeesCorePlugin;
import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.Logger;
import com.cloudbees.eclipse.core.NectarChangeListener;
import com.cloudbees.eclipse.core.NectarService;
import com.cloudbees.eclipse.core.domain.NectarInstance;
import com.cloudbees.eclipse.core.nectar.api.NectarInstanceResponse;
import com.cloudbees.eclipse.core.nectar.api.NectarJobsResponse;
import com.cloudbees.eclipse.core.nectar.api.NectarJobsResponse.Job;
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

  private List<NectarService> nectarRegistry = new ArrayList<NectarService>();

  private List<NectarChangeListener> nectarChangeListeners = new ArrayList<NectarChangeListener>();

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
    
    reg.put(CBImages.IMG_FOLDER_HOSTED,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/16x16/cb_folder_run.png")));
    reg.put(CBImages.IMG_FOLDER_LOCAL,
        ImageDescriptor.createFromURL(getBundle().getResource("/icons/16x16/cb_folder_run.png")));
    reg.put(CBImages.IMG_INSTANCE, ImageDescriptor.createFromURL(getBundle().getResource("/icons/16x16/jenkins.png")));
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

  public void reloadForgeRepos() throws CloudBeesException {

    try {
      PlatformUI.getWorkbench().getActiveWorkbenchWindow().run(true, true, new IRunnableWithProgress() {

        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
          if (!getPreferenceStore().getBoolean(PreferenceConstants.P_ENABLE_FORGE)) {
            // Forge sync disabled. TODO Load?
            return;
          }

          try {
            CloudBeesCorePlugin.getDefault().getGrandCentralService().reloadForgeRepos(monitor);
          } catch (CloudBeesException e) {
            e.printStackTrace();
            throw new InvocationTargetException(e);
          }

        }
      });
    } catch (InvocationTargetException e) {
      throw new CloudBeesException("Failed to reload Forge repositories!", e.getTargetException());
    } catch (InterruptedException e) {
      // Ignore. Log for debugging for now. TODO remove later
      e.printStackTrace();
    }

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

  public List<NectarInstance> loadManualNectarInstances() {
    IPreferenceStore store = CloudBeesUIPlugin.getDefault().getPreferenceStore();
    String instances = store.getString(PreferenceConstants.P_NECTAR_INSTANCES);
    List<NectarInstance> list = NectarInstance.decode(instances);
    System.out.println("Loaded: " + list);

    if (list != null) {
      for (NectarInstance inst : list) {
        lookupNectarService(inst);
      }
    }

    return list;
  }

  public List<NectarInstance> loadDevAtCloudInstances(IProgressMonitor monitor) throws CloudBeesException {

    // TODO read from prefs
    //    String instances = store.getString(PreferenceConstants.P_DEVATCLOUD_INSTANCES);
    //    List<NectarInstance> list = NectarInstance.decode(instances);

    List<NectarInstance> instances = CloudBeesCorePlugin.getDefault().getGrandCentralService()
        .loadDACNectarInstances(monitor);

    for (NectarInstance ni : instances) {
      if (getNectarServiceForUrl(ni.url) == null) {
        nectarRegistry.add(new NectarService(ni));
      }
    }

    return instances;
  }

  public void saveNectarInstance(NectarInstance ni) {
    if (ni == null || ni.label == null || ni.url == null || ni.label.length() == 0 || ni.url.length() == 0) {
      throw new IllegalStateException("Unable to add new instance with an empty url or label!");
    }
    List<NectarInstance> list = loadManualNectarInstances();
    System.out.println("save before: " + list);
    list.remove(ni); // when editing - id is the same, but props old, so lets kill old instance first
    list.add(ni);

    nectarRegistry.remove(new NectarService(ni));

    System.out.println("save after: " + list);
    Collections.sort(list);
    CloudBeesUIPlugin.getDefault().getPreferenceStore()
        .setValue(PreferenceConstants.P_NECTAR_INSTANCES, NectarInstance.encode(list));
  }

  public void removeNectarInstance(NectarInstance ni) {
    if (ni == null) {
      throw new RuntimeException("Unable to remove null instance!");
    }
    List<NectarInstance> list = loadManualNectarInstances();
    list.remove(ni);
    CloudBeesUIPlugin.getDefault().getPreferenceStore()
        .setValue(PreferenceConstants.P_NECTAR_INSTANCES, NectarInstance.encode(list));
  }

  public List<NectarInstanceResponse> getManualNectarsInfo(IProgressMonitor monitor) throws CloudBeesException {
    List<NectarInstance> instances = new ArrayList<NectarInstance>(loadManualNectarInstances());

    List<NectarInstanceResponse> resp = pollInstances(instances, monitor);

    return resp;
  }

  public List<NectarInstanceResponse> getDevAtCloudNectarsInfo(IProgressMonitor monitor) throws CloudBeesException {
    List<NectarInstance> instances = new ArrayList<NectarInstance>(loadDevAtCloudInstances(monitor));

    List<NectarInstanceResponse> resp = pollInstances(instances, monitor);

    return resp;
  }

  private List<NectarInstanceResponse> pollInstances(List<NectarInstance> instances, IProgressMonitor monitor) {
    List<NectarInstanceResponse> resp = new ArrayList<NectarInstanceResponse>();
    for (NectarInstance inst : instances) {
      NectarService service = lookupNectarService(inst);
      try {
        resp.add(service.getInstance(monitor));
      } catch (CloudBeesException e) {
        System.out.println("Failed to contact " + service + ". Not adding to the list for now.");//TODO log

        //TODO Consider adding it to the list anyway, just mark it somehow as "Unreachable" in UI!
        NectarInstanceResponse fakeResp = new NectarInstanceResponse();
        fakeResp.serviceUrl = inst.url;
        fakeResp.nodeName = inst.label;
        fakeResp.offline = true;

        //fakeResp.views = new NectarInstanceResponse.View[0];
        resp.add(fakeResp);

        e.printStackTrace();
      }
    }
    return resp;
  }

  public NectarService lookupNectarService(NectarInstance inst) {
    NectarService service = getNectarServiceForUrl(inst.url);
    if (service == null) {
      service = new NectarService(inst);
      nectarRegistry.add(service);
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
  public void showJobs(String serviceUrl, String viewUrl) throws CloudBeesException {
    System.out.println("Show jobs: " + serviceUrl + " - " + viewUrl);

    if (serviceUrl == null && viewUrl == null) {
      return; // no info
    }

    try {
      IProgressMonitor monitor = new NullProgressMonitor(); // TODO add progress monitor instance from somewhere

      PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(JobsView.ID);

      //TODO Start monitoring this job list

      if (serviceUrl == null && viewUrl != null) {
        serviceUrl = getNectarServiceForUrl(viewUrl).getUrl();
      }

      NectarJobsResponse jobs = getNectarServiceForUrl(serviceUrl).getJobs(viewUrl, monitor);

      Iterator<NectarChangeListener> iterator = nectarChangeListeners.iterator();
      while (iterator.hasNext()) {
        NectarChangeListener listener = (NectarChangeListener) iterator.next();
        listener.activeJobViewChanged(jobs);
      }

    } catch (PartInitException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  public void addNectarChangeListener(NectarChangeListener nectarChangeListener) {
    this.nectarChangeListeners.add(nectarChangeListener);
  }

  public void removeNectarChangeListener(NectarChangeListener listener) {
    nectarChangeListeners.remove(listener);
  }

  public NectarService getNectarServiceForUrl(String serviceOrViewOrJobUrl) {
    Iterator<NectarService> iter = nectarRegistry.iterator();
    while (iter.hasNext()) {
      NectarService service = (NectarService) iter.next();
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
    Iterator<NectarService> it = nectarRegistry.iterator();
    while (it.hasNext()) {
      NectarService service = (NectarService) it.next();
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
    System.out.println("Reloading credentials from the preferences");

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
      IWebBrowser browser = browserSupport.createBrowser(IWorkbenchBrowserSupport.LOCATION_BAR
          | IWorkbenchBrowserSupport.NAVIGATION_BAR, null, null, null);
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

}
