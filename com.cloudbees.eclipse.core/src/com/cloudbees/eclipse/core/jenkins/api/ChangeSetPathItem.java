package com.cloudbees.eclipse.core.jenkins.api;

import com.cloudbees.eclipse.core.jenkins.api.JenkinsBuildDetailsResponse.ChangeSet.ChangeSetItem;

public class ChangeSetPathItem {
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
