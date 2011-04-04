package com.cloudbees.eclipse.run.core.launchconfiguration;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;

import com.cloudbees.eclipse.run.core.CBRunCoreActivator;

public class LaunchHooksManager {

  public static void invokePreStartHooks(String projectName) {
    for(LaunchHook hook : getLaunchHooks()) {
      hook.preStartHook(projectName);
    }
  }
  
  public static void invokePreStopHooks() {
    for(LaunchHook hook : getLaunchHooks()) {
      hook.preStopHook();
    }
  }
  
  private static List<LaunchHook> getLaunchHooks() {
    List<LaunchHook> hooks = new ArrayList<LaunchHook>();
    
    IExtension[] extensions = Platform.getExtensionRegistry()
        .getExtensionPoint(CBRunCoreActivator.PLUGIN_ID, "launchHook").getExtensions();

    for (IExtension extension : extensions) {
      for (IConfigurationElement element : extension.getConfigurationElements()) {
        try {
          Object executableExtension = element.createExecutableExtension("defaultHandler");
          if (executableExtension instanceof LaunchHook) {
            hooks.add((LaunchHook) executableExtension);
          }
        } catch (CoreException e) {
          e.printStackTrace(); // FIXME
        }
      }
    }
    
    return hooks;
  }

}
