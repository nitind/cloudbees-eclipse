package com.cloudbees.eclipse.run.ui.launchconfiguration;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.cloudbees.eclipse.run.core.launchconfiguration.CBLaunchConfigurationConstants;
import com.cloudbees.eclipse.run.core.util.CBRunUtil;
import com.cloudbees.eclipse.run.ui.CBRunUiActivator;
import com.cloudbees.eclipse.run.ui.Images;

public class CBLaunchConfigurationTab extends AbstractLaunchConfigurationTab {

  private static final String TAB_NAME = "CloudBees Application";

  protected ProjectSelectionComposite projectSelector;
  protected Composite main;

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
  }

  @Override
  public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
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
    } catch (CoreException e) {
      CBRunUiActivator.logError(e);
    }
  }

  @Override
  public void performApply(ILaunchConfigurationWorkingCopy configuration) {
    String projectName = this.projectSelector.getText();
    try {
      CBRunUtil.addDefaultAttributes(configuration, projectName);
    } catch (CoreException e) {
      CBRunUiActivator.logError(e);
    }
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
    return true;
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
  public boolean isValid(ILaunchConfiguration launchConfig) {
    return this.projectSelector.validate().getSeverity() == IStatus.OK;
  }

}
