package com.cloudbees.eclipse.run.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;

public class CBLaunchConfigurationType implements ILaunchConfigurationDelegate {

  public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
      throws CoreException {
    
    if(mode.equals(ILaunchManager.RUN_MODE)) {
      //TODO
    } else if (mode.equals(ILaunchManager.DEBUG_MODE)) {
    //TODO
    }
    
  }

}