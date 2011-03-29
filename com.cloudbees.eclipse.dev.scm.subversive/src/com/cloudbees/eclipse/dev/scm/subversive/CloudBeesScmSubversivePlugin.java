package com.cloudbees.eclipse.dev.scm.subversive;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

import com.cloudbees.eclipse.core.CloudBeesCorePlugin;
import com.cloudbees.eclipse.core.ForgeSyncService;

public class CloudBeesScmSubversivePlugin extends Plugin {

  private static BundleContext context;

  static BundleContext getContext() {
    return context;
  }

  /*
   * (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(final BundleContext bundleContext) throws Exception {
    CloudBeesScmSubversivePlugin.context = bundleContext;

    if (ForgeSyncService.bundleActive("org.eclipse.team.svn.core")) {
      CloudBeesCorePlugin.getDefault().getGrandCentralService().addForgeSyncProvider(new ForgeSubversiveSync());
    }
  }

  /*
   * (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(final BundleContext bundleContext) throws Exception {
    CloudBeesScmSubversivePlugin.context = null;
  }

}
