package com.cloudbees.eclipse.run.core;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class CBRunCoreActivator implements BundleActivator {
  
  //The plug-in ID
  public static final String PLUGIN_ID = "com.cloudbees.eclipse.run.core"; //$NON-NLS-1$
  
  private static BundleContext context;

  static BundleContext getContext() {
    return context;
  }

  /*
   * (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  public void start(BundleContext bundleContext) throws Exception {
    CBRunCoreActivator.context = bundleContext;
  }

  /*
   * (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  public void stop(BundleContext bundleContext) throws Exception {
    CBRunCoreActivator.context = null;
  }

}
