package com.cloudbees.eclipse.core.forge.api;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.jenkins.api.ChangeSetPathItem;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsScmConfig;

public interface ForgeSync {

  void sync(ForgeInstance instance, IProgressMonitor monitor) throws CloudBeesException;

  boolean openRemoteFile(JenkinsScmConfig scmConfig, ChangeSetPathItem item, IProgressMonitor monitor);

  void addToRepository(ForgeInstance instance, IProject project, IProgressMonitor monitor) throws CloudBeesException;

  boolean isUnderSvnScm(IProject project);

  ForgeInstance getMainRepo(IProject project);
}
