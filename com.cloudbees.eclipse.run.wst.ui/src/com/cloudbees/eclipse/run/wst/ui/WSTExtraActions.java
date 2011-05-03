package com.cloudbees.eclipse.run.wst.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.internal.ServerWorkingCopy;

import com.cloudbees.eclipse.run.core.launchconfiguration.CBLaunchConfigurationConstants;
import com.cloudbees.eclipse.run.ui.launchconfiguration.ILaunchExtraAction;
import com.cloudbees.eclipse.run.wst.WSTUtil;

public class WSTExtraActions implements ILaunchExtraAction {

  @SuppressWarnings("restriction")
  @Override
  public void action(ILaunchConfiguration configuration, IProject project) throws CoreException {
    String id = configuration.getAttribute(CBLaunchConfigurationConstants.ATTR_CB_LAUNCH_CUSTOM_ID, "");

    ServerWorkingCopy server = (ServerWorkingCopy) WSTUtil.getServer(id, project);
    server.setServerState(IServer.STATE_STARTING);
    ILaunchConfigurationWorkingCopy wc = configuration.getWorkingCopy();
    wc.setAttribute("server-id", server.getId()); // HACK
    wc.doSave();
    server.setServerPublishState(IServer.PUBLISH_STATE_NONE);
    server.setServerState(IServer.STATE_STARTED);
  }

}
