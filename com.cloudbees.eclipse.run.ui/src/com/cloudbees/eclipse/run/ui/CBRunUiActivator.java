package com.cloudbees.eclipse.run.ui;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
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
    IStatus status = createStatus(e);
    logStatus(status);
  }

  private static void logStatus(IStatus status) {
    plugin.getLog().log(status);
  }

  private static IStatus createStatus(Throwable e) {
    IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, e.getMessage());
    return status;
  }

  public static void logErrorAndShowDialog(Exception e) {
    final IStatus s = createStatus(e);
    logStatus(s);
    Display.getDefault().syncExec(new Runnable() {

      @Override
      public void run() {
        ErrorDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "An error occured", null, s);
      }
    });
  }

  @Override
  protected void initializeImageRegistry(ImageRegistry reg) {
    super.initializeImageRegistry(reg);
    reg.put(Images.CLOUDBEES_ICON_16x16, imageDescriptorFromPlugin(PLUGIN_ID, Images.CLOUDBEES_ICON_16x16_PATH));
    reg.put(Images.CLOUDBEES_TOMCAT_ICON, imageDescriptorFromPlugin(PLUGIN_ID, Images.CLOUDBEES_TOMCAT_ICON_PATH));
    reg.put(Images.CLOUDBEES_FOLDER, imageDescriptorFromPlugin(PLUGIN_ID, Images.CLOUDBEES_FOLDER_PATH));
    reg.put(Images.CLOUDBEES_WIZ_ICON, imageDescriptorFromPlugin(PLUGIN_ID, Images.CLOUDBEES_WIZ_ICON_PATH));
  }

  public static Image getImage(final String imgKey) {
    return CBRunUiActivator.getDefault().getImageRegistry().get(imgKey);
  }

}
