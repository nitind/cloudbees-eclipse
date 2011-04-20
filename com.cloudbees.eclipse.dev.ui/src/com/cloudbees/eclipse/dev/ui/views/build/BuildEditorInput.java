package com.cloudbees.eclipse.dev.ui.views.build;

import org.eclipse.ui.internal.part.NullEditorInput;

import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse.Job;

public class BuildEditorInput extends NullEditorInput {

  //private Job job;

  private String buildUrl;
  private String jobUrl;
  private String displayName;

  private boolean lastBuildAvailable = false;


  public BuildEditorInput(final Job job) {
    super();
    //CloudBeesUIPlugin.getDefault().getLogger().info("Creating job details editor for url " + job.url);
    //this.job = job;

    this.displayName = job.getDisplayName();

    setJobUrl(job.url);

    if (job.lastBuild != null && job.lastBuild.url != null) {
      setBuildUrl(job.lastBuild.url);
      this.lastBuildAvailable = true;
    } else {
      setBuildUrl(job.url);
    }

  }

  @Override
  public boolean exists() {
    return true;
  }

  public void setBuildUrl(final String buildUrl) {
    this.buildUrl = buildUrl;
  }

  public String getBuildUrl() {
    return this.buildUrl;
  }

  public void setJobUrl(final String jobUrl) {
    this.jobUrl = jobUrl;
  }

  public String getJobUrl() {
    return this.jobUrl;
  }

  public String getDisplayName() {
    return this.displayName;
  }

  public void setLastBuildAvailable(final boolean lastBuildAvailable) {
    this.lastBuildAvailable = lastBuildAvailable;
  }

  public boolean isLastBuildAvailable() {
    return this.lastBuildAvailable;
  }

}
