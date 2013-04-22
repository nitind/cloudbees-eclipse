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
package com.cloudbees.eclipse.dev.scm.egit;

import org.eclipse.jsch.core.IJSchService;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.cloudbees.eclipse.core.Logger;

public class CloudBeesScmEgitPlugin extends AbstractUIPlugin {

  public static final String PLUGIN_ID = "com.cloudbees.eclipse.dev.scm.egit"; //$NON-NLS-1$
  
  private static CloudBeesScmEgitPlugin plugin;
  private ServiceTracker tracker;
  private Logger logger;
  
  /*
   * (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(final BundleContext bundleContext) throws Exception {
    super.start(bundleContext);
    plugin = this;
    this.logger = new Logger(getLog());
    tracker = new ServiceTracker(
        getBundle().getBundleContext(),
        IJSchService.class.getName(),
         null);
     tracker.open();
     
  }

  /*
   * (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(final BundleContext bundleContext) throws Exception {
    plugin = null;
    super.stop(bundleContext);
    tracker.close();
    logger = null;
  }

  public IJSchService getJSchService() {
    return (IJSchService)tracker.getService();
  }
  
  public static CloudBeesScmEgitPlugin getDefault() {
    return plugin;
  }
  
  public Logger getLogger() {
    return this.logger;
  }

}
