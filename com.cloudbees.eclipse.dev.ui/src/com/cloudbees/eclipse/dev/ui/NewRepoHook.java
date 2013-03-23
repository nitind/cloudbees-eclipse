package com.cloudbees.eclipse.dev.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.gc.api.ClickStartCreateResponse;
import com.cloudbees.eclipse.run.core.NewClickStartProjectHook;

public class NewRepoHook implements NewClickStartProjectHook {

  @Override
  public void hookProject(ClickStartCreateResponse resp, IProject project, IProgressMonitor monitor)
      throws CloudBeesException {
      CloudBeesDevUiPlugin.getDefault().reloadForgeRepos(false, false);
  }

}
