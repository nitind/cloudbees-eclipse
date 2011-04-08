package com.cloudbees.eclipse.run.ui.launchconfiguration;

import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.cloudbees.eclipse.run.core.launchconfiguration.CBLaunchConfigurationConstants;

public class CBCloudLaunchConfigurationTab extends CBLaunchConfigurationTab {

  private Button launchButton;

  @Override
  public void createControl(Composite parent) {
    super.createControl(parent);
    this.launchButton = new Button(this.main, SWT.CHECK);
    this.launchButton.setLayoutData(new GridData());
    this.launchButton.setSelection(true);
    Label label = new Label(this.main, SWT.NONE);
    label.setText("Open browser after launch.");
  }

  @Override
  public void performApply(ILaunchConfigurationWorkingCopy configuration) {
    super.performApply(configuration);
    configuration.setAttribute(CBLaunchConfigurationConstants.ATTR_CB_LAUNCH_BROWSER, this.launchButton.getSelection());
  }
}
