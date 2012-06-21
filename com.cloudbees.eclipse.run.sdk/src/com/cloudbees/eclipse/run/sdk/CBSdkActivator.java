package com.cloudbees.eclipse.run.sdk;

import java.io.IOException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class CBSdkActivator extends Plugin {
  // The plug-in ID
  public static final String PLUGIN_ID = "com.cloudbees.eclipse.run.sdk"; //$NON-NLS-1$

  private String sdkLocation = null;

  // The shared instance
  private static CBSdkActivator plugin;

  /**
   * The constructor
   */
  public CBSdkActivator() {
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);
    plugin = this;
    System.setProperty("bees.home", getBeesHome());    
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(BundleContext context) throws Exception {
    plugin = null;
    super.stop(context);
  }

  /**
   * Returns the shared instance
   * 
   * @return the shared instance
   */
  public static CBSdkActivator getDefault() {
    return plugin;
  }

  public String getBeesHome() {
    if (this.sdkLocation == null) {
      Path path = new Path("cloudbees-sdk");

      try {
        this.sdkLocation = FileLocator.toFileURL(FileLocator.find(plugin.getBundle(), path, null)).getFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return this.sdkLocation;
  }
}
