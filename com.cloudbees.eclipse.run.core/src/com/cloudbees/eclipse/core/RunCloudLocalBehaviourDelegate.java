package com.cloudbees.eclipse.core;

import static com.cloudbees.eclipse.run.core.launchconfiguration.CBLaunchConfigurationConstants.ATTR_CB_PROJECT_NAME;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;

import com.cloudbees.eclipse.run.core.launchconfiguration.CBLaunchConfigurationConstants;
import com.cloudbees.eclipse.run.core.launchconfiguration.CBLaunchedProjects;

public class RunCloudLocalBehaviourDelegate extends ServerBehaviourDelegate {

  public RunCloudLocalBehaviourDelegate() {
  }

  @Override
  public void stop(boolean force) {
    String projectName = getServer().getAttribute(CBLaunchConfigurationConstants.PROJECT, "");
    IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
    CBLaunchedProjects.getInstance().stop(project);
    setServerState(IServer.STATE_STOPPED);
  }

  @Override
  public void startModule(IModule[] module, IProgressMonitor monitor) throws CoreException {
    super.startModule(module, monitor);
  }

  @Override
  public IStatus publish(int kind, IProgressMonitor monitor) {
    return null;
    //    return super.publish(kind, monitor);
  }

  @Override
  public void setupLaunchConfiguration(ILaunchConfigurationWorkingCopy workingCopy, IProgressMonitor monitor)
      throws CoreException {

    String projectName = getServer().getAttribute(CBLaunchConfigurationConstants.PROJECT, "");
    workingCopy.setAttribute(ATTR_CB_PROJECT_NAME, projectName);
    setServerState(IServer.STATE_STARTED);

  }
}
