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

  public static final String PLUGIN_ID = "com.cloudbees.eclipse.core"; //$NON-NLS-1$

  public static final String[] DEFAULT_NATURES = new String[] { CloudBeesNature.NATURE_ID };

  /** The shared instance */
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
  @Override
  public void start(final BundleContext context) throws Exception {
    super.start(context);
    plugin = this;
    this.gcService = new GrandCentralService(null, null);
    this.gcService.start();

    this.proxyServiceTracker = new ServiceTracker(getBundle().getBundleContext(), IProxyService.class.getName(), null);
    this.proxyServiceTracker.open();

    this.logger = new Logger(getLog());

  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(final BundleContext context) throws Exception {
    this.logger = null;
    plugin = null;
    if (this.gcService != null) {
      this.gcService.stop();
      this.gcService = null;
    }
    if (this.proxyServiceTracker!=null) {
      this.proxyServiceTracker.close();
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
    if (this.gcService == null) {
      throw new CloudBeesException("CloudBeesCorePlugin not yet initialized!");
    }
    return this.gcService;
  }

  public Logger getLogger() {
    return this.logger;
  }

}
