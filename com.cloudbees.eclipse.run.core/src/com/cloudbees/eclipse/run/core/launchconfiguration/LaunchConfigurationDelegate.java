package com.cloudbees.eclipse.run.core.launchconfiguration;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;

public class LaunchConfigurationDelegate implements ILaunchConfigurationDelegate {

  public LaunchConfigurationDelegate() {
  }

  public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
      throws CoreException {
    System.out.println("LAUNCH CONF DELEGATE");
  }

}
