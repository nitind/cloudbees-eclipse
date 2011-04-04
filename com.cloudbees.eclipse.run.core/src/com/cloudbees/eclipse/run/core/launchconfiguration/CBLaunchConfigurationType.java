package com.cloudbees.eclipse.run.core.launchconfiguration;

import static com.cloudbees.eclipse.run.core.launchconfiguration.CBLaunchConfigurationConstants.ATTR_CB_PROJECT_NAME;

import java.text.MessageFormat;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;

import com.cloudbees.eclipse.run.core.BeesSDK;
import com.cloudbees.eclipse.run.core.CBRunCoreActivator;

public class CBLaunchConfigurationType implements ILaunchConfigurationDelegate {
  
  private static final String ERROR_MSG_PATTERN = "Failed to run ''{0}''.";

  public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
      throws CoreException {

    BeesSDK sdk = new BeesSDK();

    if (mode.equals(ILaunchManager.RUN_MODE)) {
      run(configuration, sdk, monitor);
      LaunchHooksManager.hook(configuration, mode, launch, monitor);
    } else if (mode.equals(ILaunchManager.DEBUG_MODE)) {
      debug();
    }
    
  }

  private void run(ILaunchConfiguration configuration, BeesSDK sdk, IProgressMonitor monitor) throws CoreException {
    String projectName = null;
    try {
      projectName = (String) configuration.getAttributes().get(ATTR_CB_PROJECT_NAME);
      IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
      CBLaunchedProjects.getInstance().start(project);
    } catch (Exception e) {
      String errorMsg = MessageFormat.format(ERROR_MSG_PATTERN, projectName);
      throw new CoreException(new Status(IStatus.ERROR, CBRunCoreActivator.PLUGIN_ID, errorMsg, e));
    } finally {
      monitor.done();
    }
  }
  
  private void debug() {
    // TODO
  }

}
