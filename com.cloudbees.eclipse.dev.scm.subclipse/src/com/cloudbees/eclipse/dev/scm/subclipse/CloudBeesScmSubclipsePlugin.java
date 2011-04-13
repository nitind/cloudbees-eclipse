package com.cloudbees.eclipse.dev.scm.subclipse;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class CloudBeesScmSubclipsePlugin extends AbstractUIPlugin {

  private static CloudBeesScmSubclipsePlugin plugin;

  /*
   * (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(final BundleContext bundleContext) throws Exception {
    super.start(bundleContext);

    //    if (ForgeSyncService.bundleActive("org.tigris.subversion.subclipse.core")) {
    //      CloudBeesCorePlugin.getDefault().getGrandCentralService().addForgeSyncProvider(new ForgeSubclipseSync());
    //    }

    plugin = this;
  }

  /*
   * (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(final BundleContext bundleContext) throws Exception {
    this.plugin = null;
    super.stop(bundleContext);
  }

  public static CloudBeesScmSubclipsePlugin getDefault() {
    return plugin;
  }
}
