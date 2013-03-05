package com.cloudbees.eclipse.run.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.gc.api.ClickStartCreateResponse;

public interface NewClickStartProjectHook {

  void hookProject(ClickStartCreateResponse resp, IProject project, IProgressMonitor monitor) throws CloudBeesException;
  
}
