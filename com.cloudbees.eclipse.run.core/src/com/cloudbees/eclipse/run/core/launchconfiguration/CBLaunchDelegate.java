package com.cloudbees.eclipse.run.core.launchconfiguration;

import static com.cloudbees.eclipse.run.core.launchconfiguration.CBLaunchConfigurationConstants.ATTR_CB_PROJECT_NAME;

import org.eclipse.ant.internal.launching.launchConfigurations.AntLaunchDelegate;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;

import com.cloudbees.eclipse.run.core.util.CBRunUtil;

@SuppressWarnings("restriction")
public class CBLaunchDelegate extends AntLaunchDelegate {

  @Override
  public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
      throws CoreException {

    boolean debug = mode.equals("debug");

    ILaunchConfiguration launchConf = debug ? addDebugAttributes(configuration) : configuration;
    super.launch(launchConf, mode, launch, monitor);

    String projectName = configuration.getAttribute(ATTR_CB_PROJECT_NAME, "");
    CBProjectProcessService.getInstance().addProcess(projectName, launch.getProcesses()[0]);
    DebugPlugin.getDefault().getLaunchManager().addLaunchListener(new TerminateListener(projectName));

    if (debug) {
      CBRunUtil.createTemporaryRemoteLaunchConfiguration(projectName).launch(mode, monitor);
    }
  }

  private ILaunchConfiguration addDebugAttributes(ILaunchConfiguration configuration) throws CoreException {
    ILaunchConfigurationWorkingCopy copy = configuration.copy(configuration.getName());
    String vmargs = "-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8002";
    copy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, vmargs);
    return copy;
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
