package com.cloudbees.eclipse.run.core.launchconfiguration;

import org.eclipse.core.resources.IProject;

public interface CBProjectRunnerCreationStrategy {
  
  CBProjectRunner createRunner(IProject project) throws Exception;
  
}
