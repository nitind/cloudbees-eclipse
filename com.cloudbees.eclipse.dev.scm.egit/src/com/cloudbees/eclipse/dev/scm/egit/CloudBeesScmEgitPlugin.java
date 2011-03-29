package com.cloudbees.eclipse.dev.scm.egit;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

import com.cloudbees.eclipse.core.CloudBeesCorePlugin;
import com.cloudbees.eclipse.core.ForgeSyncService;

public class CloudBeesScmEgitPlugin extends Plugin {

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
    CloudBeesScmEgitPlugin.context = bundleContext;

    if ((ForgeSyncService.bundleActive("org.eclipse.egit.core") || ForgeSyncService.bundleActive("org.eclipse.egit"))
        && ForgeSyncService.bundleActive("org.eclipse.jgit")) {
      CloudBeesCorePlugin.getDefault().getGrandCentralService().addForgeSyncProvider(new ForgeEGitSync());
    }
  }

  /*
   * (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(final BundleContext bundleContext) throws Exception {
    CloudBeesScmEgitPlugin.context = null;
  }

}
