package com.cloudbees.eclipse.run.core.launchconfiguration;


public interface LaunchHook {
  
  void preStartHook(String projectName);
  
  void preStopHook();
  
}
