package com.cloudbees.eclipse.ui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.cloudbees.eclipse.core.CloudBeesCorePlugin;
import com.cloudbees.eclipse.core.CloudBeesException;

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
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
   * )
   */
  public void stop(BundleContext context) throws Exception {
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
    Status status = new Status(IStatus.ERROR, "Error!", 0, msg, e);
    ErrorDialog.openError(Display.getCurrent().getActiveShell(), "Error!", msg, status);
  }

}
