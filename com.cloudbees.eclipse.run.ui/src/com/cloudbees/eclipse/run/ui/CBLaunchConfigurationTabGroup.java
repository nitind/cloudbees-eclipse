package com.cloudbees.eclipse.run.ui;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

public class CBLaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup {

  public CBLaunchConfigurationTabGroup() {
  }

  public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
    // TODO
    ILaunchConfigurationTab[] tabs = { new CommonTab() };
    setTabs(tabs);
  }

}
