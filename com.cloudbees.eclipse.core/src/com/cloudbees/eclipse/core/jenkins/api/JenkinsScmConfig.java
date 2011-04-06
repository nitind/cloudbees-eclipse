package com.cloudbees.eclipse.core.jenkins.api;

import com.cloudbees.eclipse.core.forge.api.ForgeSync;

public class JenkinsScmConfig {

  public Repository[] repos;

  public static class Repository {
    public ForgeSync.TYPE type;
    public String url;
    public String[] branches;
  }

}
