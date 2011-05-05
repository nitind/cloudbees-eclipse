package com.cloudbees.eclipse.run.ui.launchconfiguration;

import static com.cloudbees.eclipse.run.core.launchconfiguration.CBLaunchConfigurationConstants.ATTR_CB_PROJECT_NAME;

import java.io.FileNotFoundException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.run.core.BeesSDK;
import com.cloudbees.eclipse.run.core.launchconfiguration.CBLaunchConfigurationConstants;
import com.cloudbees.eclipse.run.ui.CBRunUiActivator;

public class CBCloudLaunchDelegate extends LaunchConfigurationDelegate {

  @SuppressWarnings("restriction")
  @Override
  public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
      throws CoreException {

    monitor.beginTask("Deploying to RUN@cloud", 1);
    try {
      String projectName = configuration.getAttribute(ATTR_CB_PROJECT_NAME, "");

      for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
        if (project.getName().equals(projectName)) {
          String account = configuration.getAttribute(CBLaunchConfigurationConstants.ATTR_CB_LAUNCH_ACCOUNT_ID, "");
          String appId = configuration.getAttribute(CBLaunchConfigurationConstants.ATTR_CB_LAUNCH_CUSTOM_ID, "");

          if (configuration.getAttribute(CBLaunchConfigurationConstants.ATTR_CB_WST_FLAG, false)) {

            start(project, account, appId);
            removeWstFlag(configuration);

          } else {
            deploy(project, account, appId);
          }

          handleExtensions(configuration, project);
          monitor.done();

        }
      }
    } catch (Exception e) {
      CBRunUiActivator.logErrorAndShowDialog(e);
    }

  }

  private void deploy(IProject project, String account, String id) throws Exception, CloudBeesException, CoreException,
      FileNotFoundException {
    String appId = "".equals(id) ? project.getName() : id;
    BeesSDK.deploy(project, account, appId, true);
  }

  private void start(IProject project, String account, String appId) throws Exception, CloudBeesException {
    BeesSDK.start(account, appId.equals("") ? project.getName() : appId);
  }

  private ILaunchConfigurationWorkingCopy removeWstFlag(ILaunchConfiguration configuration) throws CoreException {
    ILaunchConfigurationWorkingCopy workingCopy = configuration.getWorkingCopy();
    workingCopy.setAttribute(CBLaunchConfigurationConstants.ATTR_CB_WST_FLAG, false);
    workingCopy.doSave();
    return workingCopy;
  }

  private IExtension[] handleExtensions(ILaunchConfiguration configuration, IProject project) {
    IExtension[] extensions = Platform.getExtensionRegistry()
        .getExtensionPoint(CBRunUiActivator.PLUGIN_ID, "launchDelegateAditions").getExtensions();

    for (IExtension extension : extensions) {
      for (IConfigurationElement element : extension.getConfigurationElements()) {
        try {
          Object executableExtension = element.createExecutableExtension("actions");
          if (executableExtension instanceof ILaunchExtraAction) {
            ((ILaunchExtraAction) executableExtension).action(configuration, project.getName(), false);
          }
        } catch (CoreException e) {
          CBRunUiActivator.logError(e);
        }
      }
    }
    return extensions;
  }
}
