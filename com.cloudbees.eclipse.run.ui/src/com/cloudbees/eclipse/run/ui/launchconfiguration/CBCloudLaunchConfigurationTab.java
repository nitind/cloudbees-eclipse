package com.cloudbees.eclipse.run.ui.launchconfiguration;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.cloudbees.eclipse.run.core.launchconfiguration.CBLaunchConfigurationConstants;
import com.cloudbees.eclipse.run.ui.CBRunUiActivator;

public class CBCloudLaunchConfigurationTab extends CBLaunchConfigurationTab {

  private Text customIdText;
  private Button customId;

  @Override
  public void performApply(ILaunchConfigurationWorkingCopy configuration) {
    String projectName = this.projectSelector.getText();
    configuration.setAttribute(CBLaunchConfigurationConstants.ATTR_CB_PROJECT_NAME, projectName);

    if (this.customId != null) {
      if (this.customId.getSelection()) {
        configuration
            .setAttribute(CBLaunchConfigurationConstants.ATTR_CB_LAUNCH_CUSTOM_ID, this.customIdText.getText());
      } else {
        configuration.setAttribute(CBLaunchConfigurationConstants.ATTR_CB_LAUNCH_CUSTOM_ID, "");
      }
    }
  }

  @Override
  public void initializeFrom(ILaunchConfiguration configuration) {
    try {
      super.initializeFrom(configuration);

      String id = configuration.getAttribute(CBLaunchConfigurationConstants.ATTR_CB_LAUNCH_CUSTOM_ID, "");
      this.customId.setSelection(!"".equals(id));
      this.customIdText.setEnabled(!"".equals(id));
      this.customIdText.setText(id);
      updateLaunchConfigurationDialog();
    } catch (CoreException e) {
      CBRunUiActivator.logError(e);
    }
  }

  @Override
  public void createControl(Composite parent) {
    super.createControl(parent);

    this.customId = new Button(this.main, SWT.CHECK);
    this.customId.setSelection(false);
    this.customId.setText("Use Custom App ID");
    GridData gridData = new GridData();
    gridData.grabExcessHorizontalSpace = true;
    gridData.horizontalSpan = 2;
    this.customId.setLayoutData(gridData);

    final Label label = new Label(this.main, SWT.NONE);
    label.setEnabled(false);
    label.setText("App ID");
    this.customIdText = new Text(this.main, SWT.BORDER);
    this.customIdText.setText("");
    this.customIdText.setEnabled(false);
    this.customIdText.addModifyListener(new ModifyListener() {

      @Override
      public void modifyText(ModifyEvent e) {
        updateLaunchConfigurationDialog();
      }
    });
    gridData = new GridData();
    gridData.grabExcessHorizontalSpace = true;
    gridData.horizontalAlignment = SWT.FILL;
    this.customIdText.setLayoutData(gridData);

    this.customId.addSelectionListener(new SelectionListener() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        boolean selection = CBCloudLaunchConfigurationTab.this.customId.getSelection();
        label.setEnabled(selection);
        CBCloudLaunchConfigurationTab.this.customIdText.setEnabled(selection);
        updateLaunchConfigurationDialog();
      }

      @Override
      public void widgetDefaultSelected(SelectionEvent e) {

      }
    });
  }
}
