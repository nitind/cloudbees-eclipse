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
package com.cloudbees.eclipse.run.core.launchconfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IProcess;

import com.cloudbees.eclipse.run.core.CBRunCoreActivator;

public class CBProjectProcessService {

  private static CBProjectProcessService service;

  private final Map<String, IProcess> processMap;

  public CBProjectProcessService() {
    this.processMap = new HashMap<String, IProcess>();
  }

  public static CBProjectProcessService getInstance() {
    if (service == null) {
      service = new CBProjectProcessService();
    }
    return service;
  }

  public void addProcess(String name, IProcess process) {
    this.processMap.put(name, process);
    invokeStartHooks(name);
  }

  /**
   * Terminate ant process related to the given project name
   * 
   * @param projectName
   * @throws DebugException
   */
  public void terminateProcess(String projectName) throws DebugException {
    IProcess process = this.processMap.get(projectName);
    if (process != null && process.canTerminate()) {
      invokeStopHooks(projectName);
      process.terminate();
    }
    this.processMap.remove(projectName);
  }

  /**
   * If the process was terminated by system, then remove this process from the map.
   * 
   * @param projectName
   */
  public void removeProcess(String projectName) {
    if (this.processMap.containsKey(projectName)) {
      this.processMap.remove(projectName);
      invokeStopHooks(projectName);
    }
  }

  public void terminateAllProcesses() throws DebugException {
    for (IProcess process : this.processMap.values()) {
      process.terminate();
    }
  }

  public void invokeStartHooks(String projectName) {
    for (CBProjectProcessLifecycleHook hook : getLaunchHooks()) {
      hook.onStart(projectName);
    }
  }

  public void invokeStopHooks(String projectName) {
    for (CBProjectProcessLifecycleHook hook : getLaunchHooks()) {
      hook.onStop(projectName);
    }
  }

  private List<CBProjectProcessLifecycleHook> getLaunchHooks() {
    List<CBProjectProcessLifecycleHook> hooks = new ArrayList<CBProjectProcessLifecycleHook>();

    IExtension[] extensions = Platform.getExtensionRegistry()
        .getExtensionPoint(CBRunCoreActivator.PLUGIN_ID, "processLifecycleHook").getExtensions();

    for (IExtension extension : extensions) {
      for (IConfigurationElement element : extension.getConfigurationElements()) {
        try {
          Object executableExtension = element.createExecutableExtension("defaultHandler");
          if (executableExtension instanceof CBProjectProcessLifecycleHook) {
            hooks.add((CBProjectProcessLifecycleHook) executableExtension);
          }
        } catch (CoreException e) {
          e.printStackTrace(); // FIXME
        }
      }
    }

    return hooks;
  }

}
