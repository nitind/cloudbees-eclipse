package com.cloudbees.eclipse.run.wst;

import java.util.ArrayList;

import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.internal.ServerWorkingCopy;

import com.cloudbees.api.ApplicationInfo;
import com.cloudbees.api.ApplicationListResponse;
import com.cloudbees.eclipse.run.core.IStatusUpdater;

@SuppressWarnings("restriction")
public class WSTUpdater implements IStatusUpdater {

  public WSTUpdater() {
  }

  @Override
  public void update(String id, String status, ApplicationInfo info) {
    ArrayList<IServerWorkingCopy> servers = WSTUtil.getServers(id);
    for (IServerWorkingCopy iServerWorkingCopy : servers) {
      if ("stopped".equals(status)) {
        ((ServerWorkingCopy) iServerWorkingCopy).setServerState(IServer.STATE_STOPPED);
      } else if ("active".equals(status) || "hibernate".equals(status)) {
        ((ServerWorkingCopy) iServerWorkingCopy).setServerState(IServer.STATE_STARTED);
      } else {
        ((ServerWorkingCopy) iServerWorkingCopy).setServerState(IServer.STATE_UNKNOWN);
      }
    }
  }

  @Override
  public void update(ApplicationListResponse response) {
  }

}
