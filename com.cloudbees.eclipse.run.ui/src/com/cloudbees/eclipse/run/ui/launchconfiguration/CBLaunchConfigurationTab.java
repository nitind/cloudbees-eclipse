package com.cloudbees.eclipse.run.ui.launchconfiguration;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
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
    }
  }

  private static final String TAB_NAME = "CloudBees Application";

  private ProjectSelectionComposite content;

  @Override
  public void createControl(Composite parent) {
    this.content = new ProjectSelectionCompositeForLauncher(parent, SWT.None);

    setControl(this.content);
    this.content.addModifyListener(new ModifyListener() {

      @Override
      public void modifyText(ModifyEvent e) {
        CBLaunchConfigurationTab.this.content.handleUpdate();
        updateLaunchConfigurationDialog();
      }

    });

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
        projectName = this.content.getDefaultSelection();
      }
      this.content.setText(projectName);
    } catch (CoreException e) {
      CBRunUiActivator.logError(e);
    }
  }

  @Override
  public void performApply(ILaunchConfigurationWorkingCopy configuration) {
    String projectName = this.content.getText();
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
    return this.content.validate().getSeverity() == IStatus.OK;
  }

}
