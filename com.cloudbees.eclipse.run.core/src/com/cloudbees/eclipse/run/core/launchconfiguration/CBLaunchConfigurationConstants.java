package com.cloudbees.eclipse.run.core.launchconfiguration;

import com.cloudbees.eclipse.run.core.CBRunCoreActivator;

public interface CBLaunchConfigurationConstants {

  String ID_CB_LAUNCH = "com.cloudbees.eclipse.run.core.launchconfiguration.launchConfigurationType";
  String ID_CB_DEPLOY_LAUNCH = "com.cloudbees.eclipse.run.core.launchcloudconfiguration.launchConfigurationType";
  String ATTR_CB_PROJECT_NAME = CBRunCoreActivator.PLUGIN_ID + ".projectName";
  String PROJECT = "PROJECT";
  String ATTR_CB_LAUNCH_BROWSER = CBRunCoreActivator.PLUGIN_ID + ".browser";
  public static final String COM_CLOUDBEES_ECLIPSE_WST = "com.cloudbees.eclipse.wst";
  public static final String DO_NOTHING = "DO_NOTHING";

}
