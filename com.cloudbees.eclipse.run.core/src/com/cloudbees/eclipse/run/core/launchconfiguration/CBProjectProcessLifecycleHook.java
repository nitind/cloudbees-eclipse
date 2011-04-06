package com.cloudbees.eclipse.run.core.launchconfiguration;


public interface CBProjectProcessLifecycleHook {
  
  void onStart(String projectName);
  
  void onStop(String projectName);
  
}
