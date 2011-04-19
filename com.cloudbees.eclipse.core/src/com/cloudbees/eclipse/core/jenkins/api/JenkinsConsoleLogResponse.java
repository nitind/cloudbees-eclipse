package com.cloudbees.eclipse.core.jenkins.api;

import java.io.InputStream;

public class JenkinsConsoleLogResponse extends BaseJenkinsResponse {

  public InputStream logPart;
  public boolean hasMore;
  public long start;
  public String annotator;

}
