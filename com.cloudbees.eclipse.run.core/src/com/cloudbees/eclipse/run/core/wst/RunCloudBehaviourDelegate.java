package com.cloudbees.eclipse.run.core.wst;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;

import com.cloudbees.api.ApplicationStatusResponse;
import com.cloudbees.eclipse.run.core.BeesSDK;
import com.cloudbees.eclipse.run.core.CBRunCoreActivator;
import com.cloudbees.eclipse.run.core.launchconfiguration.CBLaunchConfigurationConstants;

public class RunCloudBehaviourDelegate extends ServerBehaviourDelegate {

  public RunCloudBehaviourDelegate() {
    // TODO Auto-generated constructor stub
  }

  @Override
  public void stop(boolean force) {
    IProject project = getProject();

    try {
      ApplicationStatusResponse stop = new BeesSDK().stop(project);
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
  public IStatus publish(int kind, IProgressMonitor monitor) {
    if (kind == IServer.PUBLISH_CLEAN || kind == IServer.PUBLISH_AUTO) {
      return null;
    }
    IProject project = getProject();

    try {
      new BeesSDK().deploy(project);
      setServerPublishState(IServer.PUBLISH_STATE_NONE);
      setServerState(IServer.STATE_STARTED);
      return null;
    } catch (Exception e) {
      CBRunCoreActivator.logError(e);
      return new Status(IStatus.ERROR, CBRunCoreActivator.PLUGIN_ID, e.getMessage(), e);
    }
  }

  private IProject getProject() {
    String projectName = getServer().getAttribute(CBLaunchConfigurationConstants.PROJECT, "");
    IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
    return project;
  }
}
