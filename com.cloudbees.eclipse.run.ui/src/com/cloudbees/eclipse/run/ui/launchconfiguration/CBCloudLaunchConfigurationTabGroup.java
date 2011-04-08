package com.cloudbees.eclipse.run.ui.launchconfiguration;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

public class CBCloudLaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup {

  public CBCloudLaunchConfigurationTabGroup() {
  }

  @Override
  public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
    ILaunchConfigurationTab[] tabs = { new CBCloudLaunchConfigurationTab() };
    setTabs(tabs);
  }
}
