package com.cloudbees.eclipse.run.core.launchconfiguration;

import com.cloudbees.eclipse.run.core.CBRunCoreActivator;

public interface CBLaunchConfigurationConstants {

  String ID_CB_LAUNCH = "com.cloudbees.eclipse.run.core.launchconfiguration.launchConfigurationType";
  String ATTR_CB_PROJECT_NAME = CBRunCoreActivator.PLUGIN_ID + ".projectName";
  String PROJECT = "PROJECT";
  String ATTR_CB_LAUNCH_BROWSER = "com.cloudbees.run.eclipse.launch.browser";
  public static final String COM_CLOUDBEES_ECLIPSE_WST = "com.cloudbees.eclipse.wst";
  public static final String DO_NOTHING = "DO_NOTHING";

}
