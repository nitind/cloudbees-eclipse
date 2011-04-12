package com.cloudbees.eclipse.run.ui.launchconfiguration;

import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.widgets.Button;

import com.cloudbees.eclipse.run.core.launchconfiguration.CBLaunchConfigurationConstants;

public class CBCloudLaunchConfigurationTab extends CBLaunchConfigurationTab {

  private Button launchButton;

  @Override
  public void performApply(ILaunchConfigurationWorkingCopy configuration) {
    //    super.performApply(configuration);
    String projectName = this.content.getText();
    configuration.setAttribute(CBLaunchConfigurationConstants.ATTR_CB_PROJECT_NAME, projectName);

    if (this.launchButton != null) {
      configuration.setAttribute(CBLaunchConfigurationConstants.ATTR_CB_LAUNCH_BROWSER,
          this.launchButton.getSelection());
    }
  }
}
