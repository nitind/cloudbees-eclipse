package com.cloudbees.eclipse.core;

import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author ahtik
 */
public class CloudBeesCorePlugin extends Plugin {

  // The plug-in ID
  public static final String PLUGIN_ID = "com.cloudbees.eclipse.core"; //$NON-NLS-1$

  // The shared instance
  private static CloudBeesCorePlugin plugin;

  private GrandCentralService gcService;

  private ServiceTracker proxyServiceTracker;

  private Logger logger;

  /**
   * The constructor
   */
  public CloudBeesCorePlugin() {
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
   */
  public void start(BundleContext context) throws Exception {
    super.start(context);
    plugin = this;
    gcService = new GrandCentralService();
    gcService.start();
    
    proxyServiceTracker = new ServiceTracker(getBundle().getBundleContext(), IProxyService.class.getName(), null);
    proxyServiceTracker.open();

    logger = new Logger(getLog());

  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
   */
  public void stop(BundleContext context) throws Exception {
    logger = null;
    plugin = null;
    if (gcService != null) {
      gcService.stop();
      gcService = null;
    }
    if (proxyServiceTracker!=null) {
      proxyServiceTracker.close();
    }
    super.stop(context);
  }

  /**
   * Returns the shared instance
   * 
   * @return the shared instance
   */
  public static CloudBeesCorePlugin getDefault() {
    return plugin;
  }

  public GrandCentralService getGrandCentralService() throws CloudBeesException {
    if (gcService == null) {
      throw new CloudBeesException("CloudBeesCorePlugin not yet initialized!");
    }
    return gcService;
  }

  public Logger getLogger() {
    return logger;
  }

}
