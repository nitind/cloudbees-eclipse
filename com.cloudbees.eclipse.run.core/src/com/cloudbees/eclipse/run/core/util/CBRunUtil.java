/*******************************************************************************
 * Copyright (c) 2013 Cloud Bees, Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *              http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
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
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;

import com.cloudbees.eclipse.core.CloudBeesNature;
import com.cloudbees.eclipse.run.core.CBRunCoreActivator;
import com.cloudbees.eclipse.run.core.launchconfiguration.CBLaunchConfigurationConstants;
import com.cloudbees.eclipse.run.sdk.CBSdkActivator;

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
   * @param cloud
   * @return list of launch configurations associated with this project name
   * @throws CoreException
   */
  public static List<ILaunchConfiguration> getLaunchConfigurations(String projectName, boolean cloud)
      throws CoreException {
    ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
    List<ILaunchConfiguration> launchConfigurations = new ArrayList<ILaunchConfiguration>();

    try {
      for (ILaunchConfiguration configuration : launchManager.getLaunchConfigurations()) {

        String name = configuration.getAttribute(CBLaunchConfigurationConstants.ATTR_CB_PROJECT_NAME, "");

        Map antProps = configuration.getAttribute("org.eclipse.ui.externaltools.ATTR_ANT_PROPERTIES", (Map) null);
        if (antProps != null) {
          // Overwrite bees.home with the latest directory location to support bees home dir updates.
          //antProps.put("bees.home", CBSdkActivator.getDefault().getBeesHome()+"asf");
        }

        if (name.equals(projectName)) {
          boolean cloudLaunch = "".equals(configuration.getAttribute("org.eclipse.jdt.launching.MAIN_TYPE", ""));

          if (cloudLaunch && cloud) {
            launchConfigurations.add(configuration);
          } else if (!cloudLaunch && !cloud) {
            launchConfigurations.add(configuration);
          }
        }
      }
    } catch (Exception e) {
      throw new CoreException(new Status(IStatus.ERROR, CBRunCoreActivator.PLUGIN_ID,
          "Error retrieving launch configurations", e));
    }

    return launchConfigurations;
  }

  public static List<ILaunchConfiguration> getOrCreateCloudBeesLaunchConfigurations(String projectName, boolean cloud,
      String port) throws CoreException {
    if (!cloud) {
      return getOrCreateCloudBeesLaunchConfigurations(projectName, port);
    }

    List<ILaunchConfiguration> launchConfiguration = getLaunchConfigurations(projectName, cloud);
    if (launchConfiguration.isEmpty()) {
      ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();

      ILaunchConfigurationType configType = launchManager
          .getLaunchConfigurationType(CBLaunchConfigurationConstants.ID_CB_DEPLOY_LAUNCH);

      ILaunchConfigurationWorkingCopy copy = configType.newInstance(null, projectName);
      copy.setAttribute(CBLaunchConfigurationConstants.ATTR_CB_PROJECT_NAME, projectName);
      copy.setAttribute(CBLaunchConfigurationConstants.ATTR_CB_LAUNCH_BROWSER, true);
      launchConfiguration.add(copy.doSave());
    }

    return launchConfiguration;
  }

  /**
   * @param projectName
   * @param port
   * @return list of launch configurations associated with this project name, guaranteed to return at least 1
   *         configuration
   * @throws CoreException
   */
  public static List<ILaunchConfiguration> getOrCreateCloudBeesLaunchConfigurations(String projectName, String port)
      throws CoreException {
    List<ILaunchConfiguration> launchConfiguration = getLaunchConfigurations(projectName, false);

    if (launchConfiguration.isEmpty()) {
      ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();

      ILaunchConfigurationType configType = launchManager
          .getLaunchConfigurationType(CBLaunchConfigurationConstants.ID_CB_LAUNCH);
      String name = projectName + " (local)";

      ILaunchConfigurationWorkingCopy copy = configType.newInstance(null, name);
      addDefaultAttributes(copy, projectName, port);

      launchConfiguration.add(copy.doSave());
    }

    return launchConfiguration;
  }

  /**
   * Adds attributes to this launch configuration working copy to enable launching as ant task on a separate JRE.
   * 
   * @param conf
   * @param projectName
   * @return
   * @throws CoreException
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static ILaunchConfigurationWorkingCopy addDefaultAttributes(ILaunchConfigurationWorkingCopy conf,
      String projectName, String port) throws CoreException {

    conf.setAttribute(CBLaunchConfigurationConstants.ATTR_CB_PROJECT_NAME, projectName);

    IStringVariableManager variableManager = VariablesPlugin.getDefault().getStringVariableManager();
    String workspaceVarName = "workspace_loc";

    if (!projectName.isEmpty()) {
      IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(projectName + "/build.xml"));
      String location = variableManager.generateVariableExpression(workspaceVarName, file.getFullPath().toString());
      conf.setAttribute("org.eclipse.ui.externaltools.ATTR_LOCATION", location);
    }

    Map<String, String> map = conf.getAttribute("org.eclipse.ui.externaltools.ATTR_ANT_PROPERTIES", (Map) null);

    if (map == null) {
      map = new HashMap<String, String>();
      conf.setAttribute("org.eclipse.ui.externaltools.ATTR_ANT_PROPERTIES", map);      
    }

    injectBeesHome(conf);

    if (port == null) {
      map.remove("run.port");
    } else {
      map.put("run.port", port);
    }
    conf.setAttribute(CBLaunchConfigurationConstants.ATTR_CB_PORT, port);
    

    String directory = variableManager.generateVariableExpression(workspaceVarName, "/" + projectName);
    conf.setAttribute("org.eclipse.ui.externaltools.ATTR_WORKING_DIRECTORY", directory);

    conf.setAttribute("org.eclipse.jdt.launching.MAIN_TYPE", "org.eclipse.ant.internal.ui.antsupport.InternalAntRunner");

    conf.setAttribute("org.eclipse.ui.externaltools.ATTR_ANT_TARGETS", "run");

    conf.setAttribute("process_factory_id", "org.eclipse.ant.ui.remoteAntProcessFactory");
    
    conf.setAttribute("org.eclipse.jdt.launching.CLASSPATH_PROVIDER", "org.eclipse.ant.ui.AntClasspathProvider");
    conf.setAttribute("org.eclipse.jdt.launching.DEFAULT_CLASSPATH", true);

    return conf;
  }

  public static ILaunchConfiguration createTemporaryRemoteLaunchConfiguration(String projectName) throws CoreException {
    ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
    ILaunchConfigurationType configType = launchManager
        .getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_REMOTE_JAVA_APPLICATION);
    String name = launchManager.generateLaunchConfigurationName(projectName + " remote debugger");
    ILaunchConfigurationWorkingCopy copy = configType.newInstance(null, name);

    List<String> resourcePaths = new ArrayList<String>();
    resourcePaths.add("/" + projectName);
    copy.setAttribute("org.eclipse.debug.core.MAPPED_RESOURCE_PATHS", resourcePaths);

    List<String> resourceTypes = new ArrayList<String>();
    resourceTypes.add("4");
    copy.setAttribute("org.eclipse.debug.core.MAPPED_RESOURCE_TYPES", resourceTypes);

    String connectMapAttr = IJavaLaunchConfigurationConstants.ATTR_CONNECT_MAP;
    Map<String, String> connectMap = new HashMap<String, String>();
    connectMap.put("hostname", "localhost");
    connectMap.put("port", "8002");
    copy.setAttribute(connectMapAttr, connectMap);

    copy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_ALLOW_TERMINATE, true);
    copy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, projectName);
    copy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_CONNECTOR,
        IJavaLaunchConfigurationConstants.ID_SOCKET_ATTACH_VM_CONNECTOR);

    return copy;
  }

  public static void injectBeesHome(ILaunchConfigurationWorkingCopy copy) throws CoreException {
    // cb launch conf
    Map antProps = copy.getAttribute("org.eclipse.ui.externaltools.ATTR_ANT_PROPERTIES", (Map) null);
    if (antProps==null) {
      antProps = new HashMap();
      copy.setAttribute("org.eclipse.ui.externaltools.ATTR_ANT_PROPERTIES", antProps);
    }            
    // Overwrite bees.home with the latest directory location to support bees home dir updates.
    antProps.put("bees.home", CBSdkActivator.getDefault().getBeesHome());           

  }

}
