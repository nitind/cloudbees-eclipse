package com.cloudbees.eclipse.ui;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.cloudbees.eclipse.core.CloudBeesCorePlugin;
import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.JenkinsChangeListener;
import com.cloudbees.eclipse.core.JenkinsService;
import com.cloudbees.eclipse.core.Logger;
import com.cloudbees.eclipse.core.domain.JenkinsInstance;
import com.cloudbees.eclipse.core.forge.api.ForgeSync.ChangeSetPathItem;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsInstanceResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobProperty;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsScmConfig;

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
  @Override
  public void start(final BundleContext context) throws Exception {
    super.start(context);
    plugin = this;
    this.logger = new Logger(getLog());
    loadAccountCredentials();
    hookPrefChangeListener();

    reloadForgeRepos(false);
  }

  private void hookPrefChangeListener() {
    //SecurePreferencesFactory.getDefault().// get(PreferenceConstants.P_PASSWORD, "");
    this.prefListener = new IPropertyChangeListener() {

      public void propertyChange(final PropertyChangeEvent event) {
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
    getPreferenceStore().addPropertyChangeListener(this.prefListener);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
   * )
   */
  @Override
  public void stop(final BundleContext context) throws Exception {
    this.logger = null;
    plugin = null;
    super.stop(context);
    if (this.prefListener != null) {
      getPreferenceStore().removePropertyChangeListener(this.prefListener);
      this.prefListener = null;
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
      @Override
      protected IStatus run(final IProgressMonitor monitor) {
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
    return this.logger;
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

  public List<JenkinsInstance> loadDevAtCloudInstances(final IProgressMonitor monitor) throws CloudBeesException {

    List<JenkinsInstance> instances = CloudBeesCorePlugin.getDefault().getGrandCentralService()
    .loadDevAtCloudInstances(monitor);

    for (JenkinsInstance ni : instances) {
      if (getJenkinsServiceForUrl(ni.url) == null) {
        this.jenkinsRegistry.add(new JenkinsService(ni));
      }
    }

    return instances;
  }

  public void saveJenkinsInstance(final JenkinsInstance ni) {
    if (ni == null || ni.label == null || ni.url == null || ni.label.length() == 0 || ni.url.length() == 0) {
      throw new IllegalStateException("Unable to add new instance with an empty url or label!");
    }
    List<JenkinsInstance> list = loadManualJenkinsInstances();
    list.remove(ni); // when editing - id is the same, but props old, so lets kill old instance first
    list.add(ni);

    this.jenkinsRegistry.remove(new JenkinsService(ni));

    Collections.sort(list);
    CloudBeesUIPlugin.getDefault().getPreferenceStore()
    .setValue(PreferenceConstants.P_JENKINS_INSTANCES, JenkinsInstance.encode(list));
  }

  public void removeJenkinsInstance(final JenkinsInstance ni) {
    if (ni == null) {
      throw new RuntimeException("Unable to remove null instance!");
    }
    List<JenkinsInstance> list = loadManualJenkinsInstances();
    list.remove(ni);
    CloudBeesUIPlugin.getDefault().getPreferenceStore()
    .setValue(PreferenceConstants.P_JENKINS_INSTANCES, JenkinsInstance.encode(list));
  }

  public void reloadAllJenkins(final boolean userAction) {
    org.eclipse.core.runtime.jobs.Job job = new org.eclipse.core.runtime.jobs.Job(
    "Loading DEV@Cloud & Jenkins instances") {
      @Override
      protected IStatus run(final IProgressMonitor monitor) {
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

          Iterator<JenkinsChangeListener> iterator = CloudBeesUIPlugin.this.jenkinsChangeListeners.iterator();
          while (iterator.hasNext()) {
            JenkinsChangeListener listener = iterator.next();
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

  private List<JenkinsInstanceResponse> pollInstances(final List<JenkinsInstance> instances, final IProgressMonitor monitor) {
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

          this.logger.warn("Failed to fetch info about '" + inst.url + "':" + e.getLocalizedMessage(), e.getCause());
        } finally {
          monitor.worked(1 * scale);
        }
      }

      return resp;
    } finally {
      monitor.done();
    }
  }

  public JenkinsService lookupJenkinsService(final JenkinsInstance inst) {
    JenkinsService service = getJenkinsServiceForUrl(inst.url);
    if (service == null) {
      service = new JenkinsService(inst);
      this.jenkinsRegistry.add(service);
    }
    return service;
  }

  public void addJenkinsChangeListener(final JenkinsChangeListener listener) {
    this.jenkinsChangeListeners.add(listener);
  }

  public void removeJenkinsChangeListener(final JenkinsChangeListener listener) {
    this.jenkinsChangeListeners.remove(listener);
  }

  public List<JenkinsChangeListener> getJenkinsChangeListeners() {
    return this.jenkinsChangeListeners;
  }

  public JenkinsService getJenkinsServiceForUrl(final String serviceOrViewOrJobUrl) {
    Iterator<JenkinsService> iter = this.jenkinsRegistry.iterator();
    while (iter.hasNext()) {
      JenkinsService service = iter.next();
      if (serviceOrViewOrJobUrl.startsWith(service.getUrl())) {
        return service;
      }
    }
    return null;
  }

  public List<JenkinsService> getAllJenkinsServices() {
    return this.jenkinsRegistry;
  }

  public Map<String, String> getJobPropValues(final JenkinsJobProperty[] jobProps) {
    final Map<String, String> props = new HashMap<String, String>();

    if (jobProps != null && jobProps.length > 0) {
      for (JenkinsJobProperty prop : jobProps) {
        if (prop.parameterDefinitions != null && prop.parameterDefinitions.length > 0) {
          for (JenkinsJobProperty.ParameterDefinition def : prop.parameterDefinitions) {
            JenkinsJobProperty.ParameterValue val = def.defaultParameterValue;
            InputDialog propDialog = new InputDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                "Job parameter is missing",
                "Specify value for job parameter '" + def.name + "':", val != null ? val.value : "", null);
            propDialog.setBlockOnOpen(true);
            if (propDialog.open() != InputDialog.OK) {
              throw new CancellationException();
            }
            props.put(def.name, propDialog.getValue());
          }
        }
      }
    }

    return props;
  }

  public static IWorkbenchWindow getActiveWindow() {
    IWorkbench workbench = PlatformUI.getWorkbench();
    IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
    if (window == null) {
      IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
      if (windows.length > 0) {
        window = windows[0];
      }
    }
    return window;
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

  public void openWithBrowser(final String url) {
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

  public void storeP(final String text) throws StorageException, CloudBeesException {
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

  public void openRemoteFile(final String jobUrl, final ChangeSetPathItem item) {
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

          return opened ? Status.OK_STATUS : new Status(IStatus.INFO, CloudBeesUIPlugin.PLUGIN_ID, "Can't open "
              + item.path);
        } catch (CloudBeesException e) {
          CloudBeesUIPlugin.getDefault().getLogger().error(e);
          return new Status(Status.ERROR, CloudBeesUIPlugin.PLUGIN_ID, 0, e.getLocalizedMessage(), e.getCause());
        }
      }
    };

    job.setUser(true);
    job.schedule();
  }

}
