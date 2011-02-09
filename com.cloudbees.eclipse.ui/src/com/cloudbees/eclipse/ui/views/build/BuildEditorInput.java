package com.cloudbees.eclipse.ui.views.build;

import org.eclipse.ui.internal.part.NullEditorInput;

import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse.Job;

public class BuildEditorInput extends NullEditorInput {

  //private Job job;

  private String buildUrl;
  private String jobUrl;
  private String displayName;

  private boolean lastBuildAvailable = false;

  
  public BuildEditorInput(Job job) {
    super();
    //CloudBeesUIPlugin.getDefault().getLogger().info("Creating job details editor for url " + job.url);
    //this.job = job;

    this.displayName = job.displayName;

    setJobUrl(job.url);

    if (job.lastBuild != null && job.lastBuild.url != null) {
      setBuildUrl(job.lastBuild.url);
      lastBuildAvailable = true;
    } else {
      setBuildUrl(job.url);
    }

  }

  @Override
  public boolean exists() {
    return true;
  }

  public void setBuildUrl(String buildUrl) {
    this.buildUrl = buildUrl;
  }

  public String getBuildUrl() {
    return buildUrl;
  }

  public void setJobUrl(String jobUrl) {
    this.jobUrl = jobUrl;
  }

  public String getJobUrl() {
    return jobUrl;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setLastBuildAvailable(boolean lastBuildAvailable) {
    this.lastBuildAvailable = lastBuildAvailable;
  }

  public boolean isLastBuildAvailable() {
    return lastBuildAvailable;
  }

}
