package com.cloudbees.eclipse.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
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
import com.cloudbees.eclipse.core.util.Utils;
import com.cloudbees.eclipse.ui.views.jobdetails.JobDetailsView;
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

          String password;
          try {
            password = SecurePreferencesFactory.getDefault().get(PreferenceConstants.P_PASSWORD, "");
          } catch (StorageException e) {
            throw new InvocationTargetException(e);
          }

          String email = getPreferenceStore().getString(PreferenceConstants.P_EMAIL);

          try {
            CloudBeesCorePlugin.getDefault().getGrandCentralService().reloadForgeRepos(email, password, monitor);
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

  public List<NectarInstance> getManualNectarInstances() {
    IPreferenceStore store = CloudBeesUIPlugin.getDefault().getPreferenceStore();
    String instances = store.getString(PreferenceConstants.P_NECTAR_INSTANCES);
    List<NectarInstance> list = NectarInstance.decode(instances);
    return list;
  }

  public List<NectarInstance> getDevAtCloudInstances() {

    // TODO read from prefs
    //    String instances = store.getString(PreferenceConstants.P_DEVATCLOUD_INSTANCES);
    //    List<NectarInstance> list = NectarInstance.decode(instances);

    List<NectarInstance> list = new ArrayList<NectarInstance>();
    NectarInstance ns = new NectarInstance("111", "Netbeans", "http://deadlock.netbeans.org/hudson/");
    NectarInstance ns2 = new NectarInstance("222", "jBoss", "http://hudson.jboss.org/hudson/");
    NectarInstance ns3 = new NectarInstance("333", "Eclipse", "https://hudson.eclipse.org/hudson/");
    NectarInstance ns4 = new NectarInstance("444", "ahti grandomstate", "https://grandomstate.ci.cloudbees.com/");
    list.add(ns);
    list.add(ns2);
    list.add(ns3);
    list.add(ns4);

    // XXX hack
    for (NectarInstance ni : list) {
      if (getNectarServiceForUrl(ni.url) == null) {
        nectarRegistry.add(new NectarService(ni));
      }
    }

    return list;
  }

  public void saveNectarInstance(NectarInstance ni) {
    if (ni == null || ni.label == null || ni.url == null || ni.label.length() == 0 || ni.url.length() == 0) {
      throw new IllegalStateException("Unable to add new instance with an empty url or label!");
    }
    List<NectarInstance> list = getManualNectarInstances();
    list.remove(ni); // when editing - id is the same, but props old, so lets kill old instance first
    list.add(ni);
    Collections.sort(list);
    CloudBeesUIPlugin.getDefault().getPreferenceStore()
        .putValue(PreferenceConstants.P_NECTAR_INSTANCES, NectarInstance.encode(list));
  }

  public void removeNectarInstance(NectarInstance ni) {
    if (ni == null) {
      throw new RuntimeException("Unable to remove null instance!");
    }
    List<NectarInstance> list = getManualNectarInstances();
    list.remove(ni);
    CloudBeesUIPlugin.getDefault().getPreferenceStore()
        .putValue(PreferenceConstants.P_NECTAR_INSTANCES, NectarInstance.encode(list));
  }

  public List<NectarInstanceResponse> getManualNectarsInfo() throws CloudBeesException {
    List<NectarInstance> instances = new ArrayList<NectarInstance>(getManualNectarInstances());

    List<NectarInstanceResponse> resp = pollInstances(instances);

    return resp;
  }

  public List<NectarInstanceResponse> getDevAtCloudNectarsInfo() throws CloudBeesException {
    List<NectarInstance> instances = new ArrayList<NectarInstance>(getDevAtCloudInstances());

    List<NectarInstanceResponse> resp = pollInstances(instances);

    return resp;
  }

  private List<NectarInstanceResponse> pollInstances(List<NectarInstance> instances) {
    List<NectarInstanceResponse> resp = new ArrayList<NectarInstanceResponse>();
    for (NectarInstance inst : instances) {
      NectarService service = lookupNectarService(inst);
      try {
        resp.add(service.getInstance());
      } catch (CloudBeesException e) {
        System.out.println("Failed to contact " + service + ". Not adding to the list for now.");//TODO log

        //TODO Consider adding it to the list anyway, just mark it somehow as "Unreachable" in UI!
        NectarInstanceResponse fakeResp = new NectarInstanceResponse();
        fakeResp.serviceUrl = inst.url;
        fakeResp.nodeName = inst.label;
        fakeResp.views = new NectarInstanceResponse.View[0];
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
    try {

      PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(JobsView.ID);

      //TODO Start monitoring this job list

      NectarJobsResponse jobs = getNectarServiceForUrl(serviceUrl).getJobs(viewUrl);

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

  public NectarService getNectarServiceForUrl(String serviceUrl) {
    Iterator<NectarService> iter = nectarRegistry.iterator();
    while (iter.hasNext()) {
      NectarService service = (NectarService) iter.next();
      if (service.getUrl().equals(serviceUrl)) {
        return service;
      }
    }
    return null;
  }

  public void showJobDetails(String jobUrl) {
    if (jobUrl == null) {
      return;
    }
    // Look up the service
    Iterator<NectarService> it = nectarRegistry.iterator();
    while (it.hasNext()) {
      NectarService service = (NectarService) it.next();
      if (jobUrl.startsWith(service.getUrl())) {

        try {
          PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
              .showView(JobDetailsView.ID, Utils.toB64(jobUrl), IWorkbenchPage.VIEW_ACTIVATE);
        } catch (PartInitException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        return;
      }
    }

  }

}
