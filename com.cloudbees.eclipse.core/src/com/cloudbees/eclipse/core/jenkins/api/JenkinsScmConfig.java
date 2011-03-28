package com.cloudbees.eclipse.core.jenkins.api;

public class JenkinsScmConfig {

  public Repository[] repos;

  public static class Repository {
    public String url;
    public String[] branches;
  }

}
