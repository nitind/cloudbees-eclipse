/*******************************************************************************
 * Copyright (c) 2013 Cloud Bees, Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *              http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.cloudbees.eclipse.run.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.osgi.framework.BundleContext;

import com.cloudbees.eclipse.core.CloudBeesCorePlugin;
import com.cloudbees.eclipse.run.core.launchconfiguration.CBLaunchConfigurationConstants;
import com.cloudbees.eclipse.run.core.launchconfiguration.CBProjectProcessService;
import com.cloudbees.eclipse.run.core.util.CBRunUtil;
import com.cloudbees.eclipse.run.sdk.CBSdkActivator;

public class CBRunCoreActivator extends Plugin {

  //The plug-in ID
  public static final String PLUGIN_ID = "com.cloudbees.eclipse.run.core"; //$NON-NLS-1$

  private static BundleContext context;

  private static CBRunCoreActivator plugin;

  private static ApplicationPoller poller = new ApplicationPoller();

  static BundleContext getContext() {
    return context;
  }

  /*
   * (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(BundleContext bundleContext) throws Exception {
    
    super.start(bundleContext);

    if (!CloudBeesCorePlugin.validateRUNatCloudJRE()) {
      // Throwing exception disables the plugin.
      throw new Exception(
          "Java SE 7 is not supported by CloudBees RUN@cloud. Disabling com.cloudbees.eclipse.run.core functionality.");
    }

    CBRunCoreActivator.context = bundleContext;
    plugin = this;

    cleanupCBLaunchConfigs();

    getPoller().start();
  }

  private void cleanupCBLaunchConfigs() {
    // Clean up launch configurations to get rid of bees.home variable from old launch config instances.

    String beesHome = CBSdkActivator.getDefault().getBeesHome();

    ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
    try {
      for (ILaunchConfiguration configuration : launchManager.getLaunchConfigurations()) {
        String name = configuration.getAttribute(CBLaunchConfigurationConstants.ATTR_CB_PROJECT_NAME, "");

        /*
         * adjust settings for both cb launch types (deploy and local run)
         *         if (!CBLaunchConfigurationConstants.ID_CB_LAUNCH.equals(configuration.getType().getIdentifier())) {
                  continue;
                }
        */
        if (name != null && name.length() > 0) {

          boolean dirty = false;

          if (!configuration.hasAttribute("org.eclipse.jdt.launching.CLASSPATH_PROVIDER")) {
            dirty = true;
          }

          if (!configuration.hasAttribute("org.eclipse.jdt.launching.DEFAULT_CLASSPATH")) {
            dirty = true;
          }

          if (configuration.hasAttribute("org.eclipse.ui.externaltools.ATTR_ANT_PROPERTIES")) {
            Map m = configuration.getAttribute("org.eclipse.ui.externaltools.ATTR_ANT_PROPERTIES", (Map) null);
            if (m != null) {
              if (!m.containsKey("bees.home") || !m.get("bees.home").equals(beesHome)) {
                dirty = true;
              }
            }
          } else {
            dirty = true; // if no props then add to have bees.home
          }

          //if (name.startsWith("test"))
          //{
          //  System.err.println("NAME: "+name+"\n"+configuration.getAttributes());
          //}

          if (dirty) {
            ILaunchConfigurationWorkingCopy copy = configuration.getWorkingCopy();

            //copy.setAttribute(CBLaunchConfigurationConstants.ATTR_CB_PORT, 8335);

            copy.setAttribute("org.eclipse.jdt.launching.CLASSPATH_PROVIDER", "org.eclipse.ant.ui.AntClasspathProvider");
            copy.setAttribute("org.eclipse.jdt.launching.DEFAULT_CLASSPATH", true);

            CBRunUtil.injectBeesHome(copy);

            ILaunchConfiguration newconf = copy.doSave();

          }

        }
      }
    } catch (CoreException e) {
      e.printStackTrace();
    }

  }

  /*
   * (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(BundleContext bundleContext) throws Exception {
    CBProjectProcessService.getInstance().terminateAllProcesses();
    getPoller().halt();
    CBRunCoreActivator.context = null;
    plugin = null;
  }

  public static void logError(Exception e) {
    IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, e.getMessage());
    plugin.getLog().log(status);
  }

  public static ApplicationPoller getPoller() {
    return poller;
  }

}
