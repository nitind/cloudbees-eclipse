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
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;

import com.cloudbees.eclipse.run.core.launchconfiguration.CBLaunchConfigurationConstants;
import com.cloudbees.eclipse.run.ui.CBRunUiActivator;

public class CBLaunchShortcut implements ILaunchShortcut {

  public void launch(ISelection selection, String mode) {
    if (selection instanceof IStructuredSelection) {
      
      IStructuredSelection structuredSelection = (IStructuredSelection) selection;
      
      String name = null;
      Object element = structuredSelection.getFirstElement();
      
      if(element instanceof IProject) {
        name = ((IProject) element).getName();
      } else if (element instanceof IJavaProject) {
        name = ((IJavaProject) element).getProject().getName();
      }
      
      try {
        ILaunchConfiguration launchConfiguration = getOrCreateLaunchConfiguration(name);
        DebugUITools.launch(launchConfiguration, mode);
      } catch (CoreException e) {
        CBRunUiActivator.logError(e);
      }
      
    }
  }

  public void launch(IEditorPart editor, String mode) {
    // not currently supported
  }

  private ILaunchConfiguration getOrCreateLaunchConfiguration(String name) throws CoreException {
    ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
    ILaunchConfiguration launchConfiguration = null;

    for (ILaunchConfiguration configuration : launchManager.getLaunchConfigurations()) {
      String projectName = configuration.getAttribute(CBLaunchConfigurationConstants.ATTR_CB_PROJECT_NAME, new String());
      if(projectName.equals(name)) {
        launchConfiguration = configuration;
        break;
      }
    }
    
    if(launchConfiguration == null) {
      launchConfiguration = createConfiguration(name, launchManager);
    }
    
    return launchConfiguration;
  }

  private ILaunchConfiguration createConfiguration(String projectName, ILaunchManager launchManager) throws CoreException {

    ILaunchConfigurationType configType = launchManager
        .getLaunchConfigurationType(CBLaunchConfigurationConstants.ID_CB_LAUNCH);
    String name = launchManager.generateLaunchConfigurationName(projectName);

    ILaunchConfigurationWorkingCopy copy = configType.newInstance(null, name);
    copy.setAttribute(CBLaunchConfigurationConstants.ATTR_CB_PROJECT_NAME, projectName);
    ILaunchConfiguration config = copy.doSave();

    return config;
  }

}
