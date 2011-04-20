package com.cloudbees.eclipse.core.forge.api;

import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.gc.api.AccountServiceStatusResponse.AccountServices.ForgeService.Repo;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsBuildDetailsResponse.ChangeSet.ChangeSetItem;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsScmConfig;

public interface ForgeSync {

  enum TYPE {
    SVN, GIT, CVS
  };

  enum ACTION {
    CHECKED("Checked"), ADDED("Added"), CLONED("Cloned"), SKIPPED("Skipped"), CANCELLED("Cancelled");

    private final String label;

    private ACTION(final String label) {
      this.label = label;
    }

    public String getLabel() {
      return this.label;
    }
  };

  static class ChangeSetPathItem {
    public enum TYPE {
      ADDED, DELETED, MODIFIED
    };

    public TYPE type;
    public String path;
    public ChangeSetItem parent;

    public ChangeSetPathItem(final ChangeSetItem parent, final TYPE type, final String string) {
      this.type = type;
      this.path = string;
      this.parent = parent;
    }
  }

  ACTION sync(TYPE type, Properties props, IProgressMonitor monitor) throws CloudBeesException;

  boolean openRemoteFile(JenkinsScmConfig scmConfig, ChangeSetPathItem item, IProgressMonitor monitor);

  void addToRepository(TYPE type, Repo repo, IProject project, IProgressMonitor monitor) throws CloudBeesException;
}
