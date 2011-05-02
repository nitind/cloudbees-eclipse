package com.cloudbees.eclipse.run.ui.launchconfiguration;

import static com.cloudbees.eclipse.run.core.launchconfiguration.CBLaunchConfigurationConstants.ATTR_CB_PROJECT_NAME;

import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.internal.ServerWorkingCopy;

import com.cloudbees.api.ApplicationDeployArchiveResponse;
import com.cloudbees.eclipse.run.core.BeesSDK;
import com.cloudbees.eclipse.run.core.launchconfiguration.CBLaunchConfigurationConstants;
import com.cloudbees.eclipse.run.core.wst.WSTUtil;
import com.cloudbees.eclipse.run.ui.CBRunUiActivator;

public class CBCloudLaunchDelegate extends LaunchConfigurationDelegate {

  @SuppressWarnings("restriction")
  @Override
  public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
      throws CoreException {
    boolean needLaunch = configuration.getAttribute(CBLaunchConfigurationConstants.ATTR_CB_LAUNCH_BROWSER, true);

    monitor.beginTask("Deploying to RUN@cloud", 1);
    try {
      String projectName = configuration.getAttribute(ATTR_CB_PROJECT_NAME, "");

      for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
        if (project.getName().equals(projectName)) {
          String id = configuration.getAttribute(CBLaunchConfigurationConstants.ATTR_CB_LAUNCH_CUSTOM_ID, "");

          //FIXME move to extension
          ServerWorkingCopy server = (ServerWorkingCopy) WSTUtil.getServer(id, project);
          server.setServerState(IServer.STATE_STARTING);
          ILaunchConfigurationWorkingCopy wc = configuration.getWorkingCopy();
          wc.setAttribute("server-id", server.getId()); // HACK
          wc.doSave();

          ApplicationDeployArchiveResponse deploy;
          if ("".equals(id)) {
            deploy = BeesSDK.deploy(project, true);
          } else {
            deploy = BeesSDK.deploy(project, id, true);
          }

          //FIXME move to extension
          server.setServerPublishState(IServer.PUBLISH_STATE_NONE);
          server.setServerState(IServer.STATE_STARTED);
          monitor.done();

          if (needLaunch) {
            openBrowser(deploy);
          }
        }
      }
    } catch (Exception e) {
      Status status = new Status(IStatus.ERROR, CBRunUiActivator.PLUGIN_ID, e.getMessage());
      CBRunUiActivator.getDefault().getLog().log(status);
    }

  }

  private void openBrowser(ApplicationDeployArchiveResponse deploy) {
    final String url = deploy.getUrl();
    if (url != null) {
      Display.getDefault().asyncExec(new Runnable() {

        @Override
        public void run() {
          try {

            IWorkbenchBrowserSupport browserSupport = PlatformUI.getWorkbench().getBrowserSupport();
            browserSupport.getExternalBrowser().openURL(new URL(url));

          } catch (Exception e) {
            CBRunUiActivator.logError(e);
          }

        }
      });
    }
  }
}
