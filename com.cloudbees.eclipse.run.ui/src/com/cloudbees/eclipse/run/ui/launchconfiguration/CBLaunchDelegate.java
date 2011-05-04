package com.cloudbees.eclipse.run.ui.launchconfiguration;

import static com.cloudbees.eclipse.run.core.launchconfiguration.CBLaunchConfigurationConstants.ATTR_CB_PROJECT_NAME;
import static com.cloudbees.eclipse.run.core.launchconfiguration.CBLaunchConfigurationConstants.DO_NOTHING;

import java.net.URL;

import org.eclipse.ant.internal.launching.launchConfigurations.AntLaunchDelegate;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

import com.cloudbees.eclipse.run.core.launchconfiguration.CBLaunchConfigurationConstants;
import com.cloudbees.eclipse.run.core.launchconfiguration.CBProjectProcessService;
import com.cloudbees.eclipse.run.core.util.CBRunUtil;
import com.cloudbees.eclipse.run.ui.CBRunUiActivator;

@SuppressWarnings("restriction")
public class CBLaunchDelegate extends AntLaunchDelegate {

  @Override
  public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
      throws CoreException {

    if (configuration.getAttribute(DO_NOTHING, false)) {
      monitor.setCanceled(true);
      return;
    }

    boolean debug = mode.equals("debug");

    ILaunchConfiguration launchConf = modifyLaunch(configuration, mode);
    super.launch(launchConf, mode, launch, monitor);

    String projectName = configuration.getAttribute(ATTR_CB_PROJECT_NAME, "");
    CBProjectProcessService.getInstance().addProcess(projectName, launch.getProcesses()[0]);
    DebugPlugin.getDefault().getLaunchManager().addLaunchListener(new TerminateListener(projectName));

    if (debug) {
      CBRunUtil.createTemporaryRemoteLaunchConfiguration(projectName).launch(mode, monitor);
    }

    if (configuration.getAttribute(CBLaunchConfigurationConstants.ATTR_CB_LAUNCH_BROWSER, true)) {
      openBrowser("http://localhost:8335");
    }

  }

  private ILaunchConfiguration modifyLaunch(ILaunchConfiguration configuration, String mode) throws CoreException {
    ILaunchConfigurationWorkingCopy copy = configuration.copy(configuration.getName());
    if (mode.equals("run")) {
      copy.removeAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS);
    } else if (mode.equals("debug")) {
      String vmargs = "-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8002";
      copy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, vmargs);
    }
    return copy;
  }

  private void openBrowser(final String url) {

    if (url != null) {
      Display.getDefault().asyncExec(new Runnable() {

        @Override
        public void run() {
          try {

            IWorkbenchBrowserSupport browserSupport = PlatformUI.getWorkbench().getBrowserSupport();
            browserSupport.getExternalBrowser().openURL(new URL(url));

          } catch (Exception e) {
            CBRunUiActivator.logError(e);
          }

        }
      });
    }
  }

  private class TerminateListener implements ILaunchesListener2 {

    private final String projectName;

    public TerminateListener(String projectName) {
      this.projectName = projectName;
    }

    @Override
    public void launchesRemoved(ILaunch[] launches) {
    }

    @Override
    public void launchesAdded(ILaunch[] launches) {
    }

    @Override
    public void launchesChanged(ILaunch[] launches) {
    }

    @Override
    public void launchesTerminated(ILaunch[] launches) {
      CBProjectProcessService service = CBProjectProcessService.getInstance();
      service.removeProcess(this.projectName);
    }
  }
}
