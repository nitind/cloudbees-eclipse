package com.cloudbees.eclipse.dev.core;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class CloudBeesDevCorePlugin extends Plugin {

  private static BundleContext context;

  static BundleContext getContext() {
    return context;
  }

  /*
   * (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  public void start(final BundleContext bundleContext) throws Exception {
    CloudBeesDevCorePlugin.context = bundleContext;
  }

  /*
   * (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  public void stop(final BundleContext bundleContext) throws Exception {
    CloudBeesDevCorePlugin.context = null;
  }

}
