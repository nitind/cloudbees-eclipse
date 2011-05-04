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

  private final class ProjectSelectionCompositeForLauncher extends ProjectSelectionComposite {
    private ProjectSelectionCompositeForLauncher(Composite parent, int style) {
      super(parent, style);
    }

    @Override
    public void handleUpdate() {
      IStatus status = validate();
      if (status.getSeverity() == IStatus.OK) {
        setErrorMessage(null);
        setMessage("Run CloudBees application");
      } else {
        setErrorMessage(status.getMessage());
      }
      updateLaunchConfigurationDialog();
    }
  }

  private final class AccountSelectorCompositeImpl extends AbstractAccountSelectorComposite {

    public AccountSelectorCompositeImpl(Composite parent) {
      super(parent);
    }

    @Override
    public void handleUpdate() {
      IStatus status = validate();
      if (status.isOK()) {
        setErrorMessage(null);
        setMessage("Run CloudBees application");
      } else {
        setErrorMessage(status.getMessage());
      }
    }
  }

  private static final String TAB_NAME = "CloudBees Application";

  protected ProjectSelectionComposite projectSelector;
  protected AbstractAccountSelectorComposite accountSelector;
  protected Composite main;

  @Override
  public void createControl(Composite parent) {
    this.main = new Composite(parent, SWT.NONE);
    this.main.setLayout(new GridLayout(2, false));

    this.projectSelector = new ProjectSelectionCompositeForLauncher(this.main, SWT.None);
    this.projectSelector.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
    //    this.projectSelector.addModifyListener(new ModifyListener() {
    //
    //      @Override
    //      public void modifyText(ModifyEvent e) {
    //        CBLaunchConfigurationTab.this.projectSelector.handleUpdate();
    //        updateLaunchConfigurationDialog();
    //      }
    //
    //    });

    this.accountSelector = new AccountSelectorCompositeImpl(this.main);
    this.accountSelector.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

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
