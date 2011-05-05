package com.cloudbees.eclipse.run.wst;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;

import com.cloudbees.eclipse.run.core.launchconfiguration.CBLaunchConfigurationConstants;

public class WSTUtil {

  private final static IServerType cloud = ServerCore.findServerType("com.cloudbees.eclipse.core.runcloud");
  private final static IServerType local = ServerCore.findServerType("com.cloudbees.eclipse.core.runcloud.local");

  public static IServerWorkingCopy getServer(String appId, String projectName) throws CoreException {
    return findOrCreateServer(appId, projectName, cloud);
  }

  public static IServerWorkingCopy getLocalServer(String appId, String projectName) throws CoreException {
    return findOrCreateServer(appId, projectName, local);
  }

  private static IServerWorkingCopy findOrCreateServer(String appId, String projectName, IServerType st)
      throws CoreException {
    IServer[] servers = ServerCore.getServers();

    for (IServer iServer : servers) {

      boolean nameEquals = iServer.getAttribute(CBLaunchConfigurationConstants.ATTR_CB_PROJECT_NAME, "").equals(
          projectName);
      boolean idEquals = iServer.getAttribute(CBLaunchConfigurationConstants.ATTR_CB_LAUNCH_CUSTOM_ID, "")
          .equals(appId);

      boolean serverTypeCorrect = iServer.getServerType().equals(st);

      if (nameEquals && idEquals && serverTypeCorrect) {
        return iServer.createWorkingCopy();
      }
    }

    IServerWorkingCopy wc = createServer(appId, projectName, st);
    return wc;
  }

  private static IServerWorkingCopy createServer(String appId, String projectName, IServerType st) throws CoreException {
    IServerWorkingCopy wc = st.createServer(null, null, null);

    String idString = "".equals(appId) ? "" : " (with AppId:" + appId + ")";

    String sufix;
    if (cloud.equals(st)) {
      sufix = " running at RUN@cloud";
    } else {
      sufix = " running at localhost";
    }

    wc.setName(projectName + idString + sufix);

    wc.setAttribute(CBLaunchConfigurationConstants.ATTR_CB_PROJECT_NAME, projectName);
    wc.setAttribute(CBLaunchConfigurationConstants.ATTR_CB_LAUNCH_CUSTOM_ID, appId);
    wc.save(true, null);
    return wc;
  }

  // TODO change account match
  public static ArrayList<IServerWorkingCopy> getServers(String id) {
    ArrayList<IServerWorkingCopy> r = new ArrayList<IServerWorkingCopy>();
    IServer[] servers = ServerCore.getServers();

    for (IServer iServer : servers) {
      boolean idEquals = iServer.getAttribute(CBLaunchConfigurationConstants.ATTR_CB_LAUNCH_CUSTOM_ID, "").equals(
          id.split("/")[1]);
      if (idEquals) {
        r.add(iServer.createWorkingCopy());
      }
    }

    return r;
  }

}
