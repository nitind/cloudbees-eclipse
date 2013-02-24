package com.cloudbees.eclipse.dev.scm.egit;

import org.eclipse.jsch.core.IJSchService;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

public class CloudBeesScmEgitPlugin extends AbstractUIPlugin {

  private static CloudBeesScmEgitPlugin plugin;
  private ServiceTracker tracker;

  /*
   * (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(final BundleContext bundleContext) throws Exception {
    super.start(bundleContext);
    plugin = this;
    tracker = new ServiceTracker(
        getBundle().getBundleContext(),
        IJSchService.class.getName(),
         null);
     tracker.open();
  }

  /*
   * (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(final BundleContext bundleContext) throws Exception {
    plugin = null;
    super.stop(bundleContext);
    tracker.close();
  }

  public IJSchService getJSchService() {
    return (IJSchService)tracker.getService();
  }
  
  public static CloudBeesScmEgitPlugin getDefault() {
    return plugin;
  }
}
