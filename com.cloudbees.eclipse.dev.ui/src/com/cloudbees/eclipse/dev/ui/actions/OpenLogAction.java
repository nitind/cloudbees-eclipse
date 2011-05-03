package com.cloudbees.eclipse.dev.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;

import com.cloudbees.eclipse.core.jenkins.api.JenkinsBuild;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsBuildDetailsResponse;
import com.cloudbees.eclipse.dev.ui.CBImages;
import com.cloudbees.eclipse.dev.ui.CloudBeesDevUiPlugin;

/**
 * @author antons
 */
public class OpenLogAction extends Action {

  private Object build;

  public OpenLogAction() {
    super("Open console log", Action.AS_PUSH_BUTTON | SWT.NO_FOCUS); //TODO i18n
    setToolTipText("Open console log"); //TODO i18n
    setImageDescriptor(CloudBeesDevUiPlugin.getImageDescription(CBImages.IMG_CONSOLE));
    super.setEnabled(false);
  }

  @Override
  public void setEnabled(final boolean enable) {
    // ignore
  }

  public void setBuild(final Object build) {
    this.build = build;
    super.setEnabled(this.build != null);
  }

  @Override
  public boolean isEnabled() {
    return this.build != null;
  }

  @Override
  public void run() {
    if (this.build instanceof JenkinsBuild) {
      CloudBeesDevUiPlugin.getDefault().getJobConsoleManager()
          .showConsole(((JenkinsBuild) this.build).getDisplayName(), ((JenkinsBuild) this.build).url);
    } else if (this.build instanceof JenkinsBuildDetailsResponse) {
      CloudBeesDevUiPlugin.getDefault().getJobConsoleManager()
          .showConsole(((JenkinsBuildDetailsResponse) this.build).getDisplayName(),
              ((JenkinsBuildDetailsResponse) this.build).url);
    }
  }
}
