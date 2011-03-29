package com.cloudbees.eclipse.run.ui;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

public class CBLaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup {

  public CBLaunchConfigurationTabGroup() {
  }

  public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
    ILaunchConfigurationTab[] tabs = { new CBLaunchConfigurationTab() };
    setTabs(tabs);
  }
  
}
