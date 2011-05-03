package com.cloudbees.eclipse.run.ui.launchconfiguration;

import static com.cloudbees.eclipse.run.core.launchconfiguration.CBLaunchConfigurationConstants.ATTR_CB_PROJECT_NAME;

import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

import com.cloudbees.api.ApplicationDeployArchiveResponse;
import com.cloudbees.eclipse.run.core.BeesSDK;
import com.cloudbees.eclipse.run.core.launchconfiguration.CBLaunchConfigurationConstants;
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

          ApplicationDeployArchiveResponse deploy;
          if ("".equals(id)) {
            deploy = BeesSDK.deploy(project, true);
          } else {
            deploy = BeesSDK.deploy(project, id, true);
          }
          IExtension[] extensions = Platform.getExtensionRegistry()
              .getExtensionPoint(CBRunUiActivator.PLUGIN_ID, "launchDelegateAditions").getExtensions();

          for (IExtension extension : extensions) {
            for (IConfigurationElement element : extension.getConfigurationElements()) {
              try {
                Object executableExtension = element.createExecutableExtension("actions");
                if (executableExtension instanceof ILaunchExtraAction) {
                  ((ILaunchExtraAction) executableExtension).action(configuration, project);
                }
              } catch (CoreException e) {
                CBRunUiActivator.logError(e);
              }
            }
          }
          monitor.done();

          if (needLaunch) {
            openBrowser(deploy);
          }
        }
      }
    } catch (Exception e) {
      CBRunUiActivator.logError(e);
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
