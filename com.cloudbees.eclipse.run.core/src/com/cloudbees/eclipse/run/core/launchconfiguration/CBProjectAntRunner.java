package com.cloudbees.eclipse.run.core.launchconfiguration;

import org.eclipse.ant.core.AntRunner;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import com.cloudbees.eclipse.run.core.BeesSDK;

public class CBProjectAntRunner implements CBProjectRunner, Runnable {
  
  private final IProject project;
  private final BeesSDK sdk;
  private final AntRunner antRunner;
  private boolean running;
  
  public CBProjectAntRunner(IProject project) throws Exception {
    this.sdk = new BeesSDK();
    this.project = project;
    this.antRunner = sdk.getRunLocallyTask(project);
  }
  
  public IProject getProject() {
    return this.project;
  }
  
  public void start() {
    running = true;
    Thread ant = new Thread(this);
    ant.start();
  }

  public void stop() {
    antRunner.stop();
    running = false;
  }

  public boolean isRunning() {
    return running;
  }

  public void run() {
    try {
      antRunner.run();
    } catch (CoreException e) {
      e.printStackTrace();
    }
  }

}
