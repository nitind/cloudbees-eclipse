package com.cloudbees.eclipse.run.wst;

import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;

import com.cloudbees.eclipse.run.core.launchconfiguration.CBLaunchConfigurationConstants;

public class WSTUtil {

  public static IServerWorkingCopy getServer(String appId, IProject project) throws CoreException {
    IServer[] servers = ServerCore.getServers();

    for (IServer iServer : servers) {

      boolean nameEquals = iServer.getAttribute(CBLaunchConfigurationConstants.ATTR_CB_PROJECT_NAME, "").equals(
          project.getName());
      boolean idEquals = iServer.getAttribute(CBLaunchConfigurationConstants.ATTR_CB_LAUNCH_CUSTOM_ID, "")
          .equals(appId);

      if (nameEquals && idEquals) {
        return (IServerWorkingCopy) iServer;
      }
    }

    IServerWorkingCopy wc = createServer(appId, project,
        ServerCore.findServerType("com.cloudbees.eclipse.core.runcloud"));
    return wc;
  }

  private static IServerWorkingCopy createServer(String appId, IProject project, IServerType st) throws CoreException {
    IServerWorkingCopy wc = st.createServer(null, null, null);

    String idString = "".equals(appId) ? "" : " (with AppId:" + appId + ")";

    wc.setName(project.getName() + idString + " running at RUN@cloud");

    wc.setAttribute(CBLaunchConfigurationConstants.ATTR_CB_PROJECT_NAME, project.getName());
    wc.setAttribute(CBLaunchConfigurationConstants.ATTR_CB_LAUNCH_CUSTOM_ID, appId);
    wc.save(true, null);
    return wc;
  }

  public static IServerWorkingCopy getLocalServer(String appId, IProject project) throws CoreException {
    IServerType st = ServerCore.findServerType("com.cloudbees.eclipse.core.runcloud");
    IServerWorkingCopy wc = st.createServer(null, null, null);

    String idString = "".equals(appId) ? "" : " (with AppId:" + appId + ")";

    wc.setName(project.getName() + idString + " running at RUN@cloud");

    wc.setAttribute(CBLaunchConfigurationConstants.ATTR_CB_LAUNCH_CUSTOM_ID, appId);
    wc.save(true, null);

    return wc;
  }

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
