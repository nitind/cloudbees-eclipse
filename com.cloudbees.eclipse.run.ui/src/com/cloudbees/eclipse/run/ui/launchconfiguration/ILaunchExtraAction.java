package com.cloudbees.eclipse.run.ui.launchconfiguration;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;

public interface ILaunchExtraAction {
  public void action(ILaunchConfiguration configuration, String projectName, boolean local) throws CoreException;

}
