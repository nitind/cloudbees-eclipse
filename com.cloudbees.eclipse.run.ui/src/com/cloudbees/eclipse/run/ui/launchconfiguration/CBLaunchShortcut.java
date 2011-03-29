package com.cloudbees.eclipse.run.ui.launchconfiguration;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;

import com.cloudbees.eclipse.run.core.launchconfiguration.CBLaunchConfigurationConstants;
import com.cloudbees.eclipse.run.ui.CBRunUiActivator;

public class CBLaunchShortcut implements ILaunchShortcut {

  public void launch(ISelection selection, String mode) {
    if (selection instanceof IStructuredSelection) {
      
      IStructuredSelection structuredSelection = (IStructuredSelection) selection;
      IProject project = (IProject) structuredSelection.getFirstElement();
      
      try {
        ILaunchConfiguration launchConfiguration = getOrCreateLaunchConfiguration(project);
        DebugUITools.launch(launchConfiguration, mode);
      } catch (CoreException e) {
        CBRunUiActivator.logError(e);
      }
      
    }
  }

  public void launch(IEditorPart editor, String mode) {
    // not currently supported
  }

  private ILaunchConfiguration getOrCreateLaunchConfiguration(IProject project) throws CoreException {
    ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
    ILaunchConfiguration launchConfiguration = null;

    for (ILaunchConfiguration configuration : launchManager.getLaunchConfigurations()) {
      String projectName = configuration.getAttribute(CBLaunchConfigurationConstants.ATTR_CB_PROJECT_NAME, new String());
      if(projectName.equals(project.getName())) {
        launchConfiguration = configuration;
        break;
      }
    }
    
    if(launchConfiguration == null) {
      launchConfiguration = createConfiguration(project, launchManager);
    }
    
    return launchConfiguration;
  }

  private ILaunchConfiguration createConfiguration(IProject project, ILaunchManager launchManager) throws CoreException {

    ILaunchConfigurationType configType = launchManager
        .getLaunchConfigurationType(CBLaunchConfigurationConstants.ID_CB_LAUNCH);
    String name = launchManager.generateLaunchConfigurationName(project.getName());

    ILaunchConfigurationWorkingCopy copy = configType.newInstance(null, name);
    copy.setAttribute(CBLaunchConfigurationConstants.ATTR_CB_PROJECT_NAME, project.getName());
    ILaunchConfiguration config = copy.doSave();

    return config;
  }

}
