package com.cloudbees.eclipse.run.core.launchconfiguration;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.internal.Server;

public class CBLaunchedProjects {

  private final List<CBProjectRunner> runners;
  private final CBProjectRunnerCreationStrategy defaultCreationStrategy;

  private static CBLaunchedProjects instance;

  public static CBLaunchedProjects getInstance() {
    if (instance == null) {
      instance = new CBLaunchedProjects();
    }
    return instance;
  }

  private CBLaunchedProjects() {
    runners = new ArrayList<CBProjectRunner>();
    defaultCreationStrategy = new CBProjectAntRunnerCreationStrategy();
  }

  public boolean containsRunningProject(IProject project) {
    CBProjectRunner runner = getProjectRunner(project);
    return runner != null && runner.isRunning();
  }

  public void start(IProject project) throws Exception {
    start(project, defaultCreationStrategy);
  }

  public void start(IProject project, CBProjectRunnerCreationStrategy strategy) throws Exception {
    if (containsRunningProject(project)) {
      String msg = MessageFormat.format("Cannot launch ''{0}'' because it is already running.", project.getName());
      throw new IllegalStateException(msg);
    }

    LaunchHooksManager.invokePreStartHooks(project.getName());
    
    CBProjectRunner runner = strategy.createRunner(project);
    runners.add(runner);
    runner.start();
   
    getServer().setServerState(IServer.STATE_STARTED);
  }

  public boolean stop(IProject project) {
    if (project == null || !containsRunningProject(project)) {
      return false;
    }
    
    LaunchHooksManager.invokePreStopHooks();
    getServer().setServerState(IServer.STATE_STOPPED);
    
    CBProjectRunner runner = getProjectRunner(project);
    runner.stop();
    return runners.remove(runner);
  }

  private CBProjectRunner getProjectRunner(IProject project) {
    CBProjectRunner foundRunner = null;

    for (CBProjectRunner runner : runners) {
      if (runner.getProject().equals(project)) {
        foundRunner = runner;
        break;
      }
    }

    return foundRunner;
  }
  
  private Server getServer() {
    IServer[] servers = ServerCore.getServers();
    for (IServer iServer : servers) {
      if ("com.cloudbees.eclipse.core.runcloud.local".equals(iServer.getServerType().getId())) {
        return (Server) iServer;
      }
    }
    return null;
  }
}
