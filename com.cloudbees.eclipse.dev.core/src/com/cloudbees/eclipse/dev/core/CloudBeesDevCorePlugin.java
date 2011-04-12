package com.cloudbees.eclipse.dev.core;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class CloudBeesDevCorePlugin extends Plugin {

  // The plug-in ID
  public static final String PLUGIN_ID = "com.cloudbees.eclipse.dev.core"; //$NON-NLS-1$

  private static BundleContext context;
  private static CloudBeesDevCorePlugin plugin;

  static BundleContext getContext() {
    return context;
  }

  /*
   * (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(final BundleContext bundleContext) throws Exception {
    CloudBeesDevCorePlugin.context = bundleContext;
    plugin = this;
  }

  /*
   * (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(final BundleContext bundleContext) throws Exception {
    CloudBeesDevCorePlugin.context = null;
    plugin = null;
  }

  /**
   * Returns the shared instance
   * 
   * @return the shared instance
   */
  public static CloudBeesDevCorePlugin getDefault() {
    return plugin;
  }
}
