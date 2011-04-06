package com.cloudbees.eclipse.run.core.wst;

import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.internal.Server;

import com.cloudbees.eclipse.run.core.launchconfiguration.CBLaunchConfigurationConstants;
import com.cloudbees.eclipse.run.core.launchconfiguration.CBProjectProcessLifecycleHook;

/**
 * Move this to WST plugin when created
 */
public class WSTProjectLifecycleHook implements CBProjectProcessLifecycleHook {

  public WSTProjectLifecycleHook() {
  }

  public void onStart(String projectName) {
    getServer(projectName).setServerState(IServer.STATE_STARTED);
  }

  public void onStop(String projectName) {
    getServer(projectName).setServerState(IServer.STATE_STOPPED);
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
