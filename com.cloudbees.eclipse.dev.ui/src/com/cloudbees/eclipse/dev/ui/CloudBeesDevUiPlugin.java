package com.cloudbees.eclipse.dev.ui;

import org.eclipse.team.internal.ui.Utils;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class CloudBeesDevUiPlugin extends AbstractUIPlugin {

  // The plug-in ID
  public static final String PLUGIN_ID = "com.cloudbees.eclipse.dev.ui"; //$NON-NLS-1$

  // The shared instance
  private static CloudBeesDevUiPlugin plugin;

  /**
   * The constructor
   */
  public CloudBeesDevUiPlugin() {
    try {
      Utils.openEditor(null, null);
    } catch (PartInitException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(final BundleContext context) throws Exception {
    super.start(context);
    plugin = this;
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(final BundleContext context) throws Exception {
    plugin = null;
    super.stop(context);
  }

  /**
   * Returns the shared instance
   *
   * @return the shared instance
   */
  public static CloudBeesDevUiPlugin getDefault() {
    return plugin;
  }

}
