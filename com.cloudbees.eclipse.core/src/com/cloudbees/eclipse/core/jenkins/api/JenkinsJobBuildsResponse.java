package com.cloudbees.eclipse.core.jenkins.api;

import com.google.gson.annotations.Expose;

public class JenkinsJobBuildsResponse extends BaseJenkinsResponse {

  public String name;

  @Expose(deserialize = false, serialize = false)
  public final static String QTREE = QTreeFactory.create(JenkinsJobBuildsResponse.class);

  public Boolean inQueue;

  public Build[] builds;
  public Boolean buildable;
  public String color;
  public HealthReport[] healthReport;

  public static class Build {
    public long number;
    public String url;
    public long timestamp;
    public String result;
  }

}
