package com.cloudbees.eclipse.run.core;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

import com.cloudbees.eclipse.run.core.launchconfiguration.CBProjectProcessService;

public class CBRunCoreActivator extends Plugin {

  //The plug-in ID
  public static final String PLUGIN_ID = "com.cloudbees.eclipse.run.core"; //$NON-NLS-1$

  private static BundleContext context;

  private static CBRunCoreActivator plugin;

  static BundleContext getContext() {
    return context;
  }

  /*
   * (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(BundleContext bundleContext) throws Exception {
    CBRunCoreActivator.context = bundleContext;
    plugin = this;
  }

  /*
   * (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(BundleContext bundleContext) throws Exception {
    CBProjectProcessService.getInstance().terminateAllProcesses();
    CBRunCoreActivator.context = null;
    plugin = null;
  }

  public static void logError(Exception e) {
    IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, e.getMessage());
    plugin.getLog().log(status);
  }

}
