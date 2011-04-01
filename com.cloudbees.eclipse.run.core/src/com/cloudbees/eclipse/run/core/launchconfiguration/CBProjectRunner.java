package com.cloudbees.eclipse.run.core.launchconfiguration;

import org.eclipse.core.resources.IProject;

interface CBProjectRunner {
  
  IProject getProject();
  
  void start();
  
  void stop();
  
  boolean isRunning();
  
}
