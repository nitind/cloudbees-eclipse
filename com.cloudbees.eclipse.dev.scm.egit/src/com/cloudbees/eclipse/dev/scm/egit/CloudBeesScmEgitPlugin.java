package com.cloudbees.eclipse.dev.scm.egit;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class CloudBeesScmEgitPlugin extends AbstractUIPlugin {

  private static CloudBeesScmEgitPlugin plugin;

  /*
   * (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(final BundleContext bundleContext) throws Exception {
    super.start(bundleContext);

    //    if ((ForgeSyncService.bundleActive("org.eclipse.egit.core") || ForgeSyncService.bundleActive("org.eclipse.egit"))
    //        && ForgeSyncService.bundleActive("org.eclipse.jgit")) {
    //      CloudBeesCorePlugin.getDefault().getGrandCentralService().addForgeSyncProvider(new ForgeEGitSync());
    //    }

    this.plugin = this;
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

  public static CloudBeesScmEgitPlugin getDefault() {
    return plugin;
  }
}
