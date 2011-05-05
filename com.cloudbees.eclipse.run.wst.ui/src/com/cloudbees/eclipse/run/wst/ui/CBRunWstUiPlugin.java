package com.cloudbees.eclipse.run.wst.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class CBRunWstUiPlugin extends AbstractUIPlugin {

  // The plug-in ID
  public static final String PLUGIN_ID = "com.cloudbees.eclipse.run.wst.ui"; //$NON-NLS-1$

  // The shared instance
  private static CBRunWstUiPlugin plugin;

  /**
   * The constructor
   */
  public CBRunWstUiPlugin() {
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);
    plugin = this;
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(BundleContext context) throws Exception {
    plugin = null;
    super.stop(context);
  }

  /**
   * Returns the shared instance
   * 
   * @return the shared instance
   */
  public static CBRunWstUiPlugin getDefault() {
    return plugin;
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
    IStatus s = createStatus(e);
    logStatus(s);
    ErrorDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "An error occured", null, s);
  }
}
