package com.cloudbees.eclipse.run.core.launchconfiguration;

import org.eclipse.ant.internal.launching.launchConfigurations.AntLaunchDelegate;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchesListener2;

@SuppressWarnings("restriction")
public class CBLaunchDelegate extends AntLaunchDelegate {

  @Override
  public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
      throws CoreException {
    super.launch(configuration, mode, launch, monitor);
    String projectName = configuration.getAttribute(CBLaunchConfigurationConstants.ATTR_CB_PROJECT_NAME, "");
    CBProjectProcessService.getInstance().addProcess(projectName, launch.getProcesses()[0]);
    DebugPlugin.getDefault().getLaunchManager().addLaunchListener(new TerminateListener(projectName));
  }
    
  private class TerminateListener implements ILaunchesListener2 {
    
    private final String projectName;
    
    public TerminateListener(String projectName) {
      this.projectName = projectName;
    }
    
    public void launchesRemoved(ILaunch[] launches) {
    }

    public void launchesAdded(ILaunch[] launches) {
    }

    public void launchesChanged(ILaunch[] launches) {
    }

    public void launchesTerminated(ILaunch[] launches) {
      CBProjectProcessService service = CBProjectProcessService.getInstance();
      service.removeProcess(this.projectName);
    }
  }
}
