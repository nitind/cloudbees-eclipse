package com.cloudbees.eclipse.run.core;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

import com.cloudbees.eclipse.core.CloudBeesException;

public interface WarBuilderHook {

  IFile buildProject(IProject project, IProgressMonitor monitor) throws CloudBeesException;
  
}
