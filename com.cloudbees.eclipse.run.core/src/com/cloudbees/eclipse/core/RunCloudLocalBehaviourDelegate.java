package com.cloudbees.eclipse.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;

import com.cloudbees.eclipse.run.core.launchconfiguration.CBLaunchConfigurationConstants;
import com.cloudbees.eclipse.run.core.launchconfiguration.CBProjectProcessService;
import com.cloudbees.eclipse.run.core.util.CBRunUtil;

public class RunCloudLocalBehaviourDelegate extends ServerBehaviourDelegate {

  public RunCloudLocalBehaviourDelegate() {
  }

  @Override
  public void stop(boolean force) {
    try {
      String projectName = getServer().getAttribute(CBLaunchConfigurationConstants.PROJECT, "");
      CBProjectProcessService.getInstance().terminateProcess(projectName);
    } catch (DebugException e) {
      e.printStackTrace(); //TODO better way to log
    }
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
    CBRunUtil.addDefaultAttributes(workingCopy, projectName);

  }
}
