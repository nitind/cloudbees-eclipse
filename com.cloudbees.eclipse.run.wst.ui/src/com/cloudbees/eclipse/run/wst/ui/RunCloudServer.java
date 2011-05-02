package com.cloudbees.eclipse.run.wst.ui;

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
public class RunCloudServer extends WizardFragment {

  private ProjectSelectionComposite composite;
  private IWizardHandle wizard;

  public RunCloudServer() {
    setComplete(false);
  }

  @Override
  public Composite createComposite(Composite parent, IWizardHandle wizard) {
    this.wizard = wizard;
    wizard.setTitle("CloudBees Application");
    wizard
        .setDescription("CloudBees servers are application (project) specific. Select a project for cerating a server configuration.");
    this.composite = new ProjectSelectionComposite(parent, SWT.NONE) {

      @Override
      public void handleUpdate() {
        boolean complete = validate().getSeverity() == IStatus.OK;
        updateServerName();

        if (!complete) {
          RunCloudServer.this.wizard.setMessage("Select a CloudBees project!", IStatus.ERROR);
        } else {
          RunCloudServer.this.wizard.setMessage(null, IStatus.OK);
        }
        setComplete(complete);
        RunCloudServer.this.wizard.update();
      }

      private void updateServerName() {
        ServerWorkingCopy server = (ServerWorkingCopy) getTaskModel().getObject("server");
        server.setName(getText() + " running at RUN@cloud");
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
