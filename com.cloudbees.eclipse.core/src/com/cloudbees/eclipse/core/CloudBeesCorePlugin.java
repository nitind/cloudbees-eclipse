package com.cloudbees.eclipse.core;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.net.proxy.IProxyChangeEvent;
import org.eclipse.core.net.proxy.IProxyChangeListener;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.Bundle;
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
    this.gcService = new GrandCentralService();
    this.gcService.start();

    this.proxyServiceTracker = new ServiceTracker(getBundle().getBundleContext(), IProxyService.class.getName(), null);
    this.proxyServiceTracker.open();

    IProxyService ps = getProxyService();
    if (ps != null) {
      ps.addProxyChangeListener(new IProxyChangeListener() {
        @Override
        public void proxyInfoChanged(IProxyChangeEvent event) {
          updateCBProxyConfig();
        }
      });

    }
    this.logger = new Logger(getLog());

  }

  public IProxyService getProxyService() {
    return (IProxyService) proxyServiceTracker.getService();
  }

  private void updateCBProxyConfig() {
    IProxyService ps = getProxyService();

    boolean proxySet = false;

    if (ps.isProxiesEnabled()) {

      //NOTE! For now we use just the first proxy settings with type HTTP to try out the connection. If configuration has more than 1 conf then for now this likely won't work!
      // We take the whole proxy conf into account as we can't determine the host at this point. The assumption is that if any http proxy is available then we use this for the connection.
      IProxyData[] pr = ps.getProxyData();
      if (pr != null) {
        for (int i = 0; i < pr.length; i++) {

          IProxyData prd = pr[i];

          if (IProxyData.HTTP_PROXY_TYPE.equals(prd.getType()) || IProxyData.HTTPS_PROXY_TYPE.equals(prd.getType())) {

            String proxyHost = prd.getHost();
            int proxyPort = prd.getPort();
            String proxyUser = prd.getUserId();
            String proxyPass = prd.getPassword();

            if (proxyHost == null || proxyHost.length() == 0) {
              continue; // ignore empty proxy conf
            }

            proxySet = true;

            System.setProperty("bees.api.proxy.host", proxyHost);
            System.setProperty("bees.api.proxy.port", proxyPort + "");

            if (prd.getType().equals(IProxyData.HTTP_PROXY_TYPE)) {
              System.setProperty("http.proxyHost", proxyHost);
              System.setProperty("http.proxyPort", proxyPort + "");
            } else if (prd.getType().equals(IProxyData.HTTPS_PROXY_TYPE)) {
              System.setProperty("https.proxyHost", proxyHost);
              System.setProperty("https.proxyPort", proxyPort + "");
            }

            if (prd.isRequiresAuthentication()) {
              System.setProperty("bees.api.proxy.user", proxyUser);
              System.setProperty("bees.api.proxy.password", proxyPass);

              if (prd.getType().equals(IProxyData.HTTP_PROXY_TYPE)) {
                System.setProperty("http.proxyUser", proxyUser);
                System.setProperty("http.proxyPassword", proxyPass);
              } else if (prd.getType().equals(IProxyData.HTTPS_PROXY_TYPE)) {
                System.setProperty("https.proxyUser", proxyUser);
                System.setProperty("https.proxyPassword", proxyPass);
              }

            }

            // We don't break the for loop in order to configure both http and https system props.
            // Although bees.api gets called twice, it shouldn't be an issue (only when http and https proxy host and port differ).
            // break;

          }

        }
      }
    }

    // no proxy configured, clear from the props!
    if (!proxySet) {
      System.clearProperty("bees.api.proxy.host");
      System.clearProperty("bees.api.proxy.port");

      System.clearProperty("http.proxyHost");
      System.clearProperty("http.proxyPort");
      System.clearProperty("http.proxyUser");
      System.clearProperty("http.proxyPassword");

      System.clearProperty("https.proxyHost");
      System.clearProperty("https.proxyPort");
      System.clearProperty("https.proxyUser");
      System.clearProperty("https.proxyPassword");
    }

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
    if (this.proxyServiceTracker != null) {
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

  public static File getEmptyConfigXML() throws IOException {
    Bundle bundle = CloudBeesCorePlugin.getDefault().getBundle();
    Path path = new Path("scripts/empty_config.xml");
    return new File(FileLocator.toFileURL(FileLocator.find(bundle, path, null)).getFile());
  }

  public static File getSCMConfigXML() throws IOException {
    Bundle bundle = CloudBeesCorePlugin.getDefault().getBundle();
    Path path = new Path("scripts/scm_config.xml");
    return new File(FileLocator.toFileURL(FileLocator.find(bundle, path, null)).getFile());
  }

  public static boolean validateRUNatCloudJRE() {
    //1.6.0_29, 1.7.0_06-ea etc.
    String prop = System.getProperty("java.version");
    return !(prop != null && prop.contains("1.7"));
  }

}
