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
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.cloudbees.eclipse.core.CloudBeesCorePlugin;
import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.Logger;
import com.cloudbees.eclipse.core.NectarService;
import com.cloudbees.eclipse.core.domain.NectarInstance;
import com.cloudbees.eclipse.core.nectar.api.NectarInstanceResponse;

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

  private NectarService ns = new NectarService("http://deadlock.netbeans.org/hudson/");
  private NectarService ns2 = new NectarService("http://hudson.jboss.org/hudson/");

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
      resp.add(service.getViews());
    }

    //FIXME let's pretend we have 2 manually configured nectars locally
    resp.add(ns.getViews());
    resp.add(ns2.getViews());

    return resp;
  }

  private NectarService lookupNectarService(NectarInstance inst) {
    // TODO Auto-generated method stub
    //dummily respond with the same nb deadlock service for now
    return ns;
  }

  public List<NectarInstanceResponse> getDevAtCloudNectarsInfo() throws CloudBeesException {
    List<NectarInstanceResponse> resp = new ArrayList<NectarInstanceResponse>();

    //FIXME let's pretend we have 1 dev at cloud nectar
    resp.add(ns.getViews());
    return resp;
  }

}
