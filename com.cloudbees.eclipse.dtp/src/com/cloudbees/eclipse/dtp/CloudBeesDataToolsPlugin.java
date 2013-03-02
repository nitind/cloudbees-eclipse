package com.cloudbees.eclipse.dtp;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.cloudbees.eclipse.core.Logger;
import com.cloudbees.eclipse.dtp.internal.DatabasePoller;
import com.cloudbees.eclipse.run.core.ApplicationPoller;

public class CloudBeesDataToolsPlugin extends AbstractUIPlugin {

  public static final String PLUGIN_ID = "com.cloudbees.eclipse.dtp"; //$NON-NLS-1$

  private Logger logger;
  
  private static CloudBeesDataToolsPlugin plugin;

  private static DatabasePoller poller = new DatabasePoller();
  
  
  /*
   * (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(final BundleContext bundleContext) throws Exception {
    super.start(bundleContext);
    plugin = this;
    this.logger = new Logger(getLog());
    getPoller().start();
  }

  /*
   * (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(final BundleContext bundleContext) throws Exception {
    getPoller().halt();
    plugin = null;
    logger = null;
    super.stop(bundleContext);
  }

  public static CloudBeesDataToolsPlugin getDefault() {
    return plugin;
  }
  
  @Override
  protected void initializeImageRegistry(ImageRegistry reg) {
    super.initializeImageRegistry(reg);
    reg.put(Images.CLOUDBEES_ICON_16x16, imageDescriptorFromPlugin(PLUGIN_ID, Images.CLOUDBEES_ICON_16x16_PATH));
    reg.put(Images.JDBC_16_ICON, imageDescriptorFromPlugin(PLUGIN_ID, Images.JDBC_16_PATH));
    reg.put(Images.CLOUDBEES_FOLDER, imageDescriptorFromPlugin(PLUGIN_ID, Images.CLOUDBEES_FOLDER_PATH));
    reg.put(Images.CLOUDBEES_REFRESH, imageDescriptorFromPlugin(PLUGIN_ID, Images.CLOUDBEES_REFRESH_PATH));
  }

  public static Image getImage(final String imgKey) {
    return CloudBeesDataToolsPlugin.getDefault().getImageRegistry().get(imgKey);
  }

  public static ImageDescriptor getImageDescription(final String imgKey) {
    CloudBeesDataToolsPlugin pl = CloudBeesDataToolsPlugin.getDefault();
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

  public static void logErrorAndShowDialog(final Exception e) {
    final IStatus s = createStatus(e);
    logStatus(s);
    Display.getDefault().syncExec(new Runnable() {

      @Override
      public void run() {
        String msg = e.getMessage();
        if (msg==null && e.getCause()!=null) {
          msg = e.getCause().getMessage();
        }
        if (msg==null) {
          msg = e.getClass().getName();
        }
        ErrorDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "An error occured", msg, s);
      }
    });
  }

  public Logger getLogger() {
    return this.logger;
  }

  public static DatabasePoller getPoller() {
    return poller;
  }
  
}
