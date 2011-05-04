package com.cloudbees.eclipse.core.jenkins.api;

public class ArtifactPathItem {
  public JenkinsBuildDetailsResponse parent;
  public JenkinsBuildDetailsResponse.Artifact item;

  public ArtifactPathItem(final JenkinsBuildDetailsResponse parent, final JenkinsBuildDetailsResponse.Artifact item) {
    this.parent = parent;
    this.item = item;
  }
}
