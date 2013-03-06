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
package com.cloudbees.eclipse.run.sdk;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.Bundle;
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
    getBeesHome();
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

  /**
   * TODO consider renaming to setBeesHome or initBeesHome and split getBeesHome into a separate method. 
   * @return
   */
  public String getBeesHome() {
    if (this.sdkLocation == null) {
      Bundle bundle = plugin.getBundle();
      try {
        //URL entry = bundle.getEntry("/cloudbees-sdk");
        //this.sdkLocation = FileLocator.toFileURL(entry).getPath();
        URL url = new URL("platform:/plugin/com.cloudbees.eclipse.run.sdk/cloudbees-sdk");
        this.sdkLocation = FileLocator.toFileURL(url).getPath();
        System.setProperty("bees.home", sdkLocation);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return this.sdkLocation;
  }
  
}
