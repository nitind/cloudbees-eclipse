package com.cloudbees.eclipse.run.wst;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;

import com.cloudbees.eclipse.run.core.launchconfiguration.CBLaunchConfigurationConstants;

public class WSTUtil {

  public static IServerWorkingCopy getServer(String appId, IProject project) throws CoreException {
    IServerType st = ServerCore.findServerType("com.cloudbees.eclipse.core.runcloud");
    IServerWorkingCopy wc = st.createServer(null, null, null);

    String idString = "".equals(appId) ? "" : " (with AppId:" + appId + ")";

    wc.setName(project.getName() + idString + " running at RUN@cloud");

    wc.setAttribute(CBLaunchConfigurationConstants.PROJECT, project.getName());
    wc.setAttribute(CBLaunchConfigurationConstants.ATTR_CB_LAUNCH_CUSTOM_ID, appId);
    wc.save(true, null);

    return wc;
  }

}
