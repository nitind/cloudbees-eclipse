package com.cloudbees.eclipse.run.core.launchconfiguration;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;

import com.cloudbees.eclipse.run.core.CBRunCoreActivator;

public class LaunchHooksManager {

  public static void hook(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) {
    IExtension[] extensions = Platform.getExtensionRegistry()
        .getExtensionPoint(CBRunCoreActivator.PLUGIN_ID, "launchHook").getExtensions();
    
    for(IExtension extension : extensions) {
      for(IConfigurationElement element : extension.getConfigurationElements()) {
        try {
          Object executableExtension = element.createExecutableExtension("defaultHandler");
          if(executableExtension instanceof LaunchHook) {
            ((LaunchHook) executableExtension).handle(configuration, mode, launch, monitor);
          }
        } catch (CoreException e) {
          e.printStackTrace(); // FIXME
        }
      }
    }
    
  }

}
