package com.cloudbees.eclipse.run.core.launchconfiguration;

import org.eclipse.core.resources.IProject;

public class CBProjectAntRunnerCreationStrategy implements CBProjectRunnerCreationStrategy {

  public CBProjectRunner createRunner(IProject project) throws Exception {
    return new CBProjectAntRunner(project);
  }

}
