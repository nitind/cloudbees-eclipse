package com.cloudbees.eclipse.core.jenkins.api;


public class JenkinsBuild {
  public String name;
  public String displayName;
  public String fullDisplayName;
  public String url;
  public String builtOn;

  public Boolean building;
  public Long duration;
  public Long number;
  public Long timestamp;
  public String result;

  public Action[] actions;

  public static class Action {

    // Can be null
    public Cause[] causes;

    // Can be null. junit
    public Long failCount;
    public Long skipCount;
    public Long totalCount;
    public String urlName;

    public static class Cause {
      public String shortDescription;
    }
  }

  public String getDisplayName() {
    String result = this.fullDisplayName;
    if (result == null) {
      result = this.displayName;
      if (result == null) {
        result = this.name;
      }

      if (result != null && result.indexOf("#") < 0 && this.number != null) {
        result += " #" + this.number;
      }
    }
    return result;
  }
}