package com.cloudbees.eclipse.run.wst;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;

import com.cloudbees.eclipse.run.core.CBRunCoreActivator;
import com.cloudbees.eclipse.run.core.launchconfiguration.CBLaunchConfigurationConstants;
import com.cloudbees.eclipse.run.core.launchconfiguration.CBProjectProcessService;
import com.cloudbees.eclipse.run.core.util.CBRunUtil;

public class RunCloudLocalBehaviourDelegate extends ServerBehaviourDelegate {

  public RunCloudLocalBehaviourDelegate() {
  }

  @Override
  public void stop(boolean force) {
    try {
      String projectName = getServer().getAttribute(CBLaunchConfigurationConstants.ATTR_CB_PROJECT_NAME, "");
      CBProjectProcessService.getInstance().terminateProcess(projectName);
    } catch (DebugException e) {
      CBRunCoreActivator.logError(e);
    }
  }

  @Override
  public void startModule(IModule[] module, IProgressMonitor monitor) throws CoreException {
    super.startModule(module, monitor);
  }

  @Override
  public IStatus publish(int kind, IProgressMonitor monitor) {
    return null;
  }

  @Override
  public void setupLaunchConfiguration(ILaunchConfigurationWorkingCopy workingCopy, IProgressMonitor monitor)
      throws CoreException {

    String projectName = getServer().getAttribute(CBLaunchConfigurationConstants.ATTR_CB_PROJECT_NAME, "");
    CBRunUtil.addDefaultAttributes(workingCopy, projectName);

  }
}
