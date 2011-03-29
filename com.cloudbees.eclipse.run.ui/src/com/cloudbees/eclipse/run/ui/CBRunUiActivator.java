package com.cloudbees.eclipse.run.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class CBRunUiActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.cloudbees.eclipse.run.ui"; //$NON-NLS-1$

	// The shared instance
	private static CBRunUiActivator plugin;
	
	/**
	 * The constructor
	 */
	public CBRunUiActivator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
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
	
	public static void logError(Exception e) {
	  IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, e.getMessage());
	  plugin.getLog().log(status);
	}
	
	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {
	  super.initializeImageRegistry(reg);
	  reg.put(Images.CLOUDBEES_ICON_16x16, imageDescriptorFromPlugin(PLUGIN_ID, Images.CLOUDBEES_ICON_16x16_PATH));
	}
}
