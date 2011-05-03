package com.cloudbees.eclipse.run.wst;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.model.ServerDelegate;

public class RunCloudServerDelegate extends ServerDelegate {

  /**
   * We actually don't need any of this, it's just for satisfying WST API.
   */
  public RunCloudServerDelegate() {
  }

  @Override
  public IStatus canModifyModules(IModule[] add, IModule[] remove) {
    return null;
  }

  @Override
  public IModule[] getChildModules(IModule[] module) {
    return null;
  }

  @Override
  public IModule[] getRootModules(IModule module) throws CoreException {
    return null;
  }

  @Override
  public void modifyModules(IModule[] add, IModule[] remove, IProgressMonitor monitor) throws CoreException {
  }

}
