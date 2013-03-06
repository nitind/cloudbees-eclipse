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
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.cloudbees.eclipse.core.ApplicationInfoChangeListener;
import com.cloudbees.eclipse.core.CBRemoteChangeListener;
import com.cloudbees.eclipse.core.ClickStartService;
import com.cloudbees.eclipse.core.CloudBeesCorePlugin;
import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.DatabaseInfoChangeListener;
import com.cloudbees.eclipse.core.GrandCentralService;
import com.cloudbees.eclipse.core.GrandCentralService.AuthInfo;
import com.cloudbees.eclipse.core.JenkinsService;
import com.cloudbees.eclipse.core.Logger;
import com.cloudbees.eclipse.core.domain.JenkinsInstance;
import com.cloudbees.eclipse.core.forge.api.ForgeInstance;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsInstanceResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobProperty;

/**
 * CloudBees Eclipse Toolkit UI Plugin
 * 
 * @author ahtik
 */
public class CloudBeesUIPlugin extends AbstractUIPlugin {

  private final static boolean USE_SECURE_STORAGE = false;

  private AuthStatus authStatus = AuthStatus.UNKNOWN;

  // The plug-in ID
  public static final String PLUGIN_ID = "com.cloudbees.eclipse.ui"; //$NON-NLS-1$

  // The shared instance
  private static CloudBeesUIPlugin plugin;

  private Logger logger;

  private final List<JenkinsService> jenkinsRegistry = new ArrayList<JenkinsService>();

  private final List<CBRemoteChangeListener> cbRemoteChangeListeners = new ArrayList<CBRemoteChangeListener>();

  private final List<ApplicationInfoChangeListener> applicationInfoChangeListeners = new ArrayList<ApplicationInfoChangeListener>();

  private final List<DatabaseInfoChangeListener> databaseInfoChangeListeners = new ArrayList<DatabaseInfoChangeListener>();

  private IPropertyChangeListener prefListener;

  private List<ForgeInstance> forgeRegistry;

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
       
    // not hooking as performOk will perform the validation
    // hookPrefChangeListener();
    
    validateJREforRunAtCloud();
  }

  /**
   * Validates JRE for run@cloud and if Run@cloud can support the JRE. RUN@cloud plugins will be disabled by themselves
   * (look plugin#start()).
   */
  private void validateJREforRunAtCloud() {
    if (!CloudBeesCorePlugin.validateRUNatCloudJRE()) {
      Display.getDefault().syncExec(new Runnable() {
        public void run() {
          IStatus s = new Status(
              IStatus.WARNING,
              PLUGIN_ID,
              "CloudBees RUN@cloud does not support Java SE 7. Please start Eclipse with Java SE 6!\nRUN@cloud functionality disabled.");
          ErrorDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
              "CloudBees RUN@cloud does not support Java SE 7!", null, s);
        }
      });
    }
  }

  private void hookPrefChangeListener() {
    //SecurePreferencesFactory.getDefault().// get(PreferenceConstants.P_PASSWORD, "");
    this.prefListener = new IPropertyChangeListener() {

      public void propertyChange(final PropertyChangeEvent event) {
        if (PreferenceConstants.P_PASSWORD.equalsIgnoreCase(event.getProperty())
            || PreferenceConstants.P_EMAIL.equalsIgnoreCase(event.getProperty())) {
          try {

            CloudBeesUIPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.P_ACTIVE_ACCOUNT, "");
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

  public void reloadAllCloudJenkins(final boolean userAction) {
    org.eclipse.core.runtime.jobs.Job job = new org.eclipse.core.runtime.jobs.Job("Loading DEV@cloud Jenkins instances") {
      @Override
      protected IStatus run(final IProgressMonitor monitor) {
        if (!getPreferenceStore().getBoolean(PreferenceConstants.P_ENABLE_JAAS)) {
          return new Status(Status.INFO, PLUGIN_ID, "DEV@cloud Continuous Integration is not enabled");
        }

        Exception toReport = null;

        try {
          monitor.beginTask("Reading DEV@cloud and Jenkins configuration", 1000);

          List<JenkinsInstance> instances = new ArrayList<JenkinsInstance>();
          try {
            instances.addAll(loadDevAtCloudInstances(monitor));
          } catch (Exception e) {
            if (toReport == null) {
              toReport = e;
            }
          }
          monitor.worked(250);

          List<JenkinsInstanceResponse> resp = null;
          if (instances==null || instances.size()==0) {
            resp = new ArrayList<JenkinsInstanceResponse>();             
          } else { 
            resp = pollInstances(instances, new SubProgressMonitor(monitor, 740));
          }

          //unmodifiableList to avoid ConcurrentModificationException for multithread access
          Iterator<CBRemoteChangeListener> iterator = Collections.unmodifiableList(
              CloudBeesUIPlugin.this.cbRemoteChangeListeners).iterator();
          while (iterator.hasNext()) {
            CBRemoteChangeListener listener = iterator.next();
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

  public void reloadAllLocalJenkins(final boolean userAction) {
    org.eclipse.core.runtime.jobs.Job job = new org.eclipse.core.runtime.jobs.Job("Loading local Jenkins instances") {
      @Override
      protected IStatus run(final IProgressMonitor monitor) {

        Exception toReport = null;

        try {
          monitor.beginTask("Reading Jenkins configuration", 1000);

          List<JenkinsInstance> instances = new ArrayList<JenkinsInstance>();
          try {
            instances.addAll(loadManualJenkinsInstances());
          } catch (Exception e) {
            if (toReport == null) {
              toReport = e;
            }
          }
          monitor.worked(250);

          List<JenkinsInstanceResponse> resp = pollInstances(instances, new SubProgressMonitor(monitor, 740));

          //unmodifiableList to avoid ConcurrentModificationException for multithread access
          Iterator<CBRemoteChangeListener> iterator = Collections.unmodifiableList(
              CloudBeesUIPlugin.this.cbRemoteChangeListeners).iterator();
          while (iterator.hasNext()) {
            CBRemoteChangeListener listener = iterator.next();
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
    job.schedule();

  }

  private List<JenkinsInstanceResponse> pollInstances(final List<JenkinsInstance> instances,
      final IProgressMonitor monitor) {
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

  public void addCBRemoteChangeListener(final CBRemoteChangeListener listener) {
    this.cbRemoteChangeListeners.add(listener);
  }

  public void removeCBRemoteChangeListener(final CBRemoteChangeListener listener) {
    this.cbRemoteChangeListeners.remove(listener);
  }

  public List<CBRemoteChangeListener> getJenkinsChangeListeners() {
    return this.cbRemoteChangeListeners;
  }

  public JenkinsService getJenkinsServiceForUrl(final String serviceOrViewOrJobUrl) {
    Iterator<JenkinsService> iter = new ArrayList<JenkinsService>(this.jenkinsRegistry).iterator();
    while (iter.hasNext()) {
      JenkinsService service = iter.next();
      if (serviceOrViewOrJobUrl.startsWith(service.getUrl())
          || (service.getAlternativeUrl() != null && serviceOrViewOrJobUrl.startsWith(service.getAlternativeUrl()))) {
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
                "Job parameter is missing", "Specify value for job parameter '" + def.name + "':",
                val != null ? val.value : "", null);
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

  private void loadAccountCredentials() throws CloudBeesException {

    String password;
    try {
      password = readP();
    } catch (StorageException e) {
      throw new CloudBeesException("Failed to load GrandCentral password from the storage!", e);
    }

    String email = getPreferenceStore().getString(PreferenceConstants.P_EMAIL);
    final GrandCentralService gcs = CloudBeesCorePlugin.getDefault().getGrandCentralService();
    final ClickStartService css = CloudBeesCorePlugin.getDefault().getClickStartService();
    gcs.setAuthInfo(email, password);

    if (email != null && email.length() > 0) {
      org.eclipse.core.runtime.jobs.Job job = new org.eclipse.core.runtime.jobs.Job("Validating CloudBees account") {
        protected IStatus run(final IProgressMonitor monitor) {
          try {
            AuthInfo auth = gcs.getCachedAuthInfo(true, monitor);
            css.setAuth(auth.getAuth().api_key, auth.getAuth().secret_key);

            if (USE_SECURE_STORAGE) {
              try {
                SecurePreferencesFactory.getDefault().put(PreferenceConstants.P_UID, auth.getAuth().uid, true);
                SecurePreferencesFactory.getDefault().put(PreferenceConstants.P_SECRET_KEY, auth.getAuth().secret_key, true);
                SecurePreferencesFactory.getDefault().put(PreferenceConstants.P_API_KEY, auth.getAuth().api_key, true);
              } catch (StorageException e) {
                e.printStackTrace();
              }
            } else {
              getPreferenceStore().setValue(PreferenceConstants.P_UID, auth.getAuth().uid);
              getPreferenceStore().setValue(PreferenceConstants.P_SECRET_KEY, auth.getAuth().secret_key);
              getPreferenceStore().setValue(PreferenceConstants.P_API_KEY, auth.getAuth().api_key);
            }

          } catch (CloudBeesException e) {
            CloudBeesUIPlugin.getDefault().getLogger().error(e.getMessage(), e);
            monitor.setTaskName("Failed to validate account: " + e.getMessage());
            monitor.done();
            return new Status(IStatus.ERROR, CloudBeesUIPlugin.PLUGIN_ID, 0, e.getMessage(), e);
          }
          monitor.done();
          return Status.OK_STATUS;
        }
      };
      job.setUser(false);
      try {
        job.schedule();
        job.join();
      } catch (InterruptedException e) {
        throw new CloudBeesException(e);
      }

      MultiAccountUtils.selectActiveAccount();
    }

  }

  public String getActiveAccountName(IProgressMonitor monitor) throws CloudBeesException {
    String active = CloudBeesCorePlugin.getDefault().getGrandCentralService().getActiveAccountName();
    if (active == null || active.length() == 0) {
      MultiAccountUtils.selectActiveAccount();
    }
    return active;
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
      getPreferenceStore().setValue(PreferenceConstants.P_PASSWORD, text);
    }

  }

  public String readP() throws StorageException {
    if (USE_SECURE_STORAGE) {
      return SecurePreferencesFactory.getDefault().get(PreferenceConstants.P_PASSWORD, "");
    }
    return getPreferenceStore().getString(PreferenceConstants.P_PASSWORD);
  }

  public void addApplicationInfoChangeListener(final ApplicationInfoChangeListener listener) {
    if (listener != null) {
      this.applicationInfoChangeListeners.add(listener);
    }
  }

  public void removeApplicationInfoChangeListener(final ApplicationInfoChangeListener listener) {
    if (listener != null) {
      this.applicationInfoChangeListeners.remove(listener);
    }
  }

  public List<ApplicationInfoChangeListener> getApplicationInfoChangeListeners() {
    return Collections.unmodifiableList(this.applicationInfoChangeListeners);
  }

  public void fireApplicationInfoChanged() {
    Iterator<ApplicationInfoChangeListener> iterator = getApplicationInfoChangeListeners().iterator();
    while (iterator.hasNext()) {
      ApplicationInfoChangeListener listener = iterator.next();
      listener.applicationInfoChanged();
    }
  }

  synchronized public List<ForgeInstance> getForgeRepos(final IProgressMonitor monitor) throws CloudBeesException {
    List<ForgeInstance> cloudRepos = CloudBeesCorePlugin.getDefault().getGrandCentralService().getForgeRepos(monitor);

    if (this.forgeRegistry == null) {
      IPreferenceStore store = CloudBeesUIPlugin.getDefault().getPreferenceStore();
      String instances = store.getString(PreferenceConstants.P_FORGE_INSTANCES);
      this.forgeRegistry = new ArrayList<ForgeInstance>(ForgeInstance.decode(instances));
    }

    for (ForgeInstance forge : cloudRepos) {
      int pos = this.forgeRegistry.indexOf(forge);
      if (pos >= 0) {
        ForgeInstance old = this.forgeRegistry.get(pos);
        forge.status = old.status;
      }
    }

    Collections.sort(cloudRepos);

    this.forgeRegistry.clear();
    this.forgeRegistry.addAll(cloudRepos);

    return cloudRepos;
  }

  @Override
  protected void initializeImageRegistry(ImageRegistry reg) {
    super.initializeImageRegistry(reg);

    reg.put(CBImages.ICON_16X16_CB_PLAIN,
        ImageDescriptor.createFromURL(getBundle().getResource("icons/16x16/cb_plain.png")));

    reg.put(CBImages.ICON_16X16_CB_CONSOLE,
        ImageDescriptor.createFromURL(getBundle().getResource("icons/16x16/cb_console.png")));

    reg.put(CBImages.ICON_16X16_NEW_CB_PROJ_WIZ,
        ImageDescriptor.createFromURL(getBundle().getResource("icons/16x16/cb_new_proj_wiz_ico_16x16.png")));

    reg.put(CBImages.ICON_CB_WIZARD, ImageDescriptor.createFromURL(getBundle().getResource("icons/cb_wiz_icon.png")));

  }

  public static Image getImage(final String imgKey) {
    return CloudBeesUIPlugin.getDefault().getImageRegistry().get(imgKey);
  }

  public static ImageDescriptor getImageDescription(final String imgKey) {
    CloudBeesUIPlugin pl = CloudBeesUIPlugin.getDefault();
    return pl != null ? pl.getImageRegistry().getDescriptor(imgKey) : null;
  }

  public static void logError(Throwable e) {
    IStatus status = createStatus(e);
    logStatus(status);
  }

  private static void logStatus(IStatus status) {
    plugin.getLog().log(status);
  }

  private static IStatus createStatus(Throwable e) {
    IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, e.getMessage());
    return status;
  }

  public static void logErrorAndShowDialog(Exception e) {
    final IStatus s = createStatus(e);
    logStatus(s);
    Display.getDefault().syncExec(new Runnable() {
      public void run() {
        ErrorDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "An error occured", null, s);
      }
    });
  }

  public void fireActiveAccountChanged(String newEmail, String newAccountName) {
    reloadAllCloudJenkins(false);
    fireApplicationInfoChanged();
    fireDatabaseInfoChanged();
    fireAccountNameChange(newEmail, newAccountName);
  }

  void fireAccountNameChange(String newEmail, String newAccountName) {
    Iterator<CBRemoteChangeListener> iterator = Collections.unmodifiableList(
        CloudBeesUIPlugin.this.cbRemoteChangeListeners).iterator();
    while (iterator.hasNext()) {
      CBRemoteChangeListener listener = iterator.next();
      listener.activeAccountChanged(newEmail, newAccountName);
    }
  }

  public void setActiveAccountName(String accountName) throws CloudBeesException {
    MultiAccountUtils.activateAccountName(accountName);
  }

  public void addDatabaseInfoChangeListener(final DatabaseInfoChangeListener listener) {
    if (listener != null) {
      this.databaseInfoChangeListeners.add(listener);
    }
  }

  public void removeDatabaseInfoChangeListener(final DatabaseInfoChangeListener listener) {
    if (listener != null) {
      this.databaseInfoChangeListeners.remove(listener);
    }
  }

  public List<DatabaseInfoChangeListener> getDatabaseInfoChangeListeners() {
    return Collections.unmodifiableList(this.databaseInfoChangeListeners);
  }

  public void fireDatabaseInfoChanged() {
    Iterator<DatabaseInfoChangeListener> iterator = getDatabaseInfoChangeListeners().iterator();
    while (iterator.hasNext()) {
      DatabaseInfoChangeListener listener = iterator.next();
      listener.databaseInfoChanged();
    }
  }

  public void setAuthStatus(AuthStatus newAuthStatus) {
    this.authStatus = newAuthStatus;
  }

  public AuthStatus getAuthStatus() {
    return authStatus;
  }

}
