package com.cloudbees.eclipse.run.ui.wizards;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wst.server.core.internal.ServerWorkingCopy;
import org.eclipse.wst.server.ui.wizard.IWizardHandle;
import org.eclipse.wst.server.ui.wizard.WizardFragment;

import com.cloudbees.eclipse.run.core.launchconfiguration.CBLaunchConfigurationConstants;
import com.cloudbees.eclipse.run.ui.launchconfiguration.ProjectSelectionComposite;

@SuppressWarnings("restriction")
public class RunCloudLocal extends WizardFragment {

  private ProjectSelectionComposite composite;
  private IWizardHandle wizard;

  public RunCloudLocal() {
    setComplete(false);
  }

  @Override
  public Composite createComposite(Composite parent, IWizardHandle wizard) {
    this.wizard = wizard;
    wizard.setTitle("CloudBees Project");
    wizard
        .setDescription("Cloudbees servers are project specific. Select a project for cerating a server configuration.");
    this.composite = new ProjectSelectionComposite(parent, SWT.NONE) {

      @Override
      public void handleUpdate() {
        boolean complete = validate().getSeverity() == IStatus.OK;
        updateServerName();

        if (!complete) {
          RunCloudLocal.this.wizard.setMessage("Select a Cloudbees project!", IStatus.ERROR);
        } else {
          RunCloudLocal.this.wizard.setMessage(null, IStatus.OK);
        }
        setComplete(complete);
        RunCloudLocal.this.wizard.update();
      }

      private void updateServerName() {
        ServerWorkingCopy server = (ServerWorkingCopy) getTaskModel().getObject("server");
        server.setName(getText() + " running at localhost");
        server.setAttribute(CBLaunchConfigurationConstants.PROJECT, getText());
      }
    };
    this.composite.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GREEN));
    return this.composite;
  }

  @Override
  public boolean isComplete() {
    return super.isComplete() && this.composite.validate().getSeverity() == IStatus.OK;
  }

  @Override
  public boolean hasComposite() {
    return true;
  }
}
