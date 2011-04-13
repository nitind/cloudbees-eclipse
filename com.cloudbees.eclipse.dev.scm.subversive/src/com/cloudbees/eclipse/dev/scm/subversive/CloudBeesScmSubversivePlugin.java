package com.cloudbees.eclipse.dev.scm.subversive;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class CloudBeesScmSubversivePlugin extends Plugin {

  /*
   * (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(final BundleContext bundleContext) throws Exception {
    //    if (ForgeSyncService.bundleActive("org.eclipse.team.svn.core")) {
    //      CloudBeesCorePlugin.getDefault().getGrandCentralService().addForgeSyncProvider(new ForgeSubversiveSync());
    //    }
    super.start(bundleContext);
  }

  /*
   * (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(final BundleContext bundleContext) throws Exception {
    super.stop(bundleContext);
  }

}
