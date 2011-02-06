package com.cloudbees.eclipse.core.nectar.api;

import com.google.gson.annotations.Expose;

public class NectarJobBuildsResponse extends BaseNectarResponse {

  public String name;

  @Expose(deserialize = false, serialize = false)
  public final static String QTREE = QTreeFactory.create(NectarJobBuildsResponse.class);

  public Build[] builds;

  public static class Build {
    public long number;
    public String url;
    public long timestamp;
    public String result;
  }

}
