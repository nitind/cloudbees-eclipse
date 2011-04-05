package com.cloudbees.eclipse.run.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;

import com.cloudbees.eclipse.core.CloudBeesNature;
import com.cloudbees.eclipse.run.core.CBRunCoreActivator;
import com.cloudbees.eclipse.run.core.launchconfiguration.CBLaunchConfigurationConstants;
import com.cloudbees.eclipse.sdk.CBSdkActivator;

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
      addDefaultAttributes(copy, projectName);

      launchConfiguration.add(copy.doSave());
    }

    return launchConfiguration;
  }

  /**
   * Adds attributes to this launch configuration working copy to enable launching as ant task on a separate JRE.
   * 
   * @param copy
   * @param projectName
   * @return
   * @throws CoreException
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static ILaunchConfigurationWorkingCopy addDefaultAttributes(ILaunchConfigurationWorkingCopy copy,
      String projectName) throws CoreException {

    copy.setAttribute(CBLaunchConfigurationConstants.ATTR_CB_PROJECT_NAME, projectName);

    IStringVariableManager variableManager = VariablesPlugin.getDefault().getStringVariableManager();
    String workspaceVarName = "workspace_loc";

    if (!projectName.isEmpty()) {
      IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(projectName + "/build.xml"));
      String location = variableManager.generateVariableExpression(workspaceVarName, file.getFullPath().toString());
      copy.setAttribute("org.eclipse.ui.externaltools.ATTR_LOCATION", location);
    }

    Map<String, String> map = copy.getAttribute("org.eclipse.ui.externaltools.ATTR_ANT_PROPERTIES", (Map) null);

    if (map == null) {
      map = new HashMap<String, String>();
    }

    map.put("bees.home", CBSdkActivator.getDefault().getBeesHome());
    copy.setAttribute("org.eclipse.ui.externaltools.ATTR_ANT_PROPERTIES", map);

    String directory = variableManager.generateVariableExpression(workspaceVarName, "/" + projectName);
    copy.setAttribute("org.eclipse.ui.externaltools.ATTR_WORKING_DIRECTORY", directory);

    copy.setAttribute("org.eclipse.jdt.launching.MAIN_TYPE", "org.eclipse.ant.internal.ui.antsupport.InternalAntRunner");

    copy.setAttribute("org.eclipse.ui.externaltools.ATTR_ANT_TARGETS", "run");

    copy.setAttribute("process_factory_id", "org.eclipse.ant.ui.remoteAntProcessFactory");

    return copy;
  }

}
