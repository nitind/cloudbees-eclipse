package com.cloudbees.eclipse.dev.scm.subclipse;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.cloudbees.eclipse.core.CloudBeesCorePlugin;
import com.cloudbees.eclipse.core.ForgeSyncService;

public class CloudBeesScmSubclipsePlugin extends AbstractUIPlugin {

  private static BundleContext context;
  private static CloudBeesScmSubclipsePlugin plugin;

  static BundleContext getContext() {
    return context;
  }

  /*
   * (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(final BundleContext bundleContext) throws Exception {
    super.start(context);
    CloudBeesScmSubclipsePlugin.context = bundleContext;

    if (ForgeSyncService.bundleActive("org.tigris.subversion.subclipse.core")) {
      CloudBeesCorePlugin.getDefault().getGrandCentralService().addForgeSyncProvider(new ForgeSubclipseSync());
    }

    plugin = this;
  }

  /*
   * (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(final BundleContext bundleContext) throws Exception {
    super.stop(context);
    CloudBeesScmSubclipsePlugin.context = null;
    plugin = null;
  }

  public static CloudBeesScmSubclipsePlugin getDefault() {
    return plugin;
  }
}
