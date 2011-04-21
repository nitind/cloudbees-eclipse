package com.cloudbees.eclipse.core.jenkins.api;

import com.google.gson.annotations.Expose;

public class JenkinsJobAndBuildsResponse extends BaseJenkinsResponse {

  @Expose(deserialize = false, serialize = false)
  public final static String QTREE = QTreeFactory.create(JenkinsJobAndBuildsResponse.class);

  public String name;
  public Boolean inQueue;

  public JenkinsBuild[] builds;
  public Boolean buildable;
  public String color;
  public HealthReport[] healthReport;
  public JenkinsJobProperty[] property;

}
