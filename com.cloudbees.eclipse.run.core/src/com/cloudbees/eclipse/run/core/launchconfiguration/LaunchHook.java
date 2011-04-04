package com.cloudbees.eclipse.run.core.launchconfiguration;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;

public interface LaunchHook {
  
  void handle(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor);
  
}
