package com.cloudbees.eclipse.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;

import com.cloudbees.eclipse.run.core.BeesSDK;
import com.cloudbees.eclipse.run.core.CBRunCoreActivator;
import com.cloudbees.eclipse.run.core.launchconfiguration.CBLaunchConfigurationConstants;

public class RunCloudBehaviourDelegate extends ServerBehaviourDelegate {

  public RunCloudBehaviourDelegate() {
    // TODO Auto-generated constructor stub
  }

  @Override
  public void stop(boolean force) {
    setServerState(IServer.STATE_STOPPED);
  }

  @Override
  public void startModule(IModule[] module, IProgressMonitor monitor) throws CoreException {
    super.startModule(module, monitor);
  }

  @Override
  public IStatus publish(int kind, IProgressMonitor monitor) {
    String projectName = getServer().getAttribute(CBLaunchConfigurationConstants.PROJECT, "");
    IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);

    try {
      new BeesSDK().deploy(project);
      setServerPublishState(IServer.PUBLISH_STATE_NONE);
      setServerState(IServer.STATE_STARTED);
      return null;
    } catch (Exception e) {
      return new Status(IStatus.ERROR, CBRunCoreActivator.PLUGIN_ID, e.getMessage(), e);
    }
  }
}
