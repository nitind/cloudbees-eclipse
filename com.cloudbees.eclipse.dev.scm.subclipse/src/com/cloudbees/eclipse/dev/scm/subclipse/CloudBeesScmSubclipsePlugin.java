package com.cloudbees.eclipse.dev.scm.subclipse;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

import com.cloudbees.eclipse.core.CloudBeesCorePlugin;
import com.cloudbees.eclipse.core.ForgeSyncService;

public class CloudBeesScmSubclipsePlugin extends Plugin {

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
    CloudBeesScmSubclipsePlugin.context = bundleContext;

    if (ForgeSyncService.bundleActive("org.tigris.subversion.subclipse.core")) {
      CloudBeesCorePlugin.getDefault().getGrandCentralService().addForgeSyncProvider(new ForgeSubclipseSync());
    }
  }

  /*
   * (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(final BundleContext bundleContext) throws Exception {
    CloudBeesScmSubclipsePlugin.context = null;
  }

}
