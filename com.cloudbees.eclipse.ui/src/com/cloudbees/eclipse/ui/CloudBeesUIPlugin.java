package com.cloudbees.eclipse.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
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

  private NectarService ns = new NectarService(new NectarInstance("Netbeans", "http://deadlock.netbeans.org/hudson/"));
  private NectarService ns2 = new NectarService(new NectarInstance("jBoss", "http://hudson.jboss.org/hudson/"));
  private NectarService ns3 = new NectarService(new NectarInstance("Eclipse", "https://hudson.eclipse.org/hudson/"));
  private NectarService ns4 = new NectarService(new NectarInstance("ahti grandomstate",
      "https://grandomstate.ci.cloudbees.com/"));


  private List<NectarService> nectarRegistry = new ArrayList<NectarService>();

  private List<NectarChangeListener> nectarChangeListeners = new ArrayList<NectarChangeListener>();

  public CloudBeesUIPlugin() {
    super();
    nectarRegistry.add(ns);
    nectarRegistry.add(ns2);
    nectarRegistry.add(ns3);
    nectarRegistry.add(ns4);
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

  public void addNectarInstance(NectarInstance ni) {
    if (ni == null || ni.label == null || ni.url == null || ni.label.length() == 0 || ni.url.length() == 0) {
      throw new RuntimeException("Unable to add new instance with an empty url or label!");
    }
    List<NectarInstance> list = getManualNectarInstances();
    list.add(ni);
    CloudBeesUIPlugin.getDefault().getPreferenceStore()
        .putValue(PreferenceConstants.P_NECTAR_INSTANCES, NectarInstance.encode(list));
  }

  public List<NectarInstanceResponse> getManualNectarsInfo() throws CloudBeesException {
    List<NectarInstanceResponse> resp = new ArrayList<NectarInstanceResponse>();

    Iterator<NectarInstance> it = getManualNectarInstances().iterator();
    while (it.hasNext()) {
      NectarInstance inst = (NectarInstance) it.next();
      NectarService service = lookupNectarService(inst);
      try {
        resp.add(service.getInstance());
      } catch (CloudBeesException e) {
        System.out.println("Failed to contact " + service + ". Not adding to the list for now.");//TODO log
        //TODO Consider adding it to the list anyway, just mark it somehow as "Unreachable" in UI!
        e.printStackTrace();
      }
    }

    //FIXME let's pretend we have 2 manually configured nectars locally
    NectarService service = null;
    try {
      ;
      resp.add((service = ns).getInstance());
      resp.add((service = ns2).getInstance());
      resp.add((service = ns3).getInstance());
      resp.add((service = ns4).getInstance());
    } catch (CloudBeesException e) {
      System.out.println("Failed to contact " + service + ". Not adding to the list for now.");//TODO log
      //TODO Consider adding it to the list anyway, just mark it somehow as "Unreachable" in UI!
      e.printStackTrace();
    }

    return resp;
  }

  private NectarService lookupNectarService(NectarInstance inst) {
    NectarService service = getNectarServiceForUrl(inst.url);
    if (service == null) {
      service = new NectarService(inst);
      nectarRegistry.add(service);
    }
    return service;
  }

  public List<NectarInstanceResponse> getDevAtCloudNectarsInfo() throws CloudBeesException {
    List<NectarInstanceResponse> resp = new ArrayList<NectarInstanceResponse>();

    //FIXME let's pretend we have 1 dev at cloud nectar
    resp.add(ns.getInstance());
    return resp;
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

      //TODO Look up proper service based on resp.serviceUrl. assume "ns" for now.
      NectarJobsResponse jobs = ns.getJobs(viewUrl);

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
