package com.cloudbees.eclipse.dtp;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class CloudBeesDataToolsPlugin extends AbstractUIPlugin {

  private static CloudBeesDataToolsPlugin plugin;

  /*
   * (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(final BundleContext bundleContext) throws Exception {
    super.start(bundleContext);
    plugin = this;
  }

  /*
   * (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(final BundleContext bundleContext) throws Exception {
    plugin = null;
    super.stop(bundleContext);
  }

  public static CloudBeesDataToolsPlugin getDefault() {
    return plugin;
  }
}
