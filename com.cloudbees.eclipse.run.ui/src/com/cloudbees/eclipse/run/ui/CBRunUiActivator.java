package com.cloudbees.eclipse.run.ui;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

/**
 * The activator class controls the plug-in life cycle
 */
public class CBRunUiActivator extends AbstractUIPlugin {

  // The plug-in ID
  public static final String PLUGIN_ID = "com.cloudbees.eclipse.run.ui"; //$NON-NLS-1$

  // The shared instance
  private static CBRunUiActivator plugin;
  private final ProjectDeleteListener projectDeleteListener;

  /**
   * The constructor
   */
  public CBRunUiActivator() {
    this.projectDeleteListener = new ProjectDeleteListener();
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);
    CloudBeesUIPlugin.getDefault(); // initialize this, so can use CloudBeesCore
    plugin = this;
    // ResourcesPlugin.getWorkspace().addResourceChangeListener(projectDeleteListener);
    ResourcesPlugin.getWorkspace().addResourceChangeListener(this.projectDeleteListener,
        IResourceChangeEvent.PRE_DELETE);
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(BundleContext context) throws Exception {
    ResourcesPlugin.getWorkspace().removeResourceChangeListener(this.projectDeleteListener);
    plugin = null;
    super.stop(context);
  }

  /**
   * Returns the shared instance
   * 
   * @return the shared instance
   */
  public static CBRunUiActivator getDefault() {
    return plugin;
  }

  public static void logError(Throwable e) {
    IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, e.getMessage());
    plugin.getLog().log(status);
  }

  @Override
  protected void initializeImageRegistry(ImageRegistry reg) {
    super.initializeImageRegistry(reg);
    reg.put(Images.CLOUDBEES_ICON_16x16, imageDescriptorFromPlugin(PLUGIN_ID, Images.CLOUDBEES_ICON_16x16_PATH));
    reg.put(Images.CLOUDBEES_TOMCAT_ICON, imageDescriptorFromPlugin(PLUGIN_ID, Images.CLOUDBEES_TOMCAT_ICON_PATH));
    reg.put(Images.CLOUDBEES_FOLDER, imageDescriptorFromPlugin(PLUGIN_ID, Images.CLOUDBEES_FOLDER_PATH));
  }

}
