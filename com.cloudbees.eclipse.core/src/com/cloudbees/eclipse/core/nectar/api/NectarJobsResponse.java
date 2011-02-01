package com.cloudbees.eclipse.core.nectar.api;

/**
 * Main response data
 * 
 * @author ahti
 */
public class NectarJobsResponse extends BaseNectarResponse {

  public Job[] jobs;

  static {
    initTreeQuery(NectarJobsResponse.class);
  }

  public static class Job {
    public HealthReport[] healthReport;

    public String displayName;
    public Boolean inQueue;
    public String color;

    public Boolean buildable;

    public Build lastBuild;
    public Build lastCompletedBuild;
    public Build lastFailedBuild;
    public Build lastStableBuild;
    public Build lastSuccessfulBuild;
    public Build lastUnstableBuild;
    public Build lastUnsuccessfulBuild;

    public static class Build {
      public String fullDisplayName;
      public String url;
      public String builtOn;
    }

    public static class HealthReport {

      public String description;
      public String iconUrl;
      public Long score;

    }

  }

  public final static String getTreeQuery() {
    //enforce this subclass to be loaded
    return _getTreeQuery();
  }

}
