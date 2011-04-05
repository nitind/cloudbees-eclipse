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
    if(service == null) {
      service = new CBProjectProcessService();
    }
    return service;
  }
  
  public void addProcess(String name, IProcess process) {
    processMap.put(name, process);
    invokeStartHooks(name);
  }
  
  public void terminateProcess(String projectName) throws DebugException {
    IProcess process = processMap.get(projectName);
    if(process != null && process.canTerminate()) {
      invokeStopHooks(projectName);
      process.terminate();
    }
  }
  
  public void terminateAllProcesses() throws DebugException {
    for(IProcess process : processMap.values()) {
      process.terminate();
    }
  }
  
  public void invokeStartHooks(String projectName) {
    for(CBProjectProcessLifecycleHook hook : getLaunchHooks()) {
      hook.onStart(projectName);
    }
  }
  
  public void invokeStopHooks(String projectName) {
    for(CBProjectProcessLifecycleHook hook : getLaunchHooks()) {
      hook.onStop(projectName);
    }
  }
  
  private List<CBProjectProcessLifecycleHook> getLaunchHooks() {
    List<CBProjectProcessLifecycleHook> hooks = new ArrayList<CBProjectProcessLifecycleHook>();
    
    IExtension[] extensions = Platform.getExtensionRegistry()
        .getExtensionPoint(CBRunCoreActivator.PLUGIN_ID, "launchHook").getExtensions();

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
