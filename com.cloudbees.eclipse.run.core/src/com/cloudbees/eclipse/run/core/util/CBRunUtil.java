package com.cloudbees.eclipse.run.core.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;

import com.cloudbees.eclipse.core.CloudBeesNature;
import com.cloudbees.eclipse.run.core.CBRunCoreActivator;
import com.cloudbees.eclipse.run.core.launchconfiguration.CBLaunchConfigurationConstants;

public class CBRunUtil {

  /**
   * @return projects in workbench that have {@link CloudBeesNature}
   */
  public static List<IProject> getWorkbenchCloudBeesProjects() {
    List<IProject> projects = new ArrayList<IProject>();
    for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
      if (CloudBeesNature.isEnabledFor(project)) {
        projects.add(project);
      }
    }
    return projects;
  }

  /**
   * @param projectName
   * @return list of launch configurations associated with this project name
   * @throws CoreException
   */
  public static List<ILaunchConfiguration> getLaunchConfigurations(String projectName) throws CoreException {
    ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
    List<ILaunchConfiguration> launchConfigurations = new ArrayList<ILaunchConfiguration>();

    try {
      for (ILaunchConfiguration configuration : launchManager.getLaunchConfigurations()) {
        String name = configuration.getAttribute(CBLaunchConfigurationConstants.ATTR_CB_PROJECT_NAME, new String());
        if (name.equals(projectName)) {
          launchConfigurations.add(configuration);
        }
      }
    } catch (Exception e) {
      throw new CoreException(new Status(IStatus.ERROR, CBRunCoreActivator.PLUGIN_ID,
          "Error retrieving launch configurations", e));
    }

    return launchConfigurations;
  }

  /**
   * @param projectName
   * @return list of launch configurations associated with this project name, guaranteed to return at least 1
   *         configuration
   * @throws CoreException
   */
  public static List<ILaunchConfiguration> getOrCreateCloudBeesLaunchConfigurations(String projectName)
      throws CoreException {
    List<ILaunchConfiguration> launchConfiguration = getLaunchConfigurations(projectName);

    if (launchConfiguration.isEmpty()) {
      ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
      
      ILaunchConfigurationType configType = launchManager
          .getLaunchConfigurationType(CBLaunchConfigurationConstants.ID_CB_LAUNCH);
      String name = launchManager.generateLaunchConfigurationName(projectName);

      ILaunchConfigurationWorkingCopy copy = configType.newInstance(null, name);
      copy.setAttribute(CBLaunchConfigurationConstants.ATTR_CB_PROJECT_NAME, projectName);
      launchConfiguration.add(copy.doSave());
    }

    return launchConfiguration;
  }
}
