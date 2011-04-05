package com.cloudbees.eclipse.run.core.launchconfiguration;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.internal.Server;

public class CBProjectRunnerService {

  private final List<CBProjectRunner> runners;
  private final CBProjectRunnerCreationStrategy defaultCreationStrategy;

  private static CBProjectRunnerService instance;

  public static CBProjectRunnerService getInstance() {
    if (instance == null) {
      instance = new CBProjectRunnerService();
    }
    return instance;
  }

  private CBProjectRunnerService() {
    runners = new ArrayList<CBProjectRunner>();
    defaultCreationStrategy = new CBProjectAntRunnerCreationStrategy();
  }

  public boolean containsRunningProject(IProject project) {
    CBProjectRunner runner = getProjectRunner(project);
    return runner != null && runner.isRunning();
  }

  /**
   * Currently launching projects via ant. Ant needs separate JRE instances to separate projects in parallel. At the
   * moment this support is not implemented. This method will be erased when multiple JRE launching is supported. 
   * 
   * @return false if any project is already running, true if no projects is running
   */
  @Deprecated
  private boolean isStartAllowed() {
    for (CBProjectRunner runner : runners) {
      if (runner.isRunning()) {
        return false;
      }
    }
    return true;
  }

  public void start(IProject project) throws Exception {
    start(project, defaultCreationStrategy);
  }

  public void start(IProject project, CBProjectRunnerCreationStrategy strategy) throws Exception {
    if (containsRunningProject(project)) {
      String msg = MessageFormat.format("Cannot launch ''{0}'' because it is already running.", project.getName());
      throw new IllegalStateException(msg);
    }

    if (!isStartAllowed()) {
      String msg = "Cannot launch another project. Please stop the currently running project at localhost to launch this project.";
      throw new IllegalStateException(msg);
    }

    LaunchHooksManager.invokePreStartHooks(project.getName());

    CBProjectRunner runner = strategy.createRunner(project);
    runners.add(runner);
    runner.start();

    getServer(project.getName()).setServerState(IServer.STATE_STARTED);
  }

  public boolean stop(IProject project) {
    if (project == null || !containsRunningProject(project)) {
      return false;
    }

    LaunchHooksManager.invokePreStopHooks();
    getServer(project.getName()).setServerState(IServer.STATE_STOPPED);

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

  private Server getServer(String projectName) {
    Server foundServer = null;
    IServer[] servers = ServerCore.getServers();

    for (IServer server : servers) {

      if (!(server instanceof Server)) {
        continue;
      }

      boolean isLocalServer = "com.cloudbees.eclipse.core.runcloud.local".equals(server.getServerType().getId());
      if (!isLocalServer) {
        continue;
      }

      String nameAttribute = server.getAttribute(CBLaunchConfigurationConstants.PROJECT, "");
      if (projectName.equals(nameAttribute)) {
        foundServer = (Server) server;
        break;
      }
    }

    return foundServer;
  }
}
