package com.cloudbees.eclipse.run.ui.launchconfiguration;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.cloudbees.eclipse.run.core.launchconfiguration.CBLaunchConfigurationConstants;
import com.cloudbees.eclipse.run.ui.CBRunUiActivator;
import com.cloudbees.eclipse.run.ui.Images;

public class CBCloudLaunchConfigurationTab extends AbstractLaunchConfigurationTab {

  private static final String TAB_NAME = "CloudBees Application";

  protected ProjectSelectionComposite projectSelector;
  protected Composite main;

  private Text customIdText;
  private Button customId;
  private AccountSelecionComposite accountSelector;

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

    configuration.setAttribute(CBLaunchConfigurationConstants.ATTR_CB_LAUNCH_ACCOUNT_ID,
        this.accountSelector.getAccountName());
  }

  @Override
  public void initializeFrom(ILaunchConfiguration configuration) {
    try {
      String projectName = configuration
          .getAttribute(CBLaunchConfigurationConstants.ATTR_CB_PROJECT_NAME, new String());
      if (projectName == null || projectName.length() == 0) {
        projectName = this.projectSelector.getDefaultSelection();
      }
      this.projectSelector.setText(projectName);

      String id = configuration.getAttribute(CBLaunchConfigurationConstants.ATTR_CB_LAUNCH_CUSTOM_ID, "");
      this.customId.setSelection(!"".equals(id));
      this.customIdText.setEnabled(!"".equals(id));
      this.customIdText.setText(id);

      String account = configuration.getAttribute(CBLaunchConfigurationConstants.ATTR_CB_LAUNCH_ACCOUNT_ID, "");
      this.accountSelector.setAccountName(account);

      updateLaunchConfigurationDialog();
    } catch (CoreException e) {
      CBRunUiActivator.logError(e);
    }
  }

  @Override
  public void createControl(Composite parent) {
    this.main = new Composite(parent, SWT.NONE);
    this.main.setLayout(new GridLayout(2, false));

    this.projectSelector = new ProjectSelectionComposite(this.main, SWT.None) {
      @Override
      public void handleUpdate() {
        validateConfigurationTab();
      }
    };
    this.projectSelector.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

    setControl(this.main);

    this.accountSelector = new AccountSelecionComposite(this.main) {
      @Override
      public void handleUpdate() {
        validateConfigurationTab();
      }
    };
    this.accountSelector.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

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

  protected boolean validateConfigurationTab() {
    IStatus projectStatus = this.projectSelector.validate();
    if (!projectStatus.isOK()) {
      setErrorMessage(projectStatus.getMessage());
      updateLaunchConfigurationDialog();
      return false;
    }

    setErrorMessage(null);
    setMessage("Run CloudBees application");
    updateLaunchConfigurationDialog();

    IStatus accountStatus = this.accountSelector.validate();
    if (!accountStatus.isOK()) {
      setErrorMessage(accountStatus.getMessage());
      updateLaunchConfigurationDialog();
      return false;
    }

    return true;
  }

  @Override
  public boolean isValid(ILaunchConfiguration launchConfig) {
    return this.projectSelector.validate().getSeverity() == IStatus.OK && this.accountSelector.validate().isOK();
  }

  @Override
  public String getName() {
    return TAB_NAME;
  }

  @Override
  public Image getImage() {
    return CBRunUiActivator.getDefault().getImageRegistry().get(Images.CLOUDBEES_ICON_16x16);
  }

  @Override
  public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
  }
}
