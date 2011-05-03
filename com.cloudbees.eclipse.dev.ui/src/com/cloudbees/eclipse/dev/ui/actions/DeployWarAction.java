package com.cloudbees.eclipse.dev.ui.actions;

import java.util.concurrent.CancellationException;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;

import com.cloudbees.eclipse.core.jenkins.api.JenkinsBuildDetailsResponse;
import com.cloudbees.eclipse.dev.ui.CBImages;
import com.cloudbees.eclipse.dev.ui.CloudBeesDevUiPlugin;
import com.cloudbees.eclipse.dev.ui.views.build.ArtifactsClickListener;

public class DeployWarAction extends Action {

  protected JenkinsBuildDetailsResponse build;

  public DeployWarAction() {
    super("Deploy war", Action.AS_PUSH_BUTTON | SWT.NO_FOCUS); //$NON-NLS-1$
    setToolTipText("Deploy war to specific RUN@cloud application"); //TODO i18n
    setImageDescriptor(CloudBeesDevUiPlugin.getImageDescription(CBImages.IMG_DEPLOY));
    super.setEnabled(false);
  }

  public void setBuild(final JenkinsBuildDetailsResponse build) {
    this.build = build;
    super.setEnabled(this.build != null);
  }

  @Override
  public void setEnabled(final boolean enable) {
    // ignore
  }

  @Override
  public boolean isEnabled() {
    return this.build != null;
  }

  @Override
  public void run() {
    try {
      if (this.build == null) {
        return;
      }

      ArtifactsClickListener.deployWar(this.build, null);
    } catch (CancellationException e) {
      // cancelled by user
    }
  }

}
