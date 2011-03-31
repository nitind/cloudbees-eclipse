package com.cloudbees.eclipse.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;

public class RunCloudBehaviourDelegate extends ServerBehaviourDelegate {

  public RunCloudBehaviourDelegate() {
    // TODO Auto-generated constructor stub
  }

  @Override
  public void stop(boolean force) {
    System.out.println("stopped " + force);
  }

  @Override
  public IStatus publish(int kind, IProgressMonitor monitor) {

    return super.publish(kind, monitor);
  }

}
