package com.cloudbees.eclipse.run.wst;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;

import com.cloudbees.eclipse.run.core.BeesSDK;
import com.cloudbees.eclipse.run.core.CBRunCoreActivator;
import com.cloudbees.eclipse.run.core.launchconfiguration.CBLaunchConfigurationConstants;

public class RunCloudBehaviourDelegate extends ServerBehaviourDelegate {

  public RunCloudBehaviourDelegate() {
  }

  @Override
  public void stop(boolean force) {
    try {
      String projectName = getServer().getAttribute(CBLaunchConfigurationConstants.ATTR_CB_LAUNCH_CUSTOM_ID, "");
      BeesSDK.stop(projectName);
    } catch (Exception e) {
      CBRunCoreActivator.logError(e);
    }
    setServerState(IServer.STATE_STOPPED);
  }

  @Override
  public void startModule(IModule[] module, IProgressMonitor monitor) throws CoreException {
    super.startModule(module, monitor);
  }

  @Override
  protected void initialize(IProgressMonitor monitor) {
    try {
      String projectName = getServer().getAttribute(CBLaunchConfigurationConstants.ATTR_CB_LAUNCH_CUSTOM_ID, "");

      setState(BeesSDK.getServerState(projectName).getStatus());
    } catch (Exception e) {
      CBRunCoreActivator.logError(e);
      setServerState(IServer.STATE_UNKNOWN);
    }
  }

  private void setState(String status) {
    if ("stopped".equals(status)) {
      setServerState(IServer.STATE_STOPPED);
    } else if ("active".equals(status) || "hibernate".equals(status)) {
      setServerState(IServer.STATE_STARTED);
    } else {
      setServerState(IServer.STATE_UNKNOWN);
    }
  }

  @Override
  public IStatus publish(int kind, IProgressMonitor monitor) {
    try {
      if (getServer().getServerState() != IServer.STATE_STARTED || kind == IServer.PUBLISH_CLEAN
          || kind == IServer.PUBLISH_AUTO) {
        return null;
      }

      String projectName = getServer().getAttribute(CBLaunchConfigurationConstants.PROJECT, "");
      String appId = getServer().getAttribute(CBLaunchConfigurationConstants.ATTR_CB_LAUNCH_CUSTOM_ID, "");
      IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);

      if ("".equals(appId)) {
        BeesSDK.deploy(project, true);
      } else {
        BeesSDK.deploy(project, appId, true);
      }
      setServerPublishState(IServer.PUBLISH_STATE_NONE);
      setServerState(IServer.STATE_STARTED);
      return null;
    } catch (Exception e) {
      return new Status(IStatus.ERROR, CBRunCoreActivator.PLUGIN_ID, e.getMessage(), e);
    }
  }

  @Override
  public void setupLaunchConfiguration(ILaunchConfigurationWorkingCopy workingCopy, IProgressMonitor monitor)
      throws CoreException {
    try {
      String projectName = getServer().getAttribute(CBLaunchConfigurationConstants.PROJECT, "");
      String appId = getServer().getAttribute(CBLaunchConfigurationConstants.ATTR_CB_LAUNCH_CUSTOM_ID, "");

      workingCopy.setAttribute(CBLaunchConfigurationConstants.ATTR_CB_LAUNCH_CUSTOM_ID, appId);
      workingCopy.setAttribute(CBLaunchConfigurationConstants.ATTR_CB_PROJECT_NAME, projectName);
      workingCopy.setAttribute(CBLaunchConfigurationConstants.ATTR_CB_WST_FLAG, true);

    } catch (Exception e) {
      CBRunCoreActivator.logError(e);
    }
    setServerState(IServer.STATE_STARTED);
  }
}
