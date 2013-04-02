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
package com.cloudbees.eclipse.run.core.launchconfiguration;

import com.cloudbees.eclipse.run.core.CBRunCoreActivator;

public interface CBLaunchConfigurationConstants {

  String ID_CB_LAUNCH = "com.cloudbees.eclipse.run.core.launchconfiguration.launchConfigurationType";
  String ID_CB_DEPLOY_LAUNCH = "com.cloudbees.eclipse.run.core.launchcloudconfiguration.launchConfigurationType";
  String ATTR_CB_PROJECT_NAME = CBRunCoreActivator.PLUGIN_ID + ".projectName";
  String ATTR_CB_WST_FLAG = CBRunCoreActivator.PLUGIN_ID + ".wstFlag";
  String ATTR_CB_LAUNCH_BROWSER = CBRunCoreActivator.PLUGIN_ID + ".browser";
  String ATTR_CB_LAUNCH_CUSTOM_ID = CBRunCoreActivator.PLUGIN_ID + ".customId";
  String ATTR_CB_LAUNCH_WAR_PATH = CBRunCoreActivator.PLUGIN_ID + ".warPath";
  String ATTR_CB_PORT = CBRunCoreActivator.PLUGIN_ID + ".port";
  String ATTR_CB_DEBUG_PORT = CBRunCoreActivator.PLUGIN_ID + ".debugPort";
  String ATTR_CB_LOCAL_LAUNCH = CBRunCoreActivator.PLUGIN_ID + ".localLaunch";
  
  public static final String COM_CLOUDBEES_ECLIPSE_WST = "com.cloudbees.eclipse.wst";
  public static final String DO_NOTHING = "DO_NOTHING";

}
